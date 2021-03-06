package api

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.Source
import api.authentication.AuthenticationServiceComposition._
import api.authentication.TokenContent
import api.request.WithUserCreationFields
import api.response.{TokenRefreshDone, UserLoginDone}
import api.util.RandomKnownMessageSource
import api.validation.ValidationUtil._
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.{BadRequest, Forbidden}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import io.digitalcat.publictransportation.services.identity.impl.util.{JwtTokenUtil, SecurePasswordHashing}
import org.apache.logging.log4j.scala.Logging

import scala.concurrent.{ExecutionContext, Future}

class IdentityServiceImpl(
  persistentRegistry: PersistentEntityRegistry,
  identityRepository: IdentityRepository
)(implicit ec: ExecutionContext) extends IdentityService with Logging {
  override def registerClient() = ServiceCall { request =>
    validate(request)

    def executeCommandCallback = () => {
      val ref = persistentRegistry.refFor[IdentityEntity](UUID.randomUUID().toString)

      ref.ask(
        RegisterClient(
          company = request.company,
          firstName = request.firstName,
          lastName = request.lastName,
          email = request.email,
          username = request.username,
          password = request.password
        )
      )
    }

    reserveUsernameAndEmail(request, executeCommandCallback)
  }

  override def getIdentityState() = authenticated { (tokenContent, _) =>
    ServerServiceCall { _ =>
      val ref = persistentRegistry.refFor[IdentityEntity](tokenContent.clientId.toString)

      ref.ask(GetIdentityState())
    }
  }

  override def loginUser() = ServiceCall { request =>
    def passwordMatches(providedPassword: String, storedHashedPassword: String) = SecurePasswordHashing.validatePassword(providedPassword, storedHashedPassword)

    validate(request)

    for {
      maybeUser <- identityRepository.findUserByUsername(request.username)

      token = maybeUser.filter(user => passwordMatches(request.password, user.hashedPassword))
        .map(user =>
          TokenContent(
            clientId = user.clientId,
            userId = user.id,
            username = user.username
          )
        )
        .map(tokenContent => JwtTokenUtil.generateTokens(tokenContent))
        .getOrElse(throw Forbidden("Username and password combination not found"))
    }
    yield {
      UserLoginDone(token.authToken, token.refreshToken.getOrElse(throw new IllegalStateException("Refresh token missing")))
    }
  }

  override def refreshToken() = authenticatedWithRefreshToken { tokenContent =>
    ServerServiceCall { _ =>
      val token = JwtTokenUtil.generateAuthTokenOnly(tokenContent)

      Future.successful(TokenRefreshDone(token.authToken))
    }
  }

  override def createUser() = authenticated { (tokenContent, _) =>
    ServerServiceCall { request =>
      validate(request)

      def executeCommandCallback = () => {
        val ref = persistentRegistry.refFor[IdentityEntity](tokenContent.clientId.toString)

        ref.ask(
          CreateUser(
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            username = request.username,
            password = request.password
          )
        )
      }

      reserveUsernameAndEmail(request, executeCommandCallback)
    }
  }

  def heartbeat(flow: Source[String, NotUsed]):Source[String, NotUsed] = {
    flow.map(s => {
      val mapper = new ObjectMapper
      mapper.readValue(s, classOf[JsonNode])
    }).mapAsync(1) {
      case n:JsonNode if(n.get("msgType").asText().equals("heartbeat")) => {
        logger.info("received ping")
        Future{
          val reply = "{\"msgType\":\"heartbeat\",\"payload\":\"pong\"}"
          logger.info("replied: "+reply)
          reply
        }
      }
      case n:JsonNode if(n.get("msgType").asText().equals("echo")) => {
        val payload = n.get("payload").asText()
        logger.info(s"received echo with payload: ${payload}")
        Future{
          val reply = "{\"msgType\":\"echo\",\"payload\":\"echo of '"+payload+"'\"}"
          logger.info("replied: "+reply)
          reply
        }
      }
      case n:JsonNode => {
        Future("{\"msgType\":\"error\",\"payload\":\"received jsonNode of type "+n+"\"}")
      }
      case _ => {
        logger.info("received something else")
        Future("{\"msgType\":\"error\",\"payload\":\"MatchError\"}")
      }
    }
  }

  override def stream() = authenticated { (tokenContent, _) =>
    ServerServiceCall { flow => Future(
      heartbeat(flow).async.merge(randomStream.source.async)
    )}
  }

  private def reserveUsernameAndEmail[B](request: WithUserCreationFields, onSuccess: () => Future[B]): Future[B] = {
    def rollbackReservations(request: WithUserCreationFields,usernameReserved: Boolean, emailReserved: Boolean) = {
      if (usernameReserved) {
        identityRepository.unreserveUsername(request.username)
      }
      if (emailReserved) {
        identityRepository.unreserveEmail(request.email)
      }
    }

    val canProceed = for {
      usernameReserved <- identityRepository.reserveUsername(request.username)
      emailReserved <- identityRepository.reserveEmail(request.email)
    }
    yield (usernameReserved, emailReserved)

    canProceed.flatMap(canProceed => {
      (canProceed._1 && canProceed._2) match {
        case true => onSuccess.apply()
        case false => {
          rollbackReservations(request, canProceed._1, canProceed._2)
          throw BadRequest("Either username or email is already taken.")
        }
      }
    })
  }
}

object randomStream {
  val source: Source[String, NotUsed] = Source.fromGraph(new RandomKnownMessageSource("known", 10000))
}