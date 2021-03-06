package fi.oph.koski.koskiuser

import java.lang.Thread.sleep

import fi.oph.koski.api.LocalJettyHttpSpecification
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.AuditLogTester
import org.scalatest.{FreeSpec, Matchers}

class AuthenticationSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification {
  "POST /login" - {
    "Valid credentials" in {
      post("user/login", JsonSerializer.writeWithRoot(Login("kalle", "kalle")), headers = jsonContent) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "LOGIN", "user" -> Map("oid" -> MockUsers.kalle.oid)))
      }
    }
    "Invalid credentials" in {
      post("user/login", JsonSerializer.writeWithRoot(Login("kalle", "asdf")), headers = jsonContent) {
        verifyResponseStatus(401, KoskiErrorCategory.unauthorized.loginFail("Sisäänkirjautuminen käyttäjätunnuksella kalle epäonnistui."))
      }

      // blocking because of too many login attempts
      post("user/login", JsonSerializer.writeWithRoot(Login("kalle", "kalle")), headers = jsonContent) {
        verifyResponseStatus(401, KoskiErrorCategory.unauthorized.loginFail("Sisäänkirjautuminen käyttäjätunnuksella kalle epäonnistui."))
      }

      sleep(1000)

      // blocking reset by now
      post("user/login", JsonSerializer.writeWithRoot(Login("kalle", "kalle")), headers = jsonContent) {
        verifyResponseStatusOk()
      }
    }
  }

  "GET /logout" in {
    get("user/logout") {
      verifyResponseStatusOk(302)
    }
  }
}
