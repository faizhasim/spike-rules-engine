package checkout.database

import checkout.models.ProductTypes
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.meta.MTable

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class CheckoutItemDaoSpec extends PlaySpec with ScalaFutures {
  import slick.jdbc.H2Profile.api._
  trait InMemoryDatabaseForTest {
    val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("test.slickconfig")
    val checkoutItemDao = new CheckoutItemDao(dbConfig)
    checkoutItemDao.executeSlickOperation((db, table) => {
      val f = db
        .run(MTable.getTables)
        .flatMap(tables =>
          if (!tables.exists(_.name.name == table.baseTableRow.tableName)) {
            db.run {
              table.schema.create
            }
          } else {
            Future()
          }
        )
      Await.result(f, 2 seconds)
    })
  }

  "CheckoutItemDao DB behaviours" should {
    "get a result after adding `CheckoutItemDto`" in new InMemoryDatabaseForTest {
      val customerId = Random.nextString(5)
      val productId = ProductTypes.Classic.id

      val newCheckoutItem = Await.result(checkoutItemDao.add(customerId, productId), 5 seconds)
      newCheckoutItem.customerId mustBe customerId
      newCheckoutItem.productId mustBe productId
      newCheckoutItem.id mustBe defined

      val checkoutItem = Await.result(checkoutItemDao.get(newCheckoutItem.id.get), 5 seconds)
      checkoutItem.get mustBe newCheckoutItem
    }

    "be able to remove the result after adding `CheckoutItemDto`" in new InMemoryDatabaseForTest {
      val f = for (
        newCheckoutItem <- checkoutItemDao.add(Random.nextString(5), ProductTypes.Standout.id);
        _ <- checkoutItemDao.delete(newCheckoutItem.id.get);
        checkoutItem <- checkoutItemDao.get(newCheckoutItem.id.get)
      ) yield checkoutItem

      Await.result(f, 5 seconds) mustBe None
    }

    "be able to list down checkout items for a specific customer" in new InMemoryDatabaseForTest {
      val customerId = Random.nextString(5)
      val operation = for (
        _ <- checkoutItemDao.add(customerId, ProductTypes.Classic.id);
        _ <- checkoutItemDao.add(customerId, ProductTypes.Standout.id);
        _ <- checkoutItemDao.add(Random.nextString(6), ProductTypes.Premium.id);
        _ <- checkoutItemDao.add(customerId, ProductTypes.Premium.id);
        items <- checkoutItemDao.listByCustomerId(customerId)
      ) yield items

      val items = Await.result(operation, 5 seconds)
      items.size mustBe 3
    }

    "list down correct product counts for a specific customer" in new InMemoryDatabaseForTest {
      val customerId = Random.nextString(5)
      val operation = (for (
        _ <- checkoutItemDao.add(customerId, ProductTypes.Classic.id);
        _ <- checkoutItemDao.add(Random.nextString(6), ProductTypes.Classic.id);
        _ <- checkoutItemDao.add(customerId, ProductTypes.Standout.id);
        _ <- checkoutItemDao.add(customerId, ProductTypes.Standout.id);
        _ <- checkoutItemDao.add(customerId, ProductTypes.Standout.id);
        _ <- checkoutItemDao.add(customerId, ProductTypes.Premium.id);
        _ <- checkoutItemDao.add(customerId, ProductTypes.Premium.id);
        _ <- checkoutItemDao.add(Random.nextString(6), ProductTypes.Premium.id);
        productCounts <- checkoutItemDao.productCountByCustomerId(customerId)
      ) yield productCounts).map(
        _
          .groupBy(_._1)
          .map {
            case (k, v) => (k, v.map(_._2).head)
          }
      )

      val productCounts = Await.result(operation, 5 seconds)
      productCounts(ProductTypes.Classic.id) mustBe 1
      productCounts(ProductTypes.Standout.id) mustBe 3
      productCounts(ProductTypes.Premium.id) mustBe 2
    }
  }
}
