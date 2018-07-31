package checkout.services.pricingrules

import checkout.database.PricingRulesDao
import checkout.models._

class PriceCalculator(val pricingRulesDao: PricingRulesDao) extends PricingRulesOfDefaultProductCalculations {

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

  def calculatePrice(pricingRule: PricingRule, count: Int,
                     product: ProductTypes.ProductType): BigDecimal = pricingRule match {
    case flatRule: FlatRule =>
      calculateFlatRule(flatRule, count)
    case payXForYRule: PayXForYRule =>
      calculatePayXForYRule(payXForYRule, count, product)
    case equalsOrMorePurchasedRule: EqualsOrMorePurchasedRule =>
      calculateEqualsOrMorePurchasedRule(equalsOrMorePurchasedRule, count, product)
  }

  def calculatePriceOfDefaults(count: Int, product: ProductTypes.ProductType): BigDecimal =
    singleUnitPrice(product) * count
}
