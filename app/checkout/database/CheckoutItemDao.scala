package checkout.database

import checkout.models.CheckoutItemDto
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class CheckoutItemDao(dbConfig: DatabaseConfig[JdbcProfile]) {
  import dbConfig._
  import profile.api._

  private class CheckoutItemTable(tag: Tag) extends Table[CheckoutItemDto](tag, "checkoutitem") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
    def customerId = column[String]("customerId")
    def productId = column[String]("productId")

    def idxCustomerId = index("idx_customer_id", customerId)

    override def * = (id, customerId, productId) <> ((CheckoutItemDto.apply _).tupled, CheckoutItemDto.unapply)
  }

  private val checkoutTableRef = TableQuery[CheckoutItemTable]

  def add(customerId: String, productId: String): Future[CheckoutItemDto] = db.run {
    ( checkoutTableRef
          returning checkoutTableRef.map(_.id)
          into ((checkoutItem, id) => checkoutItem.copy(id = id))
    ) += CheckoutItemDto(None, customerId, productId)
  }

  def delete(id: Long): Future[Int] = db.run {
    checkoutTableRef
      .filter(_.id === id)
      .delete
  }

  def get(id: Long): Future[Option[CheckoutItemDto]] = db.run {
    checkoutTableRef
      .filter(_.id === id)
      .result
      .headOption
  }

  def listByCustomerId(customerId: String): Future[Seq[CheckoutItemDto]] = db.run {
    checkoutTableRef
      .filter(_.customerId === customerId)
      .result
  }

  def productCountByCustomerId(customerId: String): Future[Seq[(String, Int)]] = db.run {
    checkoutTableRef
      .filter(_.customerId === customerId)
      .sortBy(p => p.productId)
      .groupBy(_.productId)
      .map {
        case (productId, group) => (productId, group.size)
      }
      .result
  }

}
