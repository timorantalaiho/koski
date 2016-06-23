package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedString._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.scalaschema.annotation.Description

case class KorkeakoulunOpiskeluoikeus(
  id: Option[Int],
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  alkamispäivä: Option[LocalDate],
  arvioituPäättymispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid],
  suoritukset: List[KorkeakouluSuoritus],
  tila: Option[KorkeakoulunOpiskeluoikeudenTila],
  läsnäolotiedot: Option[KorkeakoulunLäsnäolotiedot],
  @KoodistoKoodiarvo("korkeakoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("korkeakoulutus", Some("Korkeakoulutus"), "opiskeluoikeudentyyppi", None),
  ensisijaisuus: Option[Ensisijaisuus] = None
) extends Opiskeluoikeus {
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def versionumero = None
}

case class Ensisijaisuus(
  alkamispäivä: LocalDate,
  päättymispäivä: Option[LocalDate]
)

trait KorkeakouluSuoritus extends Suoritus {
  def toimipiste: Oppilaitos
}

  case class KorkeakouluTutkinnonSuoritus(
    koulutusmoduuli: KorkeakouluTutkinto,
    @KoodistoKoodiarvo("korkeakoulututkinto")
    tyyppi: Koodistokoodiviite = Koodistokoodiviite("korkeakoulututkinto", koodistoUri = "suorituksentyyppi"),
    arviointi: Option[List[KorkeakoulunArviointi]],
    tila: Koodistokoodiviite,
    vahvistus: Option[Henkilövahvistus],
    suorituskieli: Option[Koodistokoodiviite],
    toimipiste: Oppilaitos,
    override val osasuoritukset: Option[List[KorkeakoulunOpintojaksonSuoritus]]
  ) extends KorkeakouluSuoritus {
    override def tarvitseeVahvistuksen = false
  }

  case class KorkeakoulunOpintojaksonSuoritus(
    koulutusmoduuli: KorkeakoulunOpintojakso,
    @KoodistoKoodiarvo("korkeakoulunopintojakso")
    tyyppi: Koodistokoodiviite = Koodistokoodiviite("korkeakoulunopintojakso", koodistoUri = "suorituksentyyppi"),
    arviointi: Option[List[KorkeakoulunArviointi]],
    tila: Koodistokoodiviite,
    vahvistus: Option[Henkilövahvistus],
    suorituskieli: Option[Koodistokoodiviite],
    toimipiste: Oppilaitos,
    override val osasuoritukset: Option[List[KorkeakoulunOpintojaksonSuoritus]] = None
  ) extends KorkeakouluSuoritus {
    override def tarvitseeVahvistuksen = false
  }

@Description("Tutkintoon johtava koulutus")
case class KorkeakouluTutkinto(
  tunniste: Koodistokoodiviite
) extends Koulutus  {
  override def laajuus = None
  override def isTutkinto = true
}

@Description("Opintojakson suoritus")
case class KorkeakoulunOpintojakso(
  tunniste: PaikallinenKoodi,
  nimi: LocalizedString,
  laajuus: Option[LaajuusOpintopisteissä]
) extends Koulutusmoduuli

case class KorkeakoulunOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[KorkeakoulunOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class KorkeakoulunOpiskeluoikeusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @KoodistoUri("virtaopiskeluoikeudentila")
  tila: Koodistokoodiviite
) extends Opiskeluoikeusjakso

trait KorkeakoulunArviointi extends ArviointiPäivämäärällä {
  def hyväksytty = true
}

case class KorkeakoulunKoodistostaLöytyväArviointi(
  @KoodistoUri("virtaarvosana")
  arvosana: Koodistokoodiviite,
  päivä: LocalDate
) extends KoodistostaLöytyväArviointi with KorkeakoulunArviointi {
  override def arvioitsijat: Option[List[Arvioitsija]] = None
}

case class KorkeakoulunPaikallinenArviointi(
  arvosana: PaikallinenKoodi,
  päivä: LocalDate
) extends PaikallinenArviointi with KorkeakoulunArviointi {
  override def arvioitsijat: Option[List[Arvioitsija]] = None
}

case class LaajuusOpintopisteissä(
  arvo: Float,
  @KoodistoKoodiarvo("2")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite("2", Some(finnish("opintopistettä")), "opintojenlaajuusyksikko")
) extends Laajuus

case class KorkeakoulunLäsnäolotiedot(
  läsnäolojaksot: List[KorkeakoulunLäsnäolojakso]
) extends Läsnäolotiedot

case class KorkeakoulunLäsnäolojakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @KoodistoUri("virtalukukausiilmtila")
  tila: Koodistokoodiviite
) extends Läsnäolojakso