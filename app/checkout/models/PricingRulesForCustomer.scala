package checkout.models

import checkout.services.pricingrules.UnableToParseJson
import play.api.libs.json.{JsError, JsSuccess, Json, OFormat}

case class PricingRulesForCustomer(customerId: String, classicProductRule: Option[String], standoutProductRule: Option[String], premiumProductRule: Option[String]) {

  def pricingRuleOfProduct(product: ProductTypes.ProductType): Option[PricingRule] = (product match {
    case ProductTypes.Classic => classicProductRule
    case ProductTypes.Standout => standoutProductRule
    case ProductTypes.Premium => premiumProductRule
  }).map(jsonString =>
    Json.parse(jsonString).validate[PricingRule] match {
      case s: JsSuccess[PricingRule] => s.get
      case err: JsError => throw UnableToParseJson(err)
    })

}

object PricingRulesForCustomer {
  implicit val checkoutTrayFormat: OFormat[PricingRulesForCustomer] = Json.format[PricingRulesForCustomer]
}
