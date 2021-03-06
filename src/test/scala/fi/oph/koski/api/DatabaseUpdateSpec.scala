package fi.oph.koski.api

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.KoskiDatabaseMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.OpiskeluOikeudetWithAccessCheck
import fi.oph.koski.koskiuser.KoskiSession.systemUser
import fi.oph.koski.koskiuser.MockUsers.stadinAmmattiopistoTallentaja
import fi.oph.koski.organisaatio.MockOrganisaatiot.omnia
import fi.oph.koski.schema._
import org.scalatest.{FreeSpec, Matchers}

class DatabaseUpdateSpec extends FreeSpec with Matchers with OpiskeluoikeusTestMethodsAmmatillinen with SearchTestMethods with LocalJettyHttpSpecification with KoskiDatabaseMethods {
  "Kun opiskeluoikeus päivitetään" - {
    "Oppilaitoksen muuttuessa oppilaitos_oid päivittyy" in {
      val opiskeluoikeus = createOpiskeluoikeus(defaultHenkilö, defaultOpiskeluoikeus, user = stadinAmmattiopistoTallentaja)
      putOpiskeluoikeus(opiskeluoikeus.copy(oppilaitos = Some(Oppilaitos(omnia)), koulutustoimija = None)) {
        verifyResponseStatusOk()
      }
      opiskeluoikeus.oid.flatMap(oppilaitosOid) should equal(Some(omnia))
    }
  }

  def opiskeluoikeusId(oo: AmmatillinenOpiskeluoikeus): Option[Int] =
    oo.oid.flatMap(oid => runDbSync(OpiskeluOikeudetWithAccessCheck(systemUser).filter(_.oid === oid).map(_.id).result).headOption)

  override protected def db: DB = KoskiApplicationForTests.masterDatabase.db

  private def oppilaitosOid(opiskeluoikeusOid: String): Option[String] =
    runDbSync(OpiskeluOikeudetWithAccessCheck(systemUser).filter(_.oid === opiskeluoikeusOid).map(_.oppilaitosOid).result).headOption
}
