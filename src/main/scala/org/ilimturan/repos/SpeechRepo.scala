package org.ilimturan.repos

import io.getquill.{CamelCase, Ord, PostgresAsyncContext}
import org.ilimturan.enums.SPEECH_PROCESS_STATUS
import org.ilimturan.implicits.{QuillImplicit, QuillOperators}
import org.ilimturan.models._

import scala.concurrent.{ExecutionContext, Future}

class SpeechRepo(implicit val dbCtx: PostgresAsyncContext[CamelCase.type], ec: ExecutionContext)
    extends QuillImplicit
    with QuillOperators {

  import dbCtx._

  val specRequestTable       = quote(querySchema[SpeechRequest]("speech_request"))
  val speechFileProcessTable = quote(querySchema[SpeechFileProcess]("speech_file_process"))
  val speechTable            = quote(querySchema[Speech]("speech"))

  val aggPoliticianSpeechCountTable      = quote(querySchema[AggPoliticianSpeechCount]("agg_politician_speech_count"))
  val aggPoliticianSpeechTopicCountTable = quote(
    querySchema[AggPoliticianSpeechTopicCount]("agg_politician_speech_topic_count")
  )
  val aggPoliticianSpeechWordCountTable  = quote(
    querySchema[AggPoliticianSpeechWordCount]("agg_politician_word_count")
  )

  implicit val specRequestMeta       = insertMeta[SpeechRequest](_.id)     // to exclude id on insert
  implicit val speechFileProcessMeta = insertMeta[SpeechFileProcess](_.id) // to exclude id on insert
  implicit val speechMeta            = insertMeta[Speech](_.id)            // to exclude id on insert

  implicit val aggPoliticianSpeechCountMeta      = insertMeta[AggPoliticianSpeechCount](_.id) // to exclude id on insert
  implicit val aggPoliticianSpeechTopicCountMeta =
    insertMeta[AggPoliticianSpeechTopicCount](_.id) // to exclude id on insert
  implicit val aggPoliticianSpeechWordCountMeta =
    insertMeta[AggPoliticianSpeechWordCount](_.id) // to exclude id on insert

  private def insertSpeechRequest(speechRequest: SpeechRequest) = quote {
    specRequestTable.insert(lift(speechRequest)).returning(speechRequest => speechRequest)
  }

  private def insertSpeechFileProcess(speechFileProcess: SpeechFileProcess) = quote {
    speechFileProcessTable.insert(lift(speechFileProcess)).returning(speechFileProcess => speechFileProcess)
  }

  private def insertSpeech(speech: Speech) = quote {
    speechTable.insert(lift(speech)).returning(speech => speech)
  }

  private def insertAggPoliticianSpeechCount(agg: AggPoliticianSpeechCount)           = quote {
    aggPoliticianSpeechCountTable.insert(lift(agg)).returning(agg => agg)
  }
  private def insertAggPoliticianSpeechTopicCount(agg: AggPoliticianSpeechTopicCount) = quote {
    aggPoliticianSpeechTopicCountTable.insert(lift(agg)).returning(agg => agg)
  }
  private def insertAggPoliticianSpeechWordCount(agg: AggPoliticianSpeechWordCount)   = quote {
    aggPoliticianSpeechWordCountTable.insert(lift(agg)).returning(agg => agg)
  }

  def addAggPoliticianSpeechCount(agg: AggPoliticianSpeechCount) = {
    dbCtx
      .run(insertAggPoliticianSpeechCount(agg))
  }
  def addAggPoliticianSpeechTopicCount(agg: AggPoliticianSpeechTopicCount) = {
    dbCtx
      .run(insertAggPoliticianSpeechTopicCount(agg))
  }
  def addAggPoliticianSpeechWordCount(agg: AggPoliticianSpeechWordCount) = {
    dbCtx
      .run(insertAggPoliticianSpeechWordCount(agg))
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

  def addSpeechRequest(speechRequest: SpeechRequest): Future[SpeechRequest] = {
    dbCtx
      .run(insertSpeechRequest(speechRequest))
  }

  def addSpeechFileProcess(speechFileProcess: SpeechFileProcess): Future[SpeechFileProcess] = {
    dbCtx
      .run(insertSpeechFileProcess(speechFileProcess))
  }

  def getLatestJob(): Future[Option[SpeechFileProcess]] = {

    val query = quote {
      speechFileProcessTable
        .filter(_.status == lift(SPEECH_PROCESS_STATUS.READY))
        .sortBy(-_.id)
        .take(1)
    }

    dbCtx
      .run(query)
      .map(_.headOption)
  }

  def getLatestCompletedJob(): Future[Option[SpeechFileProcess]] = {

    val query = quote {
      speechFileProcessTable
        .filter(_.status == lift(SPEECH_PROCESS_STATUS.COMPLETED))
        .sortBy(-_.id)
        .take(1)
    }

    dbCtx
      .run(query)
      .map(_.headOption)
  }

  def addPoliticalSpeech(speech: Speech) = {
    dbCtx
      .run(insertSpeech(speech))
  }

  def speechCounts() = {

    //val yearFunc: Date => Int = dt => dt.getYear

    dbCtx.run(
      quote(
        speechTable
          //.filter(speech => infix"${speech.speechDate} <= ${lift(endSqlDate)}".as[Boolean])
          //.filter(speech => infix"${speech.speechDate} <= ${lift(endSqlDate)}".as[Boolean])
          //.groupBy(speech => (speech.politicianName, infix"${yearFunc(speech.speechDate})".as[Int])
          .groupBy(speech => (speech.politicianName, speech.partitionId))
          //.groupBy(speech => (speech.politicianName, datePart("year", speech.speechDate)))
          .map { case ((politicianName, speechYear), speeches) =>
            (politicianName, speechYear, speeches.size)
          }
      )
    )

  }

  def speechTopics() = {
    dbCtx.run(
      quote(
        speechTable
          .groupBy(speech => (speech.politicianName, speech.topicName))
          .map { case (politicianAndTopicName, speeches) =>
            (politicianAndTopicName, speeches.size)
          }
          .sortBy(-_._2)
      )
    )
  }

  def speechWordCount() = {

    dbCtx.run(
      quote(
        speechTable
          //.filter(speech => infix"${speech.speechDate} >= ${lift(startDate)}".as[Boolean])
          //.filter(speech => infix"${speech.speechDate} <= ${lift(endDate)}".as[Boolean])
          .groupBy(speech => speech.politicianName)
          .map { case (politicianName, speeches) =>
            (politicianName, speeches.map(_.wordCount).sum)
          }
          .sortBy(_._2)
      )
    )
  }

  def getMostSpeechResult() = {

    dbCtx
      .run(
        quote(
          aggPoliticianSpeechCountTable
            .sortBy(_.aggCount)(Ord.desc)
            .take(1)
        )
      )
      .map(_.headOption)
  }

  def getMostSpeechResultWithPartitionId(partitionId: Int) = {

    dbCtx
      .run(
        quote(
          aggPoliticianSpeechCountTable
            .filter(row => row.partitionId == lift(partitionId))
            .sortBy(_.aggCount)(Ord.desc)
            .take(1)
        )
      )
      .map(_.headOption)
  }

  def getMostSecurityResult(topicName: String) = {
    dbCtx
      .run(
        quote(
          aggPoliticianSpeechTopicCountTable
            .filter(row => row.topicName == lift(topicName))
            .sortBy(_.aggCount)(Ord.desc)
            .take(1)
        )
      )
      .map(_.headOption)
  }

  def getLeastWordyResult() = {

    dbCtx
      .run(
        quote(
          aggPoliticianSpeechWordCountTable
            .groupBy(speech => speech.politicianName)
            .map { case (politicianName, group) =>
              (politicianName, group.map(_.aggCount).max)
            }
            .sortBy(_._2)(Ord.asc)
            .take(1)
        )
      )
      .map(_.headOption)

  }

}

object SpeechRepo {
  def apply()(implicit dbCtx: PostgresAsyncContext[CamelCase.type], ec: ExecutionContext): SpeechRepo = {
    new SpeechRepo()(dbCtx, ec)
  }
}
