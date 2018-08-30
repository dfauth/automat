package thingy

import automat.Method
import io.restassured.RestAssured
import io.restassured.filter.{Filter, FilterContext}
import io.restassured.response.{Response, ResponseOptions}
import io.restassured.specification._
import org.apache.logging.log4j.scala.Logging

import scala.collection.mutable

object Given {

  val g = RestAssured.given()

  def when = g.when


    val handlers = mutable.Map[Int, RequestSpecification => ResponseOptions[Response]]()

  def handleStatusCode(statusCode: Int, f: RequestSpecification => ResponseOptions[Response]) = {
    handlers.put(statusCode, f)
    this
  }

  def given() = this

}

object RequestContext {

  val map = mutable.Map[String, String]()

  def addToContext(key:String, value:String) = {
    map.put(key, value)
  }

  def get(key:String):Option[String] = {
    map.get(key)
  }
}

class ThingyFilter(preHandler: FilterableRequestSpecification => FilterableRequestSpecification = r => r, postHandlers: Map[Int, RequestSpecification => Response] = Map.empty) extends Filter with Logging {

  def preHandle(requestSpec: FilterableRequestSpecification): FilterableRequestSpecification = {
    preHandler(requestSpec)
  }

  def postHandle(requestSpec: FilterableRequestSpecification, res: Response): Response = {
    val q = SpecificationQuerier.query(requestSpec)
    val uri = q.getURI
    val replayer = Method.valueOf(q.getMethod).replayer(q.getURI())
    logger.info("response: "+res.statusCode)
    postHandlers.get(res.statusCode()).map(f => {
      f.andThen[Response](r => {
        logger.info("replay original request: "+requestSpec)
        replayer.replay(requestSpec)
      })(requestSpec)
    }).getOrElse[Response](res)
  }

  override def filter(requestSpec: FilterableRequestSpecification, responseSpec: FilterableResponseSpecification, ctx: FilterContext): Response = {
    val req = preHandle(requestSpec)
    val res = ctx.next(req, responseSpec)
    postHandle(req, res)
  }
}


