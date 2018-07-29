package checkout

import checkout.controllers.CheckoutController
import checkout.database.CheckoutItemDao
import checkout.services.CheckoutService
import com.softwaremill.macwire._
import play.api.db.slick.SlickComponents
import play.api.mvc.ControllerComponents
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

trait CheckoutModule extends SlickComponents {
  def controllerComponents: ControllerComponents
  def dbConfig: DatabaseConfig[JdbcProfile]
  def executionContext: ExecutionContext

  lazy val checkoutController: CheckoutController = wire[CheckoutController]
  lazy val checkoutService: CheckoutService = wire[CheckoutService]
  lazy val checkoutDao: CheckoutItemDao = wire[CheckoutItemDao]
}
