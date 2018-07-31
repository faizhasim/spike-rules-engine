package checkout.models

import play.api.libs.json._

sealed abstract class PricingRule
case class FlatRule(productPrice: String, strategy: String = "FlatRule") extends PricingRule {
  def calculatePrice(count: Int): BigDecimal = count * BigDecimal(productPrice)
}
case class PayXForYRule(pay: Int, `for`: Int, strategy: String = "PayXForYRule") extends PricingRule
case class EqualsOrMorePurchasedRule(productUnitAmount: Int, productPrice: String, strategy: String = "EqualsOrMorePurchasedRule") extends PricingRule

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