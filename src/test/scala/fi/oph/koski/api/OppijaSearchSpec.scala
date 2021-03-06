package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.henkilo.MockOppijat._
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers.omniaKatselija
import fi.oph.koski.log.{AccessLogTester, AuditLogTester}
import org.scalatest.{FreeSpec, Matchers}

class OppijaSearchSpec extends FreeSpec with Matchers with SearchTestMethods with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen {
  "/api/henkilo/search" - {
    "Finds by name" in {
      resetFixtures
      searchForNames("eero") should equal(List("Jouni Eerola", "Eero Esimerkki", "Eéro Jorma-Petteri Markkanen-Fagerström"))
    }
    "Find only those from your organization" in {
      searchForNames("eero", omniaKatselija) should equal(List("Eéro Jorma-Petteri Markkanen-Fagerström"))
    }
    "Finds by hetu" in {
      searchForNames("010101-123N") should equal(List("Eero Esimerkki"))
    }
    "Finds only those that are in Koski" in {
      searchForNames(masterEiKoskessa.hetu.get) should equal(Nil)
    }
    "Finds with master info" in {
      createOrUpdate(MockOppijat.slaveMasterEiKoskessa.henkilö, defaultOpiskeluoikeus)
      searchForNames(masterEiKoskessa.hetu.get) should equal(List("Master Master"))
    }
    "Audit logging" in {
      search("eero", defaultUser) {
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPPIJA_HAKU", "target" -> Map("hakuEhto" -> "EERO")))
      }
    }
    "When query is too short" - {
      "Returns HTTP 400" in {
        search("aa", user = defaultUser) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.searchTermTooShort("Hakusanan pituus alle 3 merkkiä."))
        }
      }
    }
    "GET endpoints" - {
      "Finds by hetu, and does not include hetu in access log" in {
        authGet("api/henkilo/hetu/010101-123N") {
          verifyResponseStatusOk()
          body should include("Esimerkki")
          Thread.sleep(200) // wait for logging to catch up (there seems to be a slight delay)
          AccessLogTester.getLogMessages.lastOption.get.getMessage.toString should include("/koski/api/henkilo/hetu/* HTTP")
        }
      }
      "Finds by name, and does not include name in access log" in {
        authGet("api/henkilo/search?query=eero") {
          verifyResponseStatusOk()
          body should include("Eerola")
          Thread.sleep(200) // wait for logging to catch up (there seems to be a slight delay)
          AccessLogTester.getLogMessages.lastOption.get.getMessage.toString should include("/koski/api/henkilo/search?query=* HTTP")
        }
      }
    }
  }
}
