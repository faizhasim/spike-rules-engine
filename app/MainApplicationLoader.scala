import checkout.CheckoutModule
import com.softwaremill.macwire._
import controllers.Assets
import org.flywaydb.play.FlywayPlayComponents
import play.api.ApplicationLoader.Context
import play.api.db.evolutions.EvolutionsComponents
import play.api.db.slick.evolutions.SlickDBApi
import play.api.db.slick.{DbName, SlickComponents}
import play.api.db.{DBApi, HikariCPComponents}
import play.api.i18n.I18nComponents
import play.api.routing.Router
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator}
import play.filters.HttpFiltersComponents
import play.filters.csrf.CSRFComponents
import play.filters.headers.SecurityHeadersComponents
import router.Routes
import slick.jdbc.JdbcProfile

class MainApplicationLoader extends ApplicationLoader {
  override def load(context: ApplicationLoader.Context): Application = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment, context.initialConfiguration, Map.empty)
    }
    new MainComponents(context).application
  }
}

class MainComponents(context: Context)
  extends BuiltInComponentsFromContext(context)
    with CheckoutModule
    with HttpFiltersComponents
    with I18nComponents with CSRFComponents with SecurityHeadersComponents
    with SlickComponents with FlywayPlayComponents with EvolutionsComponents with HikariCPComponents
    with controllers.AssetsComponents {
  // flywayPlayInitializer
  applicationEvolutions

  lazy val applicationController = new controllers.HomeController(controllerComponents) // manual wiring from template

  override lazy val assets: Assets = wire[Assets]
  lazy val prefix: String = "/"
  lazy val router: Router = wire[Routes]

  override lazy val dbConfig = slickApi.dbConfig[JdbcProfile](DbName("default"))

  override lazy val dbApi: DBApi = SlickDBApi(slickApi)
}
