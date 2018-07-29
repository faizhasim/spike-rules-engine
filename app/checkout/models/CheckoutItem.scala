package checkout.models

import play.api.libs.json.{Json, OFormat}

case class CheckoutItem(id: Option[Long], customerId: Long, productId: Long)

object CheckoutItem {
  implicit val checkoutTrayFormat: OFormat[CheckoutItem] = Json.format[CheckoutItem]
}