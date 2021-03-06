package fi.oph.koski.koskiuser

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.HtmlServlet
import fi.oph.koski.sso.SSOSupport

class LogoutServlet(implicit val application: KoskiApplication) extends HtmlServlet with SSOSupport {
  get("/") {
    logger.info("Logged out")

    val virkailija = sessionOrStatus match {
      case Right(session) if !session.user.kansalainen => true
      case Left(SessionStatusExpiredVirkailija) => true
      case _ => false
    }

    getUser.right.toOption.flatMap(_.serviceTicket).foreach(application.koskiSessionRepository.removeSessionByTicket)
    removeUserCookie

    if (virkailija) {
      redirectToLogout
    } else {
      kansalaisLogout
    }
  }

  private def kansalaisLogout = {
    val shibbolethLogoutUrl = application.config.getString("logout.url." + langFromDomain)
    if (shibbolethLogoutUrl.isEmpty) {
      redirectToFrontpage
    } else {
      redirect(shibbolethLogoutUrl)
    }
  }
}
