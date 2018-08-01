package checkout.services.pricingrules

import checkout.database.PricingRulesDao
import checkout.models
import checkout.models.{FlatRule, ProductTypes}
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

trait PricingRulesOfDefaultProductCalculations {
  def pricingRulesDao: PricingRulesDao
  import PricingRulesOfDefaultProductCalculations._

  private val logger = Logger(classOf[PricingRulesOfDefaultProductCalculations])

  private lazy val pricingRulesForDefaultCustomer: Future[Option[models.PricingRulesForCustomer]] = pricingRulesDao.listByCustomerId(DefaultCustomerId)
  private def extractDefaultPricingRuleBlockingly(product: ProductTypes.ProductType, defaultFlatRuleValue: BigDecimal): FlatRule = {
    val defaultValueFallback = FlatRule(defaultFlatRuleValue.toString())
    Try {
      val pricingRule = for (
        pricingRulesForCustomer <- Await.result[Option[models.PricingRulesForCustomer]](pricingRulesForDefaultCustomer, 1 second);
        pr <- pricingRulesForCustomer.pricingRuleOfProduct(product)
      ) yield pr

      pricingRule
        .getOrElse(defaultValueFallback)
        .asInstanceOf[FlatRule]
    } match {
      case Success(flatRule) => flatRule
      case Failure(err) =>
        logger.warn("Exception in extracting default pricing rule. Reverting to defaults from code.", err)
        defaultValueFallback
    }
  }

  lazy val classicPricingRule: FlatRule = extractDefaultPricingRuleBlockingly(ProductTypes.Classic, BigDecimal("269.99"))
  lazy val standoutPricingRule: FlatRule = extractDefaultPricingRuleBlockingly(ProductTypes.Standout, BigDecimal("322.99"))
  lazy val premiumPricingRule: FlatRule = extractDefaultPricingRuleBlockingly(ProductTypes.Premium, BigDecimal("394.99"))

  def singleUnitPrice(product: ProductTypes.ProductType): BigDecimal = product match {
    case ProductTypes.Classic => BigDecimal(classicPricingRule.productPrice)
    case ProductTypes.Standout => BigDecimal(standoutPricingRule.productPrice)
    case ProductTypes.Premium => BigDecimal(premiumPricingRule.productPrice)
  }
}

object PricingRulesOfDefaultProductCalculations {
  val DefaultCustomerId = "_"
}