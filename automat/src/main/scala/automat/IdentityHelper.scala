package automat

object IdentityHelper {
  def extractIdentityFields(identity:Identity):PartialFunction[String, String] = {
    case "username" => identity.username()
    case "password" => identity.password()
  }
}
