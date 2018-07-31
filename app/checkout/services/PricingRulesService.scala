package checkout.services

import checkout.database.PricingRulesDao
import checkout.models
import checkout.models._
import checkout.services.pricingrules.{PriceCalculator, PricingRulesNotFound}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.language.postfixOps

class PricingRulesService(pricingRulesDao: PricingRulesDao, checkoutService: CheckoutService, priceCalculator: PriceCalculator) {

  private[services] def calculateCostPerProduct(pricingRulesForCustomer: models.PricingRulesForCustomer, productIdCounts: Map[String, Int])(product: ProductTypes.ProductType) = {
    val pricingRuleOpt: Option[PricingRule] = product match {
      case ProductTypes.Classic => pricingRulesForCustomer.pricingRuleOfProduct(ProductTypes.Classic)
      case ProductTypes.Standout => pricingRulesForCustomer.pricingRuleOfProduct(ProductTypes.Standout)
      case ProductTypes.Premium => pricingRulesForCustomer.pricingRuleOfProduct(ProductTypes.Premium)
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

  private[services] def calculateCost(pricingRulesForCustomer: models.PricingRulesForCustomer, productIdCounts: Map[String, Int]) =
    ProductTypes.products
      .map(calculateCostPerProduct(pricingRulesForCustomer, productIdCounts))
      .sum

  def calculateTotalPriceForCustomer(customerId: String): Future[BigDecimal] = {
    def calculateFromIdCountsResult(productIdCounts: Map[String, Int]) =
      pricingRulesDao
        .listByCustomerId(customerId)
        .map({
          case Some(pricingRulesForCustomer) => calculateCost(pricingRulesForCustomer, productIdCounts)
          case _ => throw PricingRulesNotFound(customerId)
        })

    for (
      productIdCounts <- checkoutService.productCountByCustomerId(customerId);
      res <- calculateFromIdCountsResult(productIdCounts)
    ) yield res
  }

}
