package checkout.services

import checkout.database.CheckoutItemDao
import checkout.models.{CheckoutItemDto, ProductTypes}
import org.mockito.invocation.InvocationOnMock
import org.mockito.{Mockito, ArgumentMatchers => m}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

class CheckoutServiceSpec extends PlaySpec with MockitoSugar with ScalaFutures {
  import org.mockito.Mockito._
  trait MockedDependencies {
    val checkoutItemDao: CheckoutItemDao = mock[CheckoutItemDao]

  }

  "CheckoutService#addItem" should {
    "call the associated DAO with expected parameters" in new MockedDependencies {
      val checkoutService = new CheckoutService(checkoutItemDao)
      when(checkoutItemDao.add(m.anyString(), m.anyString())) thenAnswer ((invocation: InvocationOnMock) =>
        Future { CheckoutItemDto(Some(1L), invocation.getArgument(0), invocation.getArgument(1)) }
      )

      for (product <- ProductTypes.products) {
        checkoutService.addItem("customerId", product)
        verify(checkoutItemDao).add(m.eq("customerId"), m.eq(product.id))
      }
    }
  }

  "CheckoutService#removeItem" should {
    "call the associated DAO with expected parameters" in new MockedDependencies {
      val checkoutService = new CheckoutService(checkoutItemDao)
      val id = 1
      checkoutService.removeItem(id)
      verify(checkoutItemDao, Mockito.atMost(1)).delete(m.eq(id))
    }
  }

  "CheckoutService#getItem" should {
    "call the associated DAO with expected parameters" in new MockedDependencies {
      val checkoutService = new CheckoutService(checkoutItemDao)
      val idThatExist = 1L
      val expectedCheckoutItemDto = CheckoutItemDto(Some(idThatExist), Random.nextString(5), Random.nextString(5))
      when(checkoutItemDao.get(m.anyLong())) thenReturn Future(None)
      when(checkoutItemDao.get(m.eq(idThatExist))) thenReturn Future(Some(expectedCheckoutItemDto))

      whenReady(checkoutService.getItem(idThatExist)) {
        _ mustBe Some(expectedCheckoutItemDto)
      }

      whenReady(checkoutService.getItem(2L)) {
        _ mustBe None
      }
    }
  }

  "CheckoutService#listItemsByCustomerId" should {
    "call the associated DAO with expected parameters" in new MockedDependencies {
      val checkoutService = new CheckoutService(checkoutItemDao)
      val customerIdThatExist = Random.nextString(5)
      val expectedCheckoutItemDtos = List(
        CheckoutItemDto(Some(Random.nextLong()), Random.nextString(5), Random.nextString(5)),
        CheckoutItemDto(Some(Random.nextLong()), Random.nextString(5), Random.nextString(5))
      )
      when(checkoutItemDao.listByCustomerId(m.anyString())) thenReturn Future(List())
      when(checkoutItemDao.listByCustomerId(m.eq(customerIdThatExist))) thenReturn Future(expectedCheckoutItemDtos)

      whenReady(checkoutService.listItemsByCustomerId(customerIdThatExist)) {
        _ mustBe expectedCheckoutItemDtos
      }

      whenReady(checkoutService.listItemsByCustomerId(Random.nextString(6))) {
        _ mustBe List()
      }

    }
  }

  "CheckoutService#productCountByCustomerId" should {
    "call the associated DAO with expected parameters" in new MockedDependencies {
      val checkoutService = new CheckoutService(checkoutItemDao)
      val customerId = Random.nextString(5)

      val item1 = (ProductTypes.Classic.id, Random.nextInt())
      val item2 = (ProductTypes.Standout.id, Random.nextInt())
      val item3 = (ProductTypes.Premium.id, Random.nextInt())

      when(checkoutItemDao.productCountByCustomerId(m.anyString())) thenReturn Future(List())
      when(checkoutItemDao.productCountByCustomerId(m.eq(customerId))) thenReturn Future(List(
        item1,
        item2,
        item3
      ))

      whenReady(checkoutService.productCountByCustomerId(customerId)) {
        _ mustBe Map(item1, item2, item3)
      }

      whenReady(checkoutService.productCountByCustomerId(Random.nextString(5))) {
        _ mustBe Map()
      }

    }
  }
}
