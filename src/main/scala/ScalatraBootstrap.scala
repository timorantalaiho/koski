import javax.servlet.ServletContext

import fi.oph.koski.IndexServlet
import fi.oph.koski.cache.CacheServlet
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db._
import fi.oph.koski.documentation.{KoodistoServlet, SchemaDocumentationServlet}
import fi.oph.koski.editor.EditorServlet
import fi.oph.koski.fixture.{FixtureServlet, Fixtures}
import fi.oph.koski.healthcheck.{HealthCheckApiServlet, HealthCheckHtmlServlet}
import fi.oph.koski.henkilo.HenkilötiedotServlet
import fi.oph.koski.history.KoskiHistoryServlet
import fi.oph.koski.koodisto.{KoodistoCreator, Koodistot}
import fi.oph.koski.koskiuser._
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusServlet, OpiskeluoikeusValidationServlet}
import fi.oph.koski.oppija.OppijaServlet
import fi.oph.koski.oppilaitos.OppilaitosServlet
import fi.oph.koski.suoritusote.SuoritusServlet
import fi.oph.koski.tiedonsiirto.TiedonsiirtoServlet
import fi.oph.koski.todistus.TodistusServlet
import fi.oph.koski.tutkinto.TutkintoServlet
import fi.oph.koski.util.Pools
import fi.oph.koski.validation.KoskiJsonSchemaValidator
import org.scalatra._

import scala.concurrent.Future

class ScalatraBootstrap extends LifeCycle with Logging with GlobalExecutionContext {
  override def init(context: ServletContext) {
    def mount(path: String, handler: Handler) = context.mount(handler, path)

    Future {
      // Parallel warm-up: org.reflections.Reflections takes a while to scan
      KoskiJsonSchemaValidator.henkilöSchema
    }

    Pools.init
    val application = Option(context.getAttribute("koski.application").asInstanceOf[KoskiApplication]).getOrElse(KoskiApplication.apply)

    if (application.config.getBoolean("koodisto.create")) {
      tryCatch("Koodistojen luonti") { KoodistoCreator.createKoodistotFromMockData(Koodistot.koskiKoodistot, application.config) }
    }


    mount("/", new IndexServlet(application))
    mount("/todistus", new TodistusServlet(application))
    mount("/opintosuoritusote", new SuoritusServlet(application))
    mount("/documentation", new SchemaDocumentationServlet(application.koodistoPalvelu))
    mount("/api/editor", new EditorServlet(application))
    mount("/api/healthcheck", new HealthCheckApiServlet(application))
    mount("/api/henkilo", new HenkilötiedotServlet(application))
    mount("/api/koodisto", new KoodistoServlet(application.koodistoPalvelu))
    mount("/api/opiskeluoikeus", new OpiskeluoikeusServlet(application))
    mount("/api/opiskeluoikeus/validate", new OpiskeluoikeusValidationServlet(application))
    mount("/api/opiskeluoikeus/historia", new KoskiHistoryServlet(application))
    mount("/api/oppija", new OppijaServlet(application))
    mount("/api/oppilaitos", new OppilaitosServlet(application))
    mount("/api/tiedonsiirrot", new TiedonsiirtoServlet(application))
    mount("/api/tutkinto", new TutkintoServlet(application.tutkintoRepository))
    mount("/healthcheck", new HealthCheckHtmlServlet(application))
    mount("/user", new UserServlet(application))
    mount("/user/logout", new LogoutServlet(application))
    mount("/cas", new CasServlet(application))
    mount("/cache", new CacheServlet(application))

    if (Fixtures.shouldUseFixtures(application.config)) {
      context.mount(new FixtureServlet(application), "/fixtures")
      application.fixtureCreator.resetFixtures
    }
  }

  override def destroy(context: ServletContext) = {
  }

  private def tryCatch(thing: String)(task: => Unit): Unit = {
    try {
      task
    } catch {
      case e: Exception => logger.error(e)(thing + " epäonnistui: " + e.getMessage)
    }
  }
}
