package org.ilimturan

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import io.getquill.{CamelCase, PostgresAsyncContext}
import org.ilimturan.config.PostgresConfig
import org.ilimturan.implicits.QuillOperators
import org.ilimturan.repos.SpeechRepo
import org.ilimturan.services.{DownloadService, SpeechService}

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

trait Components extends StrictLogging {

  implicit val actorSystem: ActorSystem
  implicit val mat: Materializer
  implicit val ec: ExecutionContext

  val dbEc = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(5))

  implicit lazy val postgresCtx: PostgresAsyncContext[CamelCase.type] =
    new PostgresAsyncContext(CamelCase, PostgresConfig.dbPostgresConfig) with QuillOperators

  lazy val speechRepo      = new SpeechRepo()(postgresCtx, dbEc)
  lazy val speechService   = new SpeechService(speechRepo)
  lazy val downloadService = new DownloadService()
}
