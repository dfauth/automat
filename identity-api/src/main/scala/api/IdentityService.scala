package api

import akka.NotUsed
import akka.stream.scaladsl.Source
import api.request.{ClientRegistration, UserCreation, UserLogin}
import api.response.{GeneratedIdDone, IdentityStateDone, TokenRefreshDone, UserLoginDone}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

trait IdentityService extends Service {
  def registerClient(): ServiceCall[ClientRegistration, GeneratedIdDone]
  def loginUser(): ServiceCall[UserLogin, UserLoginDone]
  def refreshToken(): ServiceCall[NotUsed, TokenRefreshDone]
  def getIdentityState(): ServiceCall[NotUsed, IdentityStateDone]
  def createUser(): ServiceCall[UserCreation, GeneratedIdDone]
  def stream: ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  override final def descriptor = {
    import Service._
    // @formatter:off
    named("identity-service").withCalls(
      restCall(Method.POST, "/api/client/registration", registerClient _),
      restCall(Method.POST, "/api/user/login", loginUser _),
      restCall(Method.PUT, "/api/user/token", refreshToken _),
      restCall(Method.GET, "/api/state/identity", getIdentityState _),
      restCall(Method.POST, "/api/user", createUser _),
      restCall(Method.GET, "/api/stream", stream _)
    ).withAutoAcl(true)
    // @formatter:on
  }


}
