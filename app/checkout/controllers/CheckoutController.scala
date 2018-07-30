package checkout.controllers

import checkout.models.ProductTypes
import checkout.services.{CheckoutService, PricingRulesService}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future

class CheckoutController(cc: ControllerComponents, checkoutService: CheckoutService, pricingRulesService: PricingRulesService) extends AbstractController(cc) {

  def addCheckoutItem(customerId: String, productId: String): Action[AnyContent] = Action.async { implicit request =>
    ProductTypes.productFromId(productId) match {
      case Some(productType) =>
        for (res <- checkoutService.addItem(customerId, productType)) yield {
          Created(s"${res.id.get}")
        }
      case _ => Future {
        BadRequest(s"Product with product id $productId not found.")
      }
    }
  }

  def removeCheckoutItem(id: Long): Action[AnyContent] = Action.async { implicit request =>
    for (res <- checkoutService.removeItem(id)) yield Ok
  }

  def getCheckoutItem(id: Long): Action[AnyContent] = Action.async { implicit request =>
    for (res <- checkoutService.getItem(id)) yield res match {
      case Some(checkoutItem) => Ok(Json.toJson(checkoutItem))
      case _ => NotFound
    }
  }

  def listCheckoutItemByCustomerId(customerId: String): Action[AnyContent] = Action.async { implicit request =>
    for (checkoutItems <- checkoutService.listItemsByCustomerId(customerId)) yield {
      Ok(Json.toJson(checkoutItems))
    }
  }

  def checkoutItemSummaryByCustomerId(customerId: String): Action[AnyContent] = Action.async { implicit request =>
    for (
      countSummary <- checkoutService.productCountByCustomerId(customerId);
      totalPriceSummary <- pricingRulesService.calculateTotalPriceForCustomer(customerId)
    ) yield {

      Ok(Json.toJson(Json.obj(
        "countsByProductId" -> countSummary,
        "totalPrice" -> totalPriceSummary
      )))
    }
  }

}
