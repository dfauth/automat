package api

import java.sql.Timestamp

import api.date.DateUtcUtil

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class InMemoryIdentityRepository()(implicit ec: ExecutionContext) {

  val usersByUsername = mutable.Map[String,UserByUsername]()
  val reservedUsernames = mutable.Map[String,(String, Timestamp)]()
  val reservedEmails = mutable.Map[String,(String, Timestamp)]()

  def findUserByUsername(username: String): Future[Option[UserByUsername]] = {
    Future(
      usersByUsername.get(username)
    )
  }

  def reserveUsername(username: String): Future[Boolean] = {
    val createdOn = new Timestamp(DateUtcUtil.now().getMillis)
    Future(
      reservedUsernames.put(username, (username, createdOn)).isDefined
    )
  }

  def unreserveUsername(username: String) = {
    Future(
      reservedUsernames.remove(username).isDefined
    )
  }

  def reserveEmail(email: String): Future[Boolean] = {
    val createdOn = new Timestamp(DateUtcUtil.now().getMillis)

    Future(
      reservedEmails.put(email, (email, createdOn)).isDefined
    )
  }

  def unreserveEmail(email: String) = {
    Future(
      reservedEmails.remove(email).isDefined
    )
  }
}


