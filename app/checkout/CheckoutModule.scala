package checkout

import checkout.controllers.CheckoutController
import checkout.database.{CheckoutItemDao, PricingRulesDao}
import checkout.services.pricingrules.PriceCalculator
import checkout.services.{CheckoutService, PricingRulesService}
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
  lazy val pricingRulesDao: PricingRulesDao = wire[PricingRulesDao]
  lazy val pricingRulesService: PricingRulesService = wire[PricingRulesService]
  lazy val priceCalculator: PriceCalculator = wire[PriceCalculator]
}
