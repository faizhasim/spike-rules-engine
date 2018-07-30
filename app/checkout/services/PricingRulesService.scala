package checkout.services

import checkout.database.PricingRulesDao
import checkout.models
import checkout.models._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class PricingRulesService(pricingRulesDao: PricingRulesDao, checkoutService: CheckoutService) {

  val DefaultCustomerId = "_"

  private lazy val pricingRulesForDefaultCustomer: Future[Option[models.PricingRulesForCustomer]] = pricingRulesDao.listByCustomerId(DefaultCustomerId)
  private def extractDefaultPricingRuleBlockingly(product: ProductTypes.ProductType, defaultFlatRuleValue: BigDecimal): FlatRule = Await.result(
    pricingRulesForDefaultCustomer
      .map({
        case Some(pricingRulesForCustomer) => extractPricingRule(pricingRulesForCustomer, product)
        case _ => throw new Exception("Default Pricing Rules information is missing from database.")
      })
      .map(_.get)
      .filter(_.isInstanceOf[FlatRule])
      .map(_.asInstanceOf[FlatRule])
    , 1 second)

  private lazy val defaultClassicPricingRule: FlatRule = extractDefaultPricingRuleBlockingly(ProductTypes.Classic, BigDecimal("269.99"))
  private lazy val defaultStandoutPricingRule: FlatRule = extractDefaultPricingRuleBlockingly(ProductTypes.Standout, BigDecimal("322.99"))
  private lazy val defaultPremiumPricingRule: FlatRule = extractDefaultPricingRuleBlockingly(ProductTypes.Premium, BigDecimal("394.99"))
  private def defaultFlatPrice(product: ProductTypes.ProductType): BigDecimal = product match {
    case ProductTypes.Classic => BigDecimal(defaultClassicPricingRule.productPrice)
    case ProductTypes.Standout => BigDecimal(defaultStandoutPricingRule.productPrice)
    case ProductTypes.Premium => BigDecimal(defaultPremiumPricingRule.productPrice)
  }

  private def pricingRulesByCustomerId(customerId: String): Future[Option[models.PricingRulesForCustomer]] = pricingRulesDao.listByCustomerId(customerId)

  private def extractPricingRule(pricingRulesForCustomer: models.PricingRulesForCustomer, product: ProductTypes.ProductType): Option[PricingRule] = (product match {
    case ProductTypes.Classic => pricingRulesForCustomer.classicProductRule
    case ProductTypes.Standout => pricingRulesForCustomer.standoutProductRule
    case ProductTypes.Premium => pricingRulesForCustomer.premiumProductRule
  }).map(jsonString =>
    Json.parse(jsonString).validate[PricingRule] match {
      case s: JsSuccess[PricingRule] => s.get
      case _: JsError => throw new Exception(s"Unable to parse JSON: $jsonString")
    })

  private def calculateCostPerProduct(pricingRulesForCustomer: models.PricingRulesForCustomer, product: ProductTypes.ProductType, productIdCounts: Map[String, Int]) = {
    val pricingRuleOpt: Option[PricingRule] = product match {
      case ProductTypes.Classic => extractPricingRule(pricingRulesForCustomer, ProductTypes.Classic)
      case ProductTypes.Standout => extractPricingRule(pricingRulesForCustomer, ProductTypes.Standout)
      case ProductTypes.Premium => extractPricingRule(pricingRulesForCustomer, ProductTypes.Premium)
    }

    val countOpt = product match {
      case ProductTypes.Classic => productIdCounts.get(ProductTypes.Classic.id)
      case ProductTypes.Standout => productIdCounts.get(ProductTypes.Standout.id)
      case ProductTypes.Premium => productIdCounts.get(ProductTypes.Premium.id)
    }

    countOpt.map(count =>
      pricingRuleOpt match {
        case Some(pricingRule) =>
          pricingRule match {
            case rule: FlatRule => calculateFlatRule(rule, count)
            case rule: PayXForYRule => calculatePayXForYRule(rule, count, product)
            case rule: EqualsOrMorePurchasedRule => calculateEqualsOrMorePurchasedRule(rule, count, product)
          }
        case _ =>
          product match {
            case ProductTypes.Classic => calculateFlatRule(defaultClassicPricingRule, count)
            case ProductTypes.Standout => calculateFlatRule(defaultStandoutPricingRule, count)
            case ProductTypes.Premium => calculateFlatRule(defaultPremiumPricingRule, count)
          }
      }

    )
  }.getOrElse(BigDecimal("0.00"))

  private def calculateFlatRule(flatRule: FlatRule, count: Int): BigDecimal =
    count * BigDecimal(flatRule.productPrice)

  private def calculatePayXForYRule(payXForYRule: PayXForYRule, count: Int, product: ProductTypes.ProductType): BigDecimal = {
    val payXForYRuleFor = payXForYRule.`for`
    val payXForYRulePay = payXForYRule.pay
    val discountPortionRatio = Math.abs(count/payXForYRuleFor) * payXForYRulePay
    val nonDiscountPortionRatio = count % payXForYRuleFor
    defaultFlatPrice(product) * (discountPortionRatio + nonDiscountPortionRatio)
  }

  private def calculateEqualsOrMorePurchasedRule(equalsOrMorePurchasedRule: EqualsOrMorePurchasedRule, count: Int, product: ProductTypes.ProductType): BigDecimal = count match {
    case _ if count >= equalsOrMorePurchasedRule.productUnitAmount => count * BigDecimal(equalsOrMorePurchasedRule.productPrice)
    case _ => count * defaultFlatPrice(product)
  }

  def calculateTotalPriceForCustomer(customerId: String): Future[BigDecimal] = {
    def calculateFromIdCountsResult(productIdCounts: Map[String, Int]) = pricingRulesByCustomerId(customerId).map({
      case Some(pricingRulesForCustomer) =>
        val classicProductPrice = calculateCostPerProduct(pricingRulesForCustomer, ProductTypes.Classic, productIdCounts)
        val standoutProductPrice = calculateCostPerProduct(pricingRulesForCustomer, ProductTypes.Standout, productIdCounts)
        val premiumProductPrice = calculateCostPerProduct(pricingRulesForCustomer, ProductTypes.Premium, productIdCounts)
        classicProductPrice + standoutProductPrice + premiumProductPrice
      case _ => throw new Exception(s"Pricing rules for customer id $customerId not found.")
    })

    for (
      productIdCounts <- checkoutService.productCountByCustomerId(customerId);
      res <- calculateFromIdCountsResult(productIdCounts)
    ) yield res
  }
}
