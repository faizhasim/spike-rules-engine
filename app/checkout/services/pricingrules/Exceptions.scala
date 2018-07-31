package checkout.services.pricingrules

import play.api.libs.json.JsError

sealed abstract class PricingRulesException(message: String = null, exception: Throwable = null) extends Exception(message, exception)
case class UnableToParseJson(jsError: JsError) extends PricingRulesException(message = jsError.errors.mkString(","))
case class DefaultPricingRulesMissingFromDatabase() extends PricingRulesException
case class PricingRulesNotFound(customerId: String) extends PricingRulesException(s"Pricing rules for customer id $customerId not found.")