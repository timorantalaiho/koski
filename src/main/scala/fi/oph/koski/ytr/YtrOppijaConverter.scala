package fi.oph.koski.ytr

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.log.Logging
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema._

case class YtrOppijaConverter(oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu, organisaatioRepository: OrganisaatioRepository, localizations: LocalizationRepository) extends Logging {
  def convert(ytrOppija: YtrOppija): Option[YlioppilastutkinnonOpiskeluoikeus] = {
    val ytl = organisaatioRepository.getOrganisaatio("1.2.246.562.10.43628088406")
      .flatMap(_.toKoulutustoimija)
      .getOrElse(throw new IllegalStateException(("Ylioppilastutkintolautakuntaorganisaatiota ei löytynyt organisaatiopalvelusta")))

    val oppilaitos = ytrOppija.graduationSchoolOphOid.flatMap(oid => oppilaitosRepository.findByOid(oid) match  {
      case None =>
        logger.error(s"Oppilaitosta $oid ei löydy")
        None
      case Some(oppilaitos) =>
        Some(oppilaitos)
    })

    val vahvistus = ytrOppija.graduationDate.map { graduationDate =>
      val helsinki: Koodistokoodiviite = koodistoViitePalvelu.validate("kunta", "091").getOrElse(throw new IllegalStateException("Helsingin kaupunkia ei löytynyt koodistopalvelusta"))
      Organisaatiovahvistus(graduationDate, helsinki, ytl.toOidOrganisaatio)
    }

    Some(YlioppilastutkinnonOpiskeluoikeus(
      lähdejärjestelmänId = Some(LähdejärjestelmäId(None, requiredKoodi("lahdejarjestelma", "ytr"))),
      oppilaitos = oppilaitos,
      koulutustoimija = Some(ytl),
      tila = YlioppilastutkinnonOpiskeluoikeudenTila(Nil),
      tyyppi = requiredKoodi("opiskeluoikeudentyyppi", "ylioppilastutkinto"),
      suoritukset = List(YlioppilastutkinnonSuoritus(
        tyyppi = requiredKoodi("suorituksentyyppi", "ylioppilastutkinto"),
        vahvistus = vahvistus,
        toimipiste = oppilaitos,
        koulutusmoduuli = Ylioppilastutkinto(requiredKoodi("koulutus", "301000"), None),
        pakollisetKokeetSuoritettu = ytrOppija.hasCompletedMandatoryExams,
        osasuoritukset = Some(ytrOppija.exams.flatMap(convertExam)))
      )
    ))
  }
  private def convertExam(exam: YtrExam) = koodistoViitePalvelu.validate("koskiyokokeet", exam.examId).map { tunniste =>
    val Pattern = "(\\d\\d\\d\\d)(K|S)".r
    val tutkintokerta = exam.period match {
      case Pattern(year, season) =>
        val seasonName = season match {
          case "K" => localizations.get("kevät")
          case "S" => localizations.get("syksy")
        }
        YlioppilastutkinnonTutkintokerta(exam.period, year.toInt, seasonName)
    }

    YlioppilastutkinnonKokeenSuoritus(
      koulutusmoduuli = YlioppilasTutkinnonKoe(tunniste),
      tutkintokerta = tutkintokerta,
      arviointi = Some(List(YlioppilaskokeenArviointi(requiredKoodi("koskiyoarvosanat", exam.grade), exam.points))),
      tyyppi = requiredKoodi("suorituksentyyppi", "ylioppilastutkinnonkoe")
    )
  }.orElse {
    logger.warn(s"Tuntematon yo-kokeen koetunnus: ${exam.examId}")
    None
  }

  private def requiredKoodi(uri: String, koodi: String) = {
    koodistoViitePalvelu.validateRequired(uri, koodi)
  }
}
