package fi.oph.koski.schema

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.localization.LocalizedString.{concat, unlocalized}
import fi.oph.koski.localization.{LocalizationRepository, LocalizedString}
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation._


@Description("Aikuisten perusopetuksen opiskeluoikeus")
case class AikuistenPerusopetuksenOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  @Hidden
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None,
  @Description("Oppijan oppimäärän päättymispäivä")
  päättymispäivä: Option[LocalDate] = None,
  tila: AikuistenPerusopetuksenOpiskeluoikeudenTila,
  lisätiedot: Option[AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot] = None,
  suoritukset: List[AikuistenPerusopetuksenPäätasonSuoritus],
  @KoodistoKoodiarvo("aikuistenperusopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("aikuistenperusopetus", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
}

case class AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot(
  @KoodistoUri("perusopetuksentukimuoto")
  @Description("Opiskelijan saamat laissa säädetyt tukimuodot.")
  @Tooltip("Opiskelijan saamat laissa säädetyt tukimuodot (avustajapalvelut, erityiset apuvälineet, osa-aikainen erityisopetus, tukiopetus ja/tai tulkitsemispalvelut).")
  tukimuodot: Option[List[Koodistokoodiviite]] = None,
  @Description("Tehostetun tuen päätös alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, että päätöstä ei ole tehty.")
  @Description("Mahdollisen tehostetun tuen päätös päätöksen alkamis- ja päättymispäivät.")
  @SensitiveData
  @OksaUri("tmpOKSAID511", "tehostettu tuki")
  tehostetunTuenPäätös: Option[Päätösjakso] = None,
  @Description("Opiskelu ulkomailla alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas ole ulkomailla.")
  @Tooltip("Tieto opiskelusta ulkomailla alkamis- ja päättymispäivineen. Rahoituksen laskennassa käytettävä tieto.")
  ulkomailla: Option[Päätösjakso] = None,
  @Description("Opiskelija on vuosiluokkiin sitomattomassa opetuksessa (true/false).")
  @Tooltip("Onko opiskelija vuosiluokkiin sitomattomassa opetuksessa.")
  @DefaultValue(false)
  @Title("Vuosiluokkiin sitomaton opetus")
  @SensitiveData
  vuosiluokkiinSitoutumatonOpetus: Boolean = false,
  @Description("Onko oppija muu kuin vaikeimmin kehitysvammainen. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä, onko oppija muu kuin vaikeimmin kehitysvammainen (alku- ja loppupäivämäärät). Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData
  vammainen: Option[List[Aikajakso]] = None,
  @Description("Onko oppija vaikeasti kehitysvammainen. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä, onko oppija vaikeasti kehitysvammainen (alku- ja loppupäivämäärät). Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData
  vaikeastiVammainen: Option[List[Aikajakso]] = None,
  @Description("Opiskelijalla on majoitusetu. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä, jos opiskelijalla on majoitusetu (alku- ja loppupäivämäärät). Rahoituksen laskennassa käytettävä tieto.")
  majoitusetu: Option[Päätösjakso] = None,
  @Description("Opiskelijalla on oikeus maksuttomaan asuntolapaikkaan. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä, jos opiskelijalla on oikeus maksuttomaan asuntolapaikkaan (alku- ja loppupäivämäärät).")
  oikeusMaksuttomaanAsuntolapaikkaan: Option[Päätösjakso] = None,
  @Description("Sisäoppilaitosmuotoinen majoitus, aloituspäivä ja loppupäivä. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto")
  @Tooltip("Mahdollisen sisäoppilaitosmuotoisen majoituksen aloituspäivä ja loppupäivä. Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  sisäoppilaitosmainenMajoitus: Option[List[Aikajakso]] = None
) extends OpiskeluoikeudenLisätiedot

trait AikuistenPerusopetuksenPäätasonSuoritus extends KoskeenTallennettavaPäätasonSuoritus with Toimipisteellinen with MonikielinenSuoritus with Suorituskielellinen

case class AikuistenPerusopetuksenOppimääränSuoritus(
  @Title("Koulutus")
  @Tooltip("Suoritettava koulutus ja koulutuksen opetussuunnitelman perusteiden diaarinumero.")
  koulutusmoduuli: AikuistenPerusopetus,
  @Description("Luokan tunniste, esimerkiksi 9C.")
  @Tooltip("Luokan tunniste, esimerkiksi 9C.")
  luokka: Option[String] = None,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suoritustapa: Koodistokoodiviite,
  suorituskieli: Koodistokoodiviite,
  @Tooltip("Mahdolliset muut suorituskielet.")
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  override val osasuoritukset: Option[List[AikuistenPerusopetuksenOppiaineenSuoritus]] = None,
  @Tooltip("Mahdolliset todistuksella näkyvät lisätiedot.")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("aikuistenperusopetuksenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("aikuistenperusopetuksenoppimaara", koodistoUri = "suorituksentyyppi")
) extends AikuistenPerusopetuksenPäätasonSuoritus with PerusopetuksenOppimääränSuoritus

@Description("Aikuisten perusopetuksen tunnistetiedot")
case class AikuistenPerusopetus(
 perusteenDiaarinumero: Option[String],
 @KoodistoKoodiarvo("201101")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("201101", koodistoUri = "koulutus"),
 koulutustyyppi: Option[Koodistokoodiviite] = None
) extends Perusopetus

@Description("Perusopetuksen oppiaineen suoritus osana aikuisten perusopetuksen oppimäärän suoritusta")
case class AikuistenPerusopetuksenOppiaineenSuoritus(
  koulutusmoduuli: AikuistenPerusopetuksenOppiaine,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Title("Kurssit")
  override val osasuoritukset: Option[List[AikuistenPerusopetuksenKurssinSuoritus]] = None,
  @KoodistoKoodiarvo("aikuistenperusopetuksenoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "aikuistenperusopetuksenoppiaine", koodistoUri = "suorituksentyyppi"),
  suoritustapa: Option[Koodistokoodiviite] = None
) extends PerusopetuksenOppiaineenSuoritus with Vahvistukseton with MahdollisestiSuorituskielellinen with SuoritustapanaMahdollisestiErityinenTutkinto

trait AikuistenPerusopetuksenOppiaine extends PerusopetuksenOppiaine {
  @Tooltip("Oppiaineen laajuus kursseina.")
  def laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa]
}

trait AikuistenPerusopetuksenKoodistostaLöytyväOppiaine extends AikuistenPerusopetuksenOppiaine with YleissivistavaOppiaine {
  def kuvaus: Option[LocalizedString]
}

case class AikuistenPerusopetuksenPaikallinenOppiaine(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa] = None,
  kuvaus: LocalizedString,
  perusteenDiaarinumero: Option[String] = None,
  @DefaultValue(false)
  pakollinen: Boolean = false
) extends AikuistenPerusopetuksenOppiaine with PerusopetuksenPaikallinenOppiaine

case class MuuAikuistenPerusopetuksenOppiaine(
  @KoodistoKoodiarvo("OPA")
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  perusteenDiaarinumero: Option[String] = None,
  override val laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa] = None,
  kuvaus: Option[LocalizedString] = None
) extends MuuPerusopetuksenOppiaine with AikuistenPerusopetuksenKoodistostaLöytyväOppiaine

case class AikuistenPerusopetuksenÄidinkieliJaKirjallisuus(
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "AI", koodistoUri = "koskioppiaineetyleissivistava"),
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  perusteenDiaarinumero: Option[String] = None,
  override val laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa] = None,
  kuvaus: Option[LocalizedString] = None
) extends PerusopetuksenÄidinkieliJaKirjallisuus with AikuistenPerusopetuksenKoodistostaLöytyväOppiaine

case class AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli(
  tunniste: Koodistokoodiviite,
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  perusteenDiaarinumero: Option[String] = None,
  override val laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa] = None,
  kuvaus: Option[LocalizedString] = None
) extends PerusopetuksenVierasTaiToinenKotimainenKieli with AikuistenPerusopetuksenKoodistostaLöytyväOppiaine

case class AikuistenPerusopetuksenKurssinSuoritus(
  @Description("Aikuisten perusopetuksen kurssin tunnistetiedot")
  koulutusmoduuli: AikuistenPerusopetuksenKurssi,
  @FlattenInUI
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("aikuistenperusopetuksenkurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "aikuistenperusopetuksenkurssi", koodistoUri = "suorituksentyyppi"),
  @Description("Jos kurssi on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot.")
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen] = None
) extends KurssinSuoritus with MahdollisestiSuorituskielellinen

sealed trait AikuistenPerusopetuksenKurssi extends Koulutusmoduuli {
  def laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa]
}

case class PaikallinenAikuistenPerusopetuksenKurssi(
  @FlattenInUI
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa] = None
) extends AikuistenPerusopetuksenKurssi with PaikallinenKoulutusmoduuli with StorablePreference {
  def kuvaus: LocalizedString = LocalizedString.empty
}

@Title("Aikuisten perusopetuksen opetussuunnitelman 2015 mukainen kurssi")
@OnlyWhen("../../../../../koulutusmoduuli/perusteenDiaarinumero","19/011/2015")
@OnlyWhen("../../../koulutusmoduuli/perusteenDiaarinumero", "19/011/2015")
@OnlyWhen("../..", None) // allow standalone deserialization
case class ValtakunnallinenAikuistenPerusopetuksenKurssi2015(
  @KoodistoUri("aikuistenperusopetuksenkurssit2015")
  @Title("Nimi")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa] = None
) extends AikuistenPerusopetuksenKurssi with KoodistostaLöytyväKoulutusmoduuli

@Title("Aikuisten perusopetuksen päättövaiheen opetussuunnitelman 2017 mukainen kurssi")
@OnlyWhen("../../../../../koulutusmoduuli/perusteenDiaarinumero", "OPH-1280-2017")
@OnlyWhen("../../../koulutusmoduuli/perusteenDiaarinumero", "OPH-1280-2017")
@OnlyWhen("../..", None) // allow standalone deserialization
case class ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017(
  @KoodistoUri("aikuistenperusopetuksenpaattovaiheenkurssit2017")
  @Title("Nimi")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusVuosiviikkotunneissaTaiKursseissa] = None
) extends AikuistenPerusopetuksenKurssi with KoodistostaLöytyväKoulutusmoduuli

case class AikuistenPerusopetuksenOppiaineenOppimääränSuoritus(
  koulutusmoduuli: AikuistenPerusopetuksenOppiaine,
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  override val vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suoritustapa: Koodistokoodiviite,
  suorituskieli: Koodistokoodiviite,
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @Title("Kurssit")
  override val osasuoritukset: Option[List[AikuistenPerusopetuksenKurssinSuoritus]] = None,
  @KoodistoKoodiarvo("perusopetuksenoppiaineenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenoppiaineenoppimaara", koodistoUri = "suorituksentyyppi")
) extends AikuistenPerusopetuksenPäätasonSuoritus with OppiaineenSuoritus with Todistus with SuoritustavallinenPerusopetuksenSuoritus with PerusopetuksenOppiaineenOppimääränSuoritus

@Description("Ks. tarkemmin perusopetuksen opiskeluoikeuden tilat: [confluence](https://confluence.csc.fi/display/OPHPALV/KOSKI+opiskeluoikeuden+tilojen+selitteet+koulutusmuodoittain#KOSKIopiskeluoikeudentilojenselitteetkoulutusmuodoittain-Perusopetus)")
case class AikuistenPerusopetuksenOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[AikuistenPerusopetuksenOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class AikuistenPerusopetuksenOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: Koodistokoodiviite,
  @Description("Opintojen rahoitus")
  @KoodistoUri("opintojenrahoitus")
  @KoodistoKoodiarvo("1")
  @KoodistoKoodiarvo("6")
  override val opintojenRahoitus: Option[Koodistokoodiviite] = None
) extends KoskiOpiskeluoikeusjakso
