package thingy

import java.{lang, util}
import java.util.concurrent.TimeUnit

import automat.Method
import io.restassured.RestAssured
import io.restassured.filter.{Filter, FilterContext}
import io.restassured.function.RestAssuredFunction
import io.restassured.http.ContentType
import io.restassured.matcher.DetailedCookieMatcher
import io.restassured.parsing.Parser
import io.restassured.response.{Response, ResponseOptions}
import io.restassured.specification._
import org.apache.logging.log4j.scala.Logging
import org.hamcrest.Matcher

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
}

class ThingyFilter(handlers: Map[Int, RequestSpecification => Response]) extends Filter with Logging {

  def handle(requestSpec: FilterableRequestSpecification, responseSpec: FilterableResponseSpecification, ctx: FilterContext, res: Response): Response = {
    val q = SpecificationQuerier.query(requestSpec)
    val uri = q.getURI
    val replayer = Method.valueOf(q.getMethod).replayer(q.getURI())
    logger.info("response: "+res.statusCode)
    handlers.get(res.statusCode()).map(f => {
      f.andThen[Response](r => {
        logger.info("replay original request: "+requestSpec)
        replayer.replay(requestSpec)
      })(requestSpec)
    }).getOrElse[Response](res)
  }

  override def filter(requestSpec: FilterableRequestSpecification, responseSpec: FilterableResponseSpecification, ctx: FilterContext): Response = {
    val res = ctx.next(requestSpec, responseSpec)
    handle(requestSpec, responseSpec, ctx, res)
  }
}


