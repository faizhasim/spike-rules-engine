package checkout.models

import play.api.libs.json._

import scala.util.Try

case class PricingRuleFailedValidation(message: String) extends Exception(message)

sealed abstract class PricingRule {
  def validate: Try[_ <: PricingRule]
}
case class FlatRule(productPrice: String, strategy: String = "FlatRule") extends PricingRule {
  def calculatePrice(count: Int): BigDecimal = count * BigDecimal(productPrice)

  override def validate: Try[FlatRule] = Try {
    val parsedValue = BigDecimal(productPrice)
    if (parsedValue >= 0) this else throw PricingRuleFailedValidation(s"Product price must be greater or equal to 0.00. Captured value is $parsedValue.")
  }
}

case class PayXForYRule(pay: Int, `for`: Int, strategy: String = "PayXForYRule") extends PricingRule {
  override def validate: Try[PayXForYRule] = Try {
    if (pay >= 0 && `for` >= 0) this else throw PricingRuleFailedValidation("Both `pay` and `for` values must be positive integers.")
  }
}
case class EqualsOrMorePurchasedRule(productUnitAmount: Int, productPrice: String, strategy: String = "EqualsOrMorePurchasedRule") extends PricingRule {
  override def validate: Try[EqualsOrMorePurchasedRule] = Try {
    BigDecimal(productPrice) match {
      case parsedValue: BigDecimal if parsedValue < 0 =>
        throw PricingRuleFailedValidation(s"Product price must be greater or equal to 0.00. Captured value is $parsedValue.")
      case _: BigDecimal if productUnitAmount < 0 =>
        throw PricingRuleFailedValidation("`productUnitAmount` must be positive a integer.")
      case _: BigDecimal => this
    }
  }
}

object FlatRule {
  implicit val format: OFormat[FlatRule] = Json.format[FlatRule]
}

object PayXForYRule {
  implicit val format: OFormat[PayXForYRule] = Json.format[PayXForYRule]
}

object EqualsOrMorePurchasedRule {
  implicit val format: OFormat[EqualsOrMorePurchasedRule] = Json.format[EqualsOrMorePurchasedRule]
}

object PricingRule {
  implicit val writes: OWrites[PricingRule] = Json.writes[PricingRule]
  implicit val reads: Reads[PricingRule] = (json: JsValue) => {
    val strategyReads = (JsPath \ "strategy").read[String]
    val strategyResults: JsResult[String] = json.validate[String](strategyReads)
    strategyResults.map({
      case "PayXForYRule" => json.validate[PayXForYRule](Json.reads[PayXForYRule])
      case "FlatRule" => json.validate[FlatRule](Json.reads[FlatRule])
      case "EqualsOrMorePurchasedRule" => json.validate[EqualsOrMorePurchasedRule](Json.reads[EqualsOrMorePurchasedRule])
    }).get
  }
}

//object Test extends App {
////  println(Json.prettyPrint(Json.toJson(PricingRule(FlatRule("123.33")))))
////  println(Json.prettyPrint(Json.toJson(PricingRule(PayXForYRule(pay = 5, `for` = 3, ofValue = "23.55")))))
//  println(Json.stringify(Json.toJson(FlatRule("269.99"))))
//  println(Json.stringify(Json.toJson(PayXForYRule(pay = 2, `for` = 3))))
//  println(Json.stringify(Json.toJson(EqualsOrMorePurchasedRule(productUnitAmount = 4, productPrice = "379.99"))))
//
//  println(Json.parse("{\"productPrice\":\"269.99\",\"strategy\":\"FlatRule\"}").validate[PricingRule])
//}