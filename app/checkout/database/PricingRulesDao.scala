package checkout.database

import checkout.models.PricingRulesForCustomer
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class PricingRulesDao(dbConfig: DatabaseConfig[JdbcProfile]) {
  import dbConfig._
  import profile.api._

  private class PricingRulesTable(tag: Tag) extends Table[PricingRulesForCustomer](tag, "pricingrules") {
    def customerId = column[String]("customerId", O.PrimaryKey)
    def classicProductRule = column[Option[String]]("classicProductRule")
    def standoutProductRule = column[Option[String]]("standoutProductRule")
    def premiumProductRule = column[Option[String]]("premiumProductRule")

    override def * = (customerId, classicProductRule, standoutProductRule, premiumProductRule) <> ((PricingRulesForCustomer.apply _).tupled, PricingRulesForCustomer.unapply)
  }

  private val pricingRulesTableRef = TableQuery[PricingRulesTable]

  def list: Future[Seq[PricingRulesForCustomer]] = db.run {
    pricingRulesTableRef.result
  }

  def listByCustomerId(customerId: String): Future[Option[PricingRulesForCustomer]] = db.run {
    pricingRulesTableRef.filter(_.customerId === customerId).result.headOption
  }

}
