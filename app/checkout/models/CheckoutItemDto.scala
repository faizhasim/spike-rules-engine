package checkout.models

import play.api.libs.json.{Json, OFormat}

case class CheckoutItemDto(id: Option[Long], customerId: String, productId: String)

object CheckoutItemDto {
  implicit val checkoutTrayFormat: OFormat[CheckoutItemDto] = Json.format[CheckoutItemDto]
}