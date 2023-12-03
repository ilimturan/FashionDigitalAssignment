package org.ilimturan

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import org.ilimturan.config.RestConfig
import org.ilimturan.routes.RoutesQuery

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object QuerySpeech extends App with StrictLogging with Components {

  logger.info("APP STARTING [QuerySpeech] ...")

  implicit val actorSystem = ActorSystem("QuerySpeech")
  implicit val mat         = Materializer(actorSystem)
  implicit val ec          = actorSystem.dispatcher

  try {
    val routes = new RoutesQuery(speechService)

    Http()
      .newServerAt(RestConfig.httpHost, RestConfig.httpPort)
      .bindFlow(routes.routes)
      .map { _ =>
        logger.info(s"APP STARTED [QuerySpeech] AT PORT:${RestConfig.httpPort} ...")
      }
      .recover { case e: Throwable =>
        logger.error("ERROR WHEN START APP [QuerySpeech]", e)
        throw e
      }
  } catch {
    case cause: Throwable =>
      logger.error("EXCEPTION WHEN START APP [QuerySpeech] ", cause)
      Await.result(actorSystem.terminate(), 10.seconds)
      System.exit(1)
  }

}
