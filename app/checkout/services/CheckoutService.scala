package checkout.services

import checkout.database.CheckoutItemDao
import checkout.models.CheckoutItemDto
import checkout.models.ProductTypes._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future


class CheckoutService(checkoutItemDao: CheckoutItemDao) {
  def addItem(customerId: String, product: ProductType): Future[CheckoutItemDto] = checkoutItemDao.add(customerId, product.id)
  def removeItem(id: Long): Future[Int] = checkoutItemDao.delete(id)
  def getItem(id: Long): Future[Option[CheckoutItemDto]] = checkoutItemDao.get(id)
  def listItemsByCustomerId(customerId: String): Future[Seq[CheckoutItemDto]] = checkoutItemDao.listByCustomerId(customerId)
  def productCountByCustomerId(customerId: String): Future[Map[String, Int]] =
    checkoutItemDao
      .productCountByCustomerId(customerId)
      .map(
        _
          .groupBy(_._1)
          .map {
            case (k, v) => (k, v.map(_._2).head)
          }
      )
}
