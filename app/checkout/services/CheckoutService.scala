package checkout.services

import checkout.database.CheckoutItemDao
import checkout.models.CheckoutItem

import scala.concurrent.Future

class CheckoutService(checkoutItemDao: CheckoutItemDao) {
  def addItem(customerId: Long, productId: Long): Future[CheckoutItem] = checkoutItemDao.add(customerId, productId)
  def removeItem(id: Long): Future[Int] = checkoutItemDao.delete(id)
  def getItem(id: Long): Future[Option[CheckoutItem]] = checkoutItemDao.get(id)
  def listItemsByCustomerId(customerId: Long): Future[Seq[CheckoutItem]] = checkoutItemDao.listByCustomerId(customerId)
}
