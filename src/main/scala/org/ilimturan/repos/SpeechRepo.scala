package org.ilimturan.repos

import io.getquill.{CamelCase, PostgresAsyncContext}
import org.ilimturan.enums.SPEECH_PROCESS_STATUS
import org.ilimturan.implicits.QuillImplicit
import org.ilimturan.models._

import scala.concurrent.{ExecutionContext, Future}

class SpeechRepo(implicit val dbCtx: PostgresAsyncContext[CamelCase.type], ec: ExecutionContext) extends QuillImplicit {

  //TODO remove
  println("ec: " + ec.toString)

  import dbCtx._

  val specRequestTable       = quote(querySchema[SpeechRequest]("speech_request"))
  val speechFileProcessTable = quote(querySchema[SpeechFileProcess]("speech_file_process"))
  val politicalSpeechTable   = quote(querySchema[PoliticalSpeech]("political_speech"))

  implicit val specRequestMeta       = insertMeta[SpeechRequest](_.id)     // to exclude id on insert
  implicit val speechFileProcessMeta = insertMeta[SpeechFileProcess](_.id) // to exclude id on insert
  implicit val politicalSpeechMeta   = insertMeta[PoliticalSpeech](_.id)   // to exclude id on insert

  private def insertSpeechRequest(speechRequest: SpeechRequest) = quote {
    specRequestTable.insert(lift(speechRequest)).returning(speechRequest => speechRequest)
  }

  private def insertSpeechFileProcess(speechFileProcess: SpeechFileProcess) = quote {
    speechFileProcessTable.insert(lift(speechFileProcess)).returning(speechFileProcess => speechFileProcess)
  }

  private def insertPoliticalSpeech(politicalSpeech: PoliticalSpeech) = quote {
    politicalSpeechTable.insert(lift(politicalSpeech)).returning(politicalSpeech => politicalSpeech)
  }

  def updateJob(speechFileProcess: SpeechFileProcess) = {
    val query = quote {
      speechFileProcessTable
        .filter(_.id == lift(speechFileProcess.id))
        .update(lift(speechFileProcess))
        .returning(row => row)
    }

    dbCtx
      .run(query)
  }

  def addSpeechRequest(speechRequest: SpeechRequest) = {
    dbCtx
      .run(insertSpeechRequest(speechRequest))
  }

  def addSpeechFileProcess(speechFileProcess: SpeechFileProcess) = {
    dbCtx
      .run(insertSpeechFileProcess(speechFileProcess))
  }

  def getLatestJob(): Future[Option[SpeechFileProcess]] = {

    val query = quote {
      speechFileProcessTable
        .filter(_.status == lift(SPEECH_PROCESS_STATUS.READY))
        .sortBy(_.id)
        .take(1)
    }

    dbCtx
      .run(query)
      .map(_.headOption)
  }

  def addPoliticalSpeech(politicalSpeech: PoliticalSpeech) = {
    dbCtx
      .run(insertPoliticalSpeech(politicalSpeech))
  }

}

object SpeechRepo {
  def apply()(implicit dbCtx: PostgresAsyncContext[CamelCase.type], ec: ExecutionContext): SpeechRepo = {
    new SpeechRepo()(dbCtx, ec)
  }
}
