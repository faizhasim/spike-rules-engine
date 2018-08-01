package checkout.services.pricingrules

import checkout.database.PricingRulesDao
import checkout.models._
import checkout.services.pricingrules.PricingRulesOfDefaultProductCalculations.DefaultCustomerId
import org.mockito.{ArgumentMatchers => m}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PriceCalculatorSpec extends PlaySpec with MockitoSugar with ScalaFutures {
  import org.mockito.Mockito._

  trait MockedDependencies {
    val pricingRulesDao: PricingRulesDao = mock[PricingRulesDao]
    val priceCalculator = new PriceCalculator(pricingRulesDao = pricingRulesDao)

    when(pricingRulesDao.listByCustomerId(m.eq(DefaultCustomerId))) thenReturn Future(None)

    pricingRulesDao.listByCustomerId(DefaultCustomerId)
  }

  "PriceCalculator#calculatePrice" should {
    "be able to calculate correct inputs based on `FlatRule` strategy" in new MockedDependencies {
      for (productType <- ProductTypes.products)
        priceCalculator.calculatePrice(
          FlatRule(BigDecimal("23.43").toString),
          3,
          productType
        ) mustBe 70.29

      priceCalculator.calculatePrice(
        FlatRule(BigDecimal("23.43").toString),
        0,
        ProductTypes.Classic
      ) mustBe 0

      priceCalculator.calculatePrice(
        FlatRule(BigDecimal(0).toString),
        3,
        ProductTypes.Classic
      ) mustBe 0
    }

    "be able to calculate correct inputs based on `PayXForYRule` strategy" in new MockedDependencies {
      priceCalculator.calculatePrice(
        PayXForYRule(pay = 2, `for` = 3),
        3,
        ProductTypes.Classic
      ) mustBe 539.98

      priceCalculator.calculatePrice(
        PayXForYRule(pay = 2, `for` = 3),
        3,
        ProductTypes.Standout
      ) mustBe 645.98

      priceCalculator.calculatePrice(
        PayXForYRule(pay = 2, `for` = 3),
        3,
        ProductTypes.Premium
      ) mustBe 789.98

      priceCalculator.calculatePrice(
        PayXForYRule(pay = 2, `for` = 3),
        0,
        ProductTypes.Classic
      ) mustBe 0.00
    }

    "be able to calculate correct inputs based on `EqualsOrMorePurchasedRule` strategy" in new MockedDependencies {
      priceCalculator.calculatePrice(
        EqualsOrMorePurchasedRule(productUnitAmount = 2, productPrice = "129.99"),
        1,
        ProductTypes.Classic
      ) mustBe 269.99

      priceCalculator.calculatePrice(
        EqualsOrMorePurchasedRule(productUnitAmount = 2, productPrice = "129.99"),
        1,
        ProductTypes.Standout
      ) mustBe 322.99

      priceCalculator.calculatePrice(
        EqualsOrMorePurchasedRule(productUnitAmount = 2, productPrice = "129.99"),
        1,
        ProductTypes.Premium
      ) mustBe 394.99

      for (productType <- ProductTypes.products) {
        priceCalculator.calculatePrice(
          EqualsOrMorePurchasedRule(productUnitAmount = 2, productPrice = "129.99"),
          2,
          productType
        ) mustBe 259.98

        priceCalculator.calculatePrice(
          EqualsOrMorePurchasedRule(productUnitAmount = 2, productPrice = "129.99"),
          3,
          productType
        ) mustBe 389.97
      }

    }

    "fail fast on invalid inputs" in new MockedDependencies {
      assertThrows[PriceCalculator.FailedValidation] {
        priceCalculator.calculatePrice(
          FlatRule(BigDecimal("23.43").toString),
          -3,
          ProductTypes.Classic
        )
      }

      assertThrows[PricingRuleFailedValidation] {
        priceCalculator.calculatePrice(
          FlatRule(BigDecimal("-23.43").toString),
          3,
          ProductTypes.Classic
        )
      }

      assertThrows[PricingRuleFailedValidation] {
        priceCalculator.calculatePrice(
          PayXForYRule(pay = 2, `for` = -3),
          3,
          ProductTypes.Classic
        )
      }

      assertThrows[PricingRuleFailedValidation] {
        priceCalculator.calculatePrice(
          PayXForYRule(pay = -2, `for` = 3),
          3,
          ProductTypes.Classic
        )
      }

      assertThrows[PricingRuleFailedValidation] {
        priceCalculator.calculatePrice(
          EqualsOrMorePurchasedRule(productUnitAmount = -1, productPrice = BigDecimal("23.43").toString),
          3,
          ProductTypes.Classic
        )
      }

      assertThrows[PricingRuleFailedValidation] {
        priceCalculator.calculatePrice(
          EqualsOrMorePurchasedRule(productUnitAmount = 1, productPrice = BigDecimal("-23.43").toString),
          3,
          ProductTypes.Classic
        )
      }

    }
  }

}
