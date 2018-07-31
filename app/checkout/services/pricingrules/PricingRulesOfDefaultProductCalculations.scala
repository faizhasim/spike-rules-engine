package checkout.services.pricingrules

import checkout.database.PricingRulesDao
import checkout.models
import checkout.models.{FlatRule, ProductTypes}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

trait PricingRulesOfDefaultProductCalculations {
  def pricingRulesDao: PricingRulesDao
  import PricingRulesOfDefaultProductCalculations._

  private lazy val pricingRulesForDefaultCustomer: Future[Option[models.PricingRulesForCustomer]] = pricingRulesDao.listByCustomerId(DefaultCustomerId)
  private def extractDefaultPricingRuleBlockingly(product: ProductTypes.ProductType, defaultFlatRuleValue: BigDecimal): FlatRule = Await.result(
    pricingRulesForDefaultCustomer
      .map({
        case Some(pricingRulesForCustomer) => pricingRulesForCustomer.pricingRuleOfProduct(product)
        case _ => throw DefaultPricingRulesMissingFromDatabase()
      })
      .map(_.get)
      .filter(_.isInstanceOf[FlatRule])
      .map(_.asInstanceOf[FlatRule])
    , 1 second)

  private lazy val classicPricingRule: FlatRule = extractDefaultPricingRuleBlockingly(ProductTypes.Classic, BigDecimal("269.99"))
  private lazy val standoutPricingRule: FlatRule = extractDefaultPricingRuleBlockingly(ProductTypes.Standout, BigDecimal("322.99"))
  private lazy val premiumPricingRule: FlatRule = extractDefaultPricingRuleBlockingly(ProductTypes.Premium, BigDecimal("394.99"))

  def singleUnitPrice(product: ProductTypes.ProductType): BigDecimal = product match {
    case ProductTypes.Classic => BigDecimal(classicPricingRule.productPrice)
    case ProductTypes.Standout => BigDecimal(standoutPricingRule.productPrice)
    case ProductTypes.Premium => BigDecimal(premiumPricingRule.productPrice)
  }
}

object PricingRulesOfDefaultProductCalculations {
  val DefaultCustomerId = "_"
}