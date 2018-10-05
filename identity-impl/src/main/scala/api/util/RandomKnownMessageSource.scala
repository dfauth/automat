package api.util

import java.util.concurrent.ArrayBlockingQueue

import akka.stream.{Attributes, Outlet, SourceShape}
import akka.stream.stage.{AbstractInOutHandler, GraphStage, GraphStageLogic}
import org.apache.logging.log4j.scala.Logging

import scala.concurrent.Future
import scala.util.Random

class RandomKnownMessageSource(prefix:String, delay:Int = 1000) extends GraphStage[SourceShape[String]] with Logging {

  val out: Outlet[String] = Outlet.create("RandomNumberSource.out")
  override def shape: SourceShape[String] = SourceShape.of(out)

  val queue = new ArrayBlockingQueue[String](100)

  import scala.concurrent.ExecutionContext.Implicits.global
  Future {
    while(true) {
      Thread.sleep((Random.nextDouble()*delay).toInt)
      val next = (Random.nextDouble()*1000).toInt
      queue.offer("{\"msgType\":\""+prefix+"\",\"payload\":\""+next+"\"}")
      logger.info("offered: "+next)
    }
  }

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(out, new AbstractInOutHandler {

        override def onPull(): Unit = {
          var next:String = null
          next = queue.take()
          if(next != null) {
            push(out, next)
            logger.info("pushed out: "+next)
          }
        }

        override def onPush(): Unit = {}
      })
    }
  }

}
