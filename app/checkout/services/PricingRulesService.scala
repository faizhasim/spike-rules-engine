package checkout.services

import checkout.database.PricingRulesDao
import checkout.models
import checkout.models._
import checkout.services.pricingrules.{PriceCalculator, PricingRulesOfDefaultProductCalculations}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.language.postfixOps

class PricingRulesService(pricingRulesDao: PricingRulesDao, checkoutService: CheckoutService, priceCalculator: PriceCalculator, pricingRulesOfDefaultProductCalculations: PricingRulesOfDefaultProductCalculations) {

  private[services] def calculateCostPerProduct(pricingRulesForCustomerOpt: Option[models.PricingRulesForCustomer], productIdCounts: Map[String, Int])(product: ProductTypes.ProductType) = {
    val pricingRuleOpt: Option[PricingRule] = pricingRulesForCustomerOpt match {
      case Some(pricingRulesForCustomer) =>
        product match {
          case ProductTypes.Classic => pricingRulesForCustomer.pricingRuleOfProduct(ProductTypes.Classic)
          case ProductTypes.Standout => pricingRulesForCustomer.pricingRuleOfProduct(ProductTypes.Standout)
          case ProductTypes.Premium => pricingRulesForCustomer.pricingRuleOfProduct(ProductTypes.Premium)
        }
      case _ =>
        product match {
          case ProductTypes.Classic => Some(pricingRulesOfDefaultProductCalculations.classicPricingRule)
          case ProductTypes.Standout => Some(pricingRulesOfDefaultProductCalculations.standoutPricingRule)
          case ProductTypes.Premium => Some(pricingRulesOfDefaultProductCalculations.premiumPricingRule)
        }
    }

    val countOpt = product match {
      case ProductTypes.Classic => productIdCounts.get(ProductTypes.Classic.id)
      case ProductTypes.Standout => productIdCounts.get(ProductTypes.Standout.id)
      case ProductTypes.Premium => productIdCounts.get(ProductTypes.Premium.id)
    }

    countOpt.map(count =>
      pricingRuleOpt match {
        case Some(pricingRule) =>
          priceCalculator.calculatePrice(pricingRule, count, product)
        case _ =>
          priceCalculator.calculatePriceOfDefaults(count, product)
      }
    )
  }.getOrElse(BigDecimal("0.00"))

  private[services] def calculateCost(pricingRulesForCustomerOpt: Option[models.PricingRulesForCustomer], productIdCounts: Map[String, Int]) =
    ProductTypes.products
      .map(calculateCostPerProduct(pricingRulesForCustomerOpt, productIdCounts))
      .sum

  def calculateTotalPriceForCustomer(customerId: String): Future[BigDecimal] = {
    def calculateFromIdCountsResult(productIdCounts: Map[String, Int]) =
      pricingRulesDao
        .listByCustomerId(customerId)
        .map({
          case Some(pricingRulesForCustomer) => calculateCost(Some(pricingRulesForCustomer), productIdCounts)
          case _ => calculateCost(None, productIdCounts)
        })

    for (
      productIdCounts <- checkoutService.productCountByCustomerId(customerId);
      res <- calculateFromIdCountsResult(productIdCounts)
    ) yield res
  }

}
