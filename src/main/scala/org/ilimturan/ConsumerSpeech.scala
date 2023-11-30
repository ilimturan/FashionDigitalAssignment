package org.ilimturan

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import org.ilimturan.config.ConsumerConfig
import org.ilimturan.consumer.SpeechConsumer
import org.ilimturan.parser.SpeechCsvParser
import org.ilimturan.routes.RoutesConsumer

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object ConsumerSpeech extends App with StrictLogging with Components {

  logger.info("APP STARTING [ConsumerSpeech] ...")

  implicit val actorSystem = ActorSystem("ConsumerSpeech")
  implicit val mat         = Materializer(actorSystem)
  implicit val ec          = actorSystem.dispatcher

  try {
    val routes = new RoutesConsumer()

    Http()
      .newServerAt(ConsumerConfig.httpHost, ConsumerConfig.httpPort)
      .bindFlow(routes.routes)
      .map { _ =>

        val parser   = new SpeechCsvParser()
        val consumer = new SpeechConsumer(speechService, parser, downloadService)
        consumer.init()

        logger.info(s"APP STARTED [ConsumerSpeech] AT PORT:${ConsumerConfig.httpPort} ...")
      }
      .recover { case e: Throwable =>
        logger.error("ERROR WHEN START APP [ConsumerSpeech]", e)
        throw e
      }
  } catch {
    case cause: Throwable =>
      logger.error("EXCEPTION WHEN START APP [ConsumerSpeech] ", cause)
      Await.result(actorSystem.terminate(), 10.seconds)
      System.exit(1)
  }

}
