package checkout.services

import checkout.database.CheckoutItemDao
import checkout.models.{CheckoutItemDto, ProductTypes}
import org.mockito.invocation.InvocationOnMock
import org.mockito.{ArgumentMatchers => m}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckoutServiceSpec extends PlaySpec with MockitoSugar {
  import org.mockito.Mockito._
  trait MockedDependencies {
    val checkoutItemDao: CheckoutItemDao = mock[CheckoutItemDao]
    when(checkoutItemDao.add(m.anyString(), m.anyString())) thenAnswer ((invocation: InvocationOnMock) =>
      Future { CheckoutItemDto(Some(1L), invocation.getArgument(0), invocation.getArgument(1)) }
    )
  }

  "CheckoutService#addItem" should {
    "call the associated DAO with expected parameters" in new MockedDependencies {
      val checkoutService = new CheckoutService(checkoutItemDao)
      for (product <- ProductTypes.products) {
        checkoutService.addItem("customerId", product)
        verify(checkoutItemDao).add(m.eq("customerId"), m.eq(product.id))
      }
    }
  }
}
