package org.ilimturan

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import org.ilimturan.config.ProducerConfig
import org.ilimturan.routes.RoutesProducer

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object ProducerDownload extends App with StrictLogging with Components {

  logger.info("APP STARTING [ProducerDownload] ...")

  implicit val actorSystem = ActorSystem("ProducerDownload")
  implicit val mat         = Materializer(actorSystem)
  implicit val ec          = actorSystem.dispatcher

  try {
    val routes = new RoutesProducer(speechService)

    Http()
      .newServerAt(ProducerConfig.httpHost, ProducerConfig.httpPort)
      .bindFlow(routes.routes)
      .map { _ =>
        logger.info(s"APP STARTED [ProducerDownload] AT PORT:${ProducerConfig.httpPort} ...")
      }
      .recover { case e: Throwable =>
        logger.error("ERROR WHEN START APP [ProducerDownload]", e)
        throw e
      }
  } catch {
    case cause: Throwable =>
      logger.error("EXCEPTION WHEN START APP [ProducerDownload] ", cause)
      Await.result(actorSystem.terminate(), 10.seconds)
      System.exit(1)
  }

}
