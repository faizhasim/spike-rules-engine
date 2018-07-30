package checkout.models

import play.api.libs.json.{Json, OFormat}

case class PricingRulesForCustomer(customerId: String, classicProductRule: Option[String], standoutProductRule: Option[String], premiumProductRule: Option[String])

object PricingRulesForCustomer {
  implicit val checkoutTrayFormat: OFormat[PricingRulesForCustomer] = Json.format[PricingRulesForCustomer]
}