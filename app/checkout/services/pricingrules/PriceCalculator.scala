package checkout.services.pricingrules

import checkout.database.PricingRulesDao
import checkout.models._
import play.api.Logger

import scala.util.{Failure, Success, Try}

class PriceCalculator(val pricingRulesDao: PricingRulesDao) extends PricingRulesOfDefaultProductCalculations {
  import PriceCalculator._

  private val logger = Logger(getClass)

  private def calculateFlatRule(flatRule: FlatRule, count: Int): BigDecimal = flatRule.calculatePrice(count)

  private def calculatePayXForYRule(payXForYRule: PayXForYRule,
                                    count: Int,
                                    product: ProductTypes.ProductType): BigDecimal = {
    val payXForYRuleFor = payXForYRule.`for`
    val payXForYRulePay = payXForYRule.pay
    val discountPortionRatio = Math.abs(count/payXForYRuleFor) * payXForYRulePay
    val nonDiscountPortionRatio = count % payXForYRuleFor
    singleUnitPrice(product) * (discountPortionRatio + nonDiscountPortionRatio)
  }

  private def calculateEqualsOrMorePurchasedRule(equalsOrMorePurchasedRule: EqualsOrMorePurchasedRule,
                                                 count: Int, product:
                                                 ProductTypes.ProductType): BigDecimal =
    if (count >= equalsOrMorePurchasedRule.productUnitAmount)
      count * BigDecimal(equalsOrMorePurchasedRule.productPrice)
    else
      count * singleUnitPrice(product)

  private def validateOrFailEarly(count: Int): Try[Int] = Try {
    if (count >= 0)
      count
    else
      throw FailedValidation(s"Invalid count: $count")
  }

  def calculatePrice(pricingRule: PricingRule, count: Int,
                     product: ProductTypes.ProductType): BigDecimal = {
    validateOrFailEarly(count) match {
      case Success(_) =>
        pricingRule.validate match {
          case Success(_) =>
            pricingRule match {
              case flatRule: FlatRule =>
                calculateFlatRule(flatRule, count)
              case payXForYRule: PayXForYRule =>
                calculatePayXForYRule(payXForYRule, count, product)
              case equalsOrMorePurchasedRule: EqualsOrMorePurchasedRule =>
                calculateEqualsOrMorePurchasedRule(equalsOrMorePurchasedRule, count, product)
            }
          case Failure(err) =>
            logger.error("Failed to validate PricingRule", err)
            throw err
        }
      case Failure(err) =>
        logger.error("Unable to calculate price.", err)
        throw err
    }

  }

  def calculatePriceOfDefaults(count: Int, product: ProductTypes.ProductType): BigDecimal =
    singleUnitPrice(product) * count
}

object PriceCalculator {
  private[pricingrules] case class FailedValidation(message: String) extends Exception(message)
}
