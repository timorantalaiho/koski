package fi.oph.tor.schema

import java.time.LocalDate
import fi.oph.tor.schema.generic.annotation.{Description, ReadOnly}


case class TorOppija(
  henkilö: Henkilö,
  @Description("Lista henkilön opinto-oikeuksista. Sisältää vain ne opinto-oikeudet, joihin käyttäjällä on oikeudet. Esimerkiksi ammatilliselle toimijalle ei välttämättä näy henkilön lukio-opintojen tietoja")
  opintoOikeudet: Seq[OpiskeluOikeus]
)

@Description("Henkilötiedot. Syötettäessä vaaditaan joko `oid` tai kaikki muut kentät, jolloin järjestelmään voidaan tarvittaessa luoda uusi henkilö")
sealed trait Henkilö {}

case class HenkilöFull(
  @Description("Yksilöivä tunniste Opintopolku-palvelussa")
  @OksaUri("tmpOKSAID760", "oppijanumero")
  oid: String,
  @Description("Suomalainen henkilötunnus")
  hetu: String,
  @Description("Henkilön kaikki etunimet. Esimerkiksi Sanna Katariina")
  etunimet:String,
  @Description("Kutsumanimi, oltava yksi etunimistä. Esimerkiksi etunimille \"Juha-Matti Petteri\" kelpaavat joko \"Juha-Matti\", \"Juha\", \"Matti\" tai \"Petteri\"")
  kutsumanimi: String,
  sukunimi: String
) extends Henkilö

@Description("Henkilö, jonka oid ei ole tiedossa. Tietoja syötettäessä luodaan mahdollisesti uusi henkilö Henkilöpalveluun")
case class HenkilöNew(
  @Description("Suomalainen henkilötunnus")
  hetu: String,
  @Description("Henkilön kaikki etunimet. Esimerkiksi Sanna Katariina")
  etunimet:String,
  @Description("Kutsumanimi, oltava yksi etunimistä. Esimerkiksi etunimille \"Juha-Matti Petteri\" kelpaavat joko \"Juha-Matti\", \"Juha\", \"Matti\" tai \"Petteri\"")
  kutsumanimi: String,
  sukunimi: String
) extends Henkilö

@Description("Henkilö, jonka oid on tiedossa")
case class HenkilöOid(
  @Description("Yksilöivä tunniste Opintopolku-palvelussa")
  @OksaUri("tmpOKSAID760", "oppijanumero")
  oid: String
) extends Henkilö

object Henkilö {
  type Id = String
  def withOid(oid: String) = HenkilöOid(oid)
  def apply(hetu: String, etunimet: String, kutsumanimi: String, sukunimi: String) = HenkilöNew(hetu, etunimet, kutsumanimi, sukunimi)
}

case class OpiskeluOikeus(
  @Description("Opinto-oikeuden uniikki tunniste. Tietoja syötettäessä kenttä ei ole pakollinen. Tietoja päivitettäessä TOR tunnistaa opinto-oikeuden joko tämän id:n tai muiden kenttien (oppijaOid, organisaatio, diaarinumero) perusteella")
  id: Option[Int],
  @Description("Opiskelijan opinto-oikeuden alkamisaika joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa. Muoto YYYY-MM-DD")
  alkamispäivä: Option[LocalDate],
  @Description("Opiskelijan opinto-oikeuden arvioitu päättymispäivä joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa. Muoto YYYY-MM-DD")
  arvioituPäättymispäivä: Option[LocalDate],
  @Description("Opiskelijan opinto-oikeuden päättymispäivä joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa. Muoto YYYY-MM-DD")
  päättymispäivä: Option[LocalDate],
  @Description("Opinnot tarjoava koulutustoimija")
  koulutustoimija: Organisaatio,
  @Description("Oppilaitos, jossa opinnot on suoritettu")
  oppilaitos: Organisaatio,
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  toimipiste: Option[Organisaatio],
  @Description("Opinto-oikeuteen liittyvän (tutkinto-)suorituksen tiedot")
  suoritus: Suoritus,
  hojks: Option[Hojks],
  @Description("Opiskelijan suorituksen tavoite-tieto kertoo sen, suorittaako opiskelija tutkintotavoitteista koulutusta (koko tutkintoa) vai tutkinnon osa tavoitteista koulutusta (tutkinnon osaa)")
  @KoodistoUri("opintojentavoite")
  tavoite: Option[KoodistoKoodiViite],
  läsnäolotiedot: Option[Läsnäolotiedot],
  @Description("Opintojen rahoitus")
  @KoodistoUri("opintojenrahoitus")
  opintojenRahoitus: Option[KoodistoKoodiViite]
)

case class Suoritus(
  @Description("Koulutusmoduulin tunniste. Joko tutkinto tai tutkinnon osa")
  koulutusmoduuli: Koulutusmoduulitoteutus,
  @Description("Opintojen suorituskieli")
  @KoodistoUri("kieli")
  @OksaUri("tmpOKSAID309", "opintosuorituksen kieli")
  suorituskieli: Option[KoodistoKoodiViite],
  @Description("Tutkinnon tai tutkinnon osan suoritustapa")
  @OksaUri("tmpOKSAID141", "ammatillisen koulutuksen järjestämistapa")
  @OksaUri("tmpOKSAID142", "koulutusmuoto")
  koulutusMuoto: Option[KoulutusMuoto],
  hyväksiluku: Option[HyväksiLuku],
  @Description("Suorituksen tila")
  @KoodistoUri("suorituksentila")
  tila: Option[KoodistoKoodiViite],
  alkamispäivä: Option[LocalDate],
  arviointi: Option[List[Arviointi]],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[Suoritus]]
)

trait Koulutusmoduulitoteutus
  case class Koulutustoteutus(
    @Description("Tutkinnon 6-numeroinen tutkintokoodi")
    @KoodistoUri("koulutus")
    @OksaUri("tmpOKSAID560", "tutkinto")
    tutkintokoodi: KoodistoKoodiViite,
    @Description("Tutkinnon perusteen diaarinumero (pakollinen). Ks. ePerusteet-palvelu")
    perusteenDiaarinumero: Option[String],
    @Description("Tieto siitä mihin tutkintonimikkeeseen oppijan tutkinto liittyy")
    @KoodistoUri("tutkintonimikkeet")
    @OksaUri("tmpOKSAID588", "tutkintonimike")
    tutkintonimike: Option[List[KoodistoKoodiViite]] = None,
    @Description("Osaamisala")
    @KoodistoUri("osaamisala")
    @OksaUri(tunnus = "tmpOKSAID299", käsite = "osaamisala")
    osaamisala: Option[List[KoodistoKoodiViite]] = None
  ) extends Koulutusmoduulitoteutus

  case class TutkinnonosatoteutusOps(
    @Description("Tutkinnon osan kansallinen koodi")
    @KoodistoUri("tutkinnonosat")
    tutkinnonosakoodi: KoodistoKoodiViite,
    @Description("Onko pakollinen osa tutkinnossa")
    pakollinen: Boolean,
    paikallinenKoodi: Option[Paikallinenkoodi] = None,
    kuvaus: Option[String] = None
  ) extends Koulutusmoduulitoteutus

  case class TutkinnonosatoteutusPaikallinen(
    paikallinenKoodi: Paikallinenkoodi,
    kuvaus: String,
    @Description("Onko pakollinen osa tutkinnossa")
    pakollinen: Boolean
  ) extends Koulutusmoduulitoteutus

case class Arviointi(
  @Description("Arvosana. Kullekin arviointiasteikolle löytyy oma koodistonsa")
  arvosana: KoodistoKoodiViite,
  päivä: Option[LocalDate],
  @Description("Onko kyseessä arvosanan korotus")
  arvosananKorottaminen: Option[Boolean],
  @Description("Tutkinnon osan suorituksen arvioinnista päättäneen henkilön nimi")
  arvioitsijat: Option[List[Arvioitsija]]
)

case class Arvioitsija(
  nimi: String
)

case class Vahvistus(
  @Description("Tutkinnon tai tutkinnonosan vahvistettu suorituspäivämäärä, eli päivämäärä jolloin suoritus on hyväksyttyä todennettua osaamista.")
  päivä: Option[LocalDate]
)

sealed trait KoulutusMuoto {
  def järjestämismuoto: KoodistoKoodiViite
  def suoritustapa: KoodistoKoodiViite
}

case class OppilaitosmuotoinenOpetussuunnitelmaperusteinenKoulutus(
  @KoodistoUri("järjestämismuoto")
  järjestämismuoto: KoodistoKoodiViite,
  @KoodistoUri("suoritustapa")
  suoritustapa: KoodistoKoodiViite
) extends KoulutusMuoto

case class OppilaitosmuotoinenNäyttötutkintoonValmistavaKoulutus(
  @KoodistoUri("järjestämismuoto")
  järjestämismuoto: KoodistoKoodiViite,
  @KoodistoUri("suoritustapa")
  suoritustapa: KoodistoKoodiViite,
  näyttö: Näyttö
) extends KoulutusMuoto

case class OppisopimusmuotoinenOpetussuunnitelmaperusteinenKoulutus(
  @KoodistoUri("järjestämismuoto")
  järjestämismuoto: KoodistoKoodiViite,
  @KoodistoUri("suoritustapa")
  suoritustapa: KoodistoKoodiViite,
  oppisopimus: Oppisopimus
) extends KoulutusMuoto

case class OppisopimusmuotoinenNäyttötutkintoonValmistavaKoulutus(
  @KoodistoUri("järjestämismuoto")
  järjestämismuoto: KoodistoKoodiViite,
  @KoodistoUri("suoritustapa")
  suoritustapa: KoodistoKoodiViite,
  näyttö: Näyttö,
  oppisopimus: Oppisopimus
) extends KoulutusMuoto

case class HyväksiLuku (
  @Description("Aiemman, korvaavan suorituksen kuvaus")
  osaaminen: Koulutusmoduulitoteutus,
  @Description("Osaamisen tunnustamisen kautta saatavan tutkinnon osan suorituksen selite")
  @OksaUri("tmpOKSAID629", "osaamisen tunnustaminen")
  selite: Option[String]
)

@Description("Näytön kuvaus")
case class Näyttö(
  kuvaus: String,
  suorituspaikka: String
)

@Description("Oppisopimuksen tiedot")
case class Oppisopimus(
  työnantaja: Yritys
)

case class Yritys(
  nimi: String,
  yTunnus: String
)

case class Läsnäolotiedot(
  läsnäolojaksot: List[Läsnäolojakso]
)

case class Läsnäolojakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @Description("Läsnäolotila (läsnä, poissa...)")
  @KoodistoUri("lasnaolotila")
  tila: KoodistoKoodiViite
)

case class Kunta(koodi: String, nimi: Option[String])

case class KoodistoKoodiViite(
  @Description("Koodin tunniste koodistossa")
  koodiarvo: String,
  @Description("Koodin selväkielinen, kielistetty nimi")
  @ReadOnly("Tiedon syötössä kuvausta ei tarvita; kuvaus haetaan Koodistopalvelusta")
  nimi: Option[String],
  @Description("Käytetyn koodiston tunniste")
  koodistoUri: String,
  @Description("Käytetyn koodiston versio")
  koodistoVersio: Int
)

@Description("Henkilökohtainen opetuksen järjestämistä koskeva suunnitelma, https://fi.wikipedia.org/wiki/HOJKS")
@OksaUri("tmpOKSAID228", "erityisopiskelija")
case class Hojks(hojksTehty: Boolean)

@Description("Paikallinen, koulutustoimijan oma kooditus koulutukselle. Käytetään kansallisen koodiston puuttuessa")
case class Paikallinenkoodi(
  @Description("Koodin tunniste koodistossa")
  koodiarvo: String,
  @Description("Koodin selväkielinen nimi")
  nimi: String,
  @Description("Koodiston tunniste")
  koodistoUri: String
)

case class Organisaatio(
  @Description("Organisaation tunniste Opintopolku-palvelussa")
  oid: String,
  @Description("Organisaation (kielistetty) nimi")
  @ReadOnly("Tiedon syötössä nimeä ei tarvita; kuvaus haetaan Organisaatiopalvelusta")
  nimi: Option[String] = None
)
