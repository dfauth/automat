package automat

object IdentityHelper {
  def wrap[A,B](pf:PartialFunction[A,B]):java.util.function.Function[A,B] = {
    new java.util.function.Function[A,B](){
      override def apply(a: A): B = if(pf.isDefinedAt(a)) {
        pf.apply(a)
      } else {
        throw new IllegalArgumentException("Oops function not defined at "+a)
      }
    }
  }
  def extractIdentityFields(identity:Identity):PartialFunction[String, String] = {
    case "username" => identity.username()
    case "password" => identity.password()
  }
}
