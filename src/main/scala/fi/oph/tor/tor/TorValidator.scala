package fi.oph.tor.tor

import fi.oph.tor.http.{TorErrorCategory, HttpStatus}
import fi.oph.tor.json.Json
import fi.oph.tor.koodisto.KoodistoViitePalvelu
import fi.oph.tor.organisaatio.OrganisaatioRepository
import fi.oph.tor.schema._
import fi.oph.tor.tor.DateValidation._
import fi.oph.tor.toruser.TorUser
import fi.oph.tor.tutkinto.{TutkintoRakenneValidator, TutkintoRepository}
import fi.oph.tor.util.Timing
import org.json4s.{JArray, JValue}

class TorValidator(tutkintoRepository: TutkintoRepository, val koodistoPalvelu: KoodistoViitePalvelu, val organisaatioRepository: OrganisaatioRepository) extends Timing {
  def validateAsJson(oppija: TorOppija)(implicit user: TorUser): Either[HttpStatus, TorOppija] = {
    extractAndValidate(Json.toJValue(oppija))
  }

  def extractAndValidateBatch(parsedJson: JArray)(implicit user: TorUser): List[Either[HttpStatus, TorOppija]] = {
    timed("extractAndValidateBatch") {
      parsedJson.arr.par.map { row =>
        extractAndValidate(row.asInstanceOf[JValue])
      }.toList
    }
  }

  def fillMissingInfo(oppija: TorOppija) = oppija.copy(opiskeluoikeudet = oppija.opiskeluoikeudet.map(addKoulutusToimija(_)))

  def addKoulutusToimija(oo: Opiskeluoikeus) = {
    organisaatioRepository.getOrganisaatioHierarkiaIncludingParents(oo.oppilaitos.oid) match {
      case Some(hierarkia) => oo.withKoulutustoimija(hierarkia.toOrganisaatio)
      case _ => oo
    }
  }

  def extractAndValidate(parsedJson: JValue)(implicit user: TorUser): Either[HttpStatus, TorOppija] = {
    timed("extractAndValidate") {
      TorJsonSchemaValidator.jsonSchemaValidate(parsedJson) match {
        case status: HttpStatus if status.isOk =>
          val extractionResult: Either[HttpStatus, TorOppija] = ValidatingAndResolvingExtractor.extract[TorOppija](parsedJson, ValidationAndResolvingContext(koodistoPalvelu, organisaatioRepository))
          extractionResult.right.flatMap { oppija =>
            validateOpiskeluoikeudet(oppija.opiskeluoikeudet) match {
              case status: HttpStatus if status.isOk => Right(fillMissingInfo(oppija))
              case status: HttpStatus => Left(status)
            }
          }
        case status: HttpStatus => Left(status)
      }
    }
  }

  private def validateOpiskeluoikeudet(opiskeluoikeudet: Seq[Opiskeluoikeus])(implicit user: TorUser): HttpStatus = {
    if (opiskeluoikeudet.length == 0) {
      TorErrorCategory.badRequest.validation.tyhjäOpiskeluoikeusLista()
    }
    else {
      HttpStatus.fold(opiskeluoikeudet.map(validateOpiskeluOikeus))
    }
  }

  private def validateOpiskeluOikeus(opiskeluOikeus: Opiskeluoikeus)(implicit user: TorUser): HttpStatus = {
    HttpStatus.validate(user.hasReadAccess(opiskeluOikeus.oppilaitos)) { TorErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon " + opiskeluOikeus.oppilaitos.oid) }
      .then { HttpStatus.fold(
      validateDateOrder(("alkamispäivä", opiskeluOikeus.alkamispäivä), ("päättymispäivä", opiskeluOikeus.päättymispäivä)),
      validateDateOrder(("alkamispäivä", opiskeluOikeus.alkamispäivä), ("arvioituPäättymispäivä", opiskeluOikeus.arvioituPäättymispäivä)),
      DateValidation.validateJaksot("opiskeluoikeudenTila.opiskeluoikeusjaksot", opiskeluOikeus.opiskeluoikeudenTila.toList.flatMap(_.opiskeluoikeusjaksot)),
      DateValidation.validateJaksot("läsnäolotiedot.läsnäolojaksot", opiskeluOikeus.läsnäolotiedot.toList.flatMap(_.läsnäolojaksot)),
      HttpStatus.fold(opiskeluOikeus.suoritukset.map(validateSuoritus(_, None)))
    )}
      .then {
        HttpStatus.fold(opiskeluOikeus.suoritukset.map(TutkintoRakenneValidator(tutkintoRepository).validateTutkintoRakenne(_)))
      }
  }

  def validateSuoritus(suoritus: Suoritus, parent: Option[Suoritus]): HttpStatus = {
    val arviointipäivä = ("suoritus.arviointi.päivä", suoritus.arviointi.toList.flatten.flatMap(_.päivä))
    HttpStatus.fold(
      validateDateOrder(("suoritus.alkamispäivä", suoritus.alkamispäivä), arviointipäivä)
        :: validateDateOrder(arviointipäivä, ("suoritus.vahvistus.päivä", suoritus.vahvistus.map(_.päivä)))
        :: validateStatus(suoritus, parent)
        :: suoritus.osasuoritusLista.map(validateSuoritus(_, Some(suoritus)))
    )
  }

  private def validateStatus(suoritus: Suoritus, parent: Option[Suoritus]): HttpStatus = {
    val hasArviointi: Boolean = !suoritus.arviointi.toList.flatten.isEmpty
    val hasVahvistus: Boolean = suoritus.vahvistus.isDefined
    val tilaValmis: Boolean = suoritus.tila.koodiarvo == "VALMIS"
    def suorituksenTunniste(suoritus: Suoritus): KoodiViite = {
      suoritus.koulutusmoduuli.tunniste
    }
    if (hasVahvistus && !tilaValmis) {
      TorErrorCategory.badRequest.validation.tila.vahvistusVäärässäTilassa("Suorituksella " + suorituksenTunniste(suoritus) + " on vahvistus, vaikka suorituksen tila on " + suoritus.tila.koodiarvo)
    } else if (!hasVahvistus && tilaValmis && !parent.isDefined) {
      TorErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta " + suorituksenTunniste(suoritus) + " puuttuu vahvistus, vaikka suorituksen tila on " + suoritus.tila.koodiarvo)
    } else {
      (tilaValmis, suoritus.rekursiivisetOsasuoritukset.find(_.tila.koodiarvo == "KESKEN")) match {
        case (true, Some(keskeneräinenOsasuoritus)) =>
          TorErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus(
            "Suorituksella " + suorituksenTunniste(suoritus) + " on keskeneräinen osasuoritus " + suorituksenTunniste(keskeneräinenOsasuoritus) + " vaikka suorituksen tila on " + suoritus.tila.koodiarvo)
        case _ =>
          HttpStatus.ok
      }
    }
  }
}
