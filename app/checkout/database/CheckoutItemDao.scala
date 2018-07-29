package checkout.database

import checkout.models.CheckoutItem
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class CheckoutItemDao(dbConfig: DatabaseConfig[JdbcProfile]) {
  import dbConfig._
  import profile.api._

  private class CheckoutItemTable(tag: Tag) extends Table[CheckoutItem](tag, "checkoutitem") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
    def customerId = column[Long]("customerId")
    def productId = column[Long]("productId")

    def idxCustomerId = index("idx_customer_id", customerId)
    def idxUniqueCustomerProduct = index("idx_unique_customer_product", (customerId, productId), unique = true)

    override def * = (id, customerId, productId) <> ((CheckoutItem.apply _).tupled, CheckoutItem.unapply)
  }

  private val checkoutTableRef = TableQuery[CheckoutItemTable]

  def add(customerId: Long, productId: Long): Future[CheckoutItem] = db.run {
    ( checkoutTableRef
          returning checkoutTableRef.map(_.id)
          into ((checkoutItem, id) => checkoutItem.copy(id = id))
    ) += CheckoutItem(None, customerId, productId)
  }

  def delete(id: Long): Future[Int] = db.run {
    checkoutTableRef
      .filter(_.id === id)
      .delete
  }

  def get(id: Long): Future[Option[CheckoutItem]] = db.run {
    checkoutTableRef
      .filter(_.id === id)
      .result
      .headOption
  }

  def listByCustomerId(customerId: Long): Future[Seq[CheckoutItem]] = db.run {
    checkoutTableRef
      .filter(_.customerId === customerId)
      .result
  }

}
