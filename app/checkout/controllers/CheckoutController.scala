package checkout.controllers

import checkout.services.CheckoutService
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class CheckoutController(cc: ControllerComponents, checkoutService: CheckoutService) extends AbstractController(cc) {

  def index() = Action.async { implicit request: Request[AnyContent] =>
    for (
      res <- checkoutService.addItem(2,2)
    ) yield {
      Ok(s"result = $res")
    }

//    Ok(s"Ok")
  }

}
