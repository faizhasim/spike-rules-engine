package checkout.models

object ProductTypes {
  sealed abstract class ProductType(val id: String, val name: String)

  case object Classic extends ProductType(id = "Classic", name = "Classic Ad")
  case object Standout extends ProductType(id = "Standout", name = "Standout Ad")
  case object Premium extends ProductType(id = "Premium", name = "Premium Ad")

  val products: Seq[ProductType] = Seq(Classic, Standout, Premium)

  def productFromId(productId: String): Option[ProductType] = products.find(_.id == productId)

}
