package org.ilimturan

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import org.ilimturan.config.AggregatorConfig
import org.ilimturan.routes.RoutesAggregator
import org.ilimturan.services.AggregatorService

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object AggregatorSpeech extends App with StrictLogging with Components {

  logger.info("APP STARTING [AggregatorSpeech] ...")

  implicit val actorSystem = ActorSystem("AggregatorSpeech")
  implicit val mat         = Materializer(actorSystem)
  implicit val ec          = actorSystem.dispatcher

  try {
    val routes = new RoutesAggregator()

    Http()
      .newServerAt(AggregatorConfig.httpHost, AggregatorConfig.httpPort)
      .bindFlow(routes.routes)
      .map { _ =>
        val aggregator = new AggregatorService(speechService)
        aggregator.init()

        logger.info(s"APP STARTED [AggregatorSpeech] AT PORT:${AggregatorConfig.httpPort} ...")
      }
      .recover { case e: Throwable =>
        logger.error("ERROR WHEN START APP [AggregatorSpeech]", e)
        throw e
      }
  } catch {
    case cause: Throwable =>
      logger.error("EXCEPTION WHEN START APP [AggregatorSpeech] ", cause)
      Await.result(actorSystem.terminate(), 10.seconds)
      System.exit(1)
  }

}
