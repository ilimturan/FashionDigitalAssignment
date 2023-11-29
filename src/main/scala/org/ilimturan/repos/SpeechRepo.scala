package org.ilimturan.repos

import io.getquill.{CamelCase, PostgresAsyncContext}
import org.ilimturan.implicits.QuillImplicit
import org.ilimturan.models._

import scala.concurrent.ExecutionContext

class SpeechRepo(implicit val dbCtx: PostgresAsyncContext[CamelCase.type], ec: ExecutionContext) extends QuillImplicit {

  //TODO remove
  println("ec: " + ec.toString)

  import dbCtx._

  val tableSpecRequest       = quote(querySchema[SpeechRequest]("speech_request"))
  val tableSpeechFileProcess = quote(querySchema[SpeechFileProcess]("speech_file_process"))
  val tablePolitician        = quote(querySchema[Politician]("politician"))
  val tableTopic             = quote(querySchema[Topic]("topic"))
  val tablePoliticalSpeech   = quote(querySchema[PoliticalSpeech]("political_speech"))

  implicit val speechRequestMeta     = insertMeta[SpeechRequest](_.id)     // to exclude id on insert
  implicit val speechFileProcessMeta = insertMeta[SpeechFileProcess](_.id) // to exclude id on insert

  private def insertSpeechRequest(speechRequest: SpeechRequest) = quote {
    tableSpecRequest.insert(lift(speechRequest)).returning(speechRequest => speechRequest)
  }

  private def insertSpeechFileProcess(speechFileProcess: SpeechFileProcess) = quote {
    tableSpeechFileProcess.insert(lift(speechFileProcess)).returning(speechFileProcess => speechFileProcess)
  }

  def addSpeechRequest(speechRequest: SpeechRequest) = {
    dbCtx
      .run(insertSpeechRequest(speechRequest))
  }

  def addSpeechFileProcess(speechFileProcess: SpeechFileProcess) = {
    dbCtx
      .run(insertSpeechFileProcess(speechFileProcess))
  }

}

object SpeechRepo {
  def apply()(implicit dbCtx: PostgresAsyncContext[CamelCase.type], ec: ExecutionContext): SpeechRepo = {
    new SpeechRepo()(dbCtx, ec)
  }
}
