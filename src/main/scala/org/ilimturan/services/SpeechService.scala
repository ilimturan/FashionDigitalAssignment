package org.ilimturan.services

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.typesafe.scalalogging.StrictLogging
import org.ilimturan.enums.SPEECH_PROCESS_STATUS
import org.ilimturan.models._
import org.ilimturan.repos.SpeechRepo
import org.ilimturan.validator.SpeechValidator
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

class SpeechService(speechRepo: SpeechRepo)(implicit ec: ExecutionContext, mat: Materializer) extends StrictLogging {

  def handleEvaluation(urls: Seq[String]): Future[SpeechResponse] = {

    logger.info(s"Received URLs: ${urls.mkString(", ")}")

    //TODO URL encode OR decode?
    val urlSet        = urls.distinct.map(str => str.trim)
    val validUrlSet   = urlSet.filter(url => SpeechValidator.isValidUrl(url))
    val invalidUrlSet = urlSet diff validUrlSet

    if (invalidUrlSet.nonEmpty) {
      Future.successful(
        SpeechResponse(
          errors = Seq(s"Your request contains invalid urls [${invalidUrlSet.mkString(",")}]")
        )
      )
    } else if (validUrlSet.isEmpty) {
      Future.successful(
        SpeechResponse(
          errors = Seq(s"Your request url's empty")
        )
      )
    } else {
      //TODO -1L is a trick for quill
      val speechRequest = SpeechRequest(-1L, urls = urlSet.mkString(","), insertTime = DateTime.now())

      speechRepo
        .addSpeechRequest(speechRequest)
        .flatMap { speechRequestRes =>
          val speechFileProcessSeqF = urlSet.map { url =>
            val speechFileProcess = SpeechFileProcess(
              id = -1L,
              requestId = speechRequestRes.id,
              url = url,
              status = SPEECH_PROCESS_STATUS.READY,
              failReason = None,
              insertTime = DateTime.now()
            )
            speechRepo.addSpeechFileProcess(speechFileProcess).map(_ => 1)
          }

          val speechFileProcessResultF = Future.sequence(speechFileProcessSeqF).map(res => res.sum)

          speechFileProcessResultF.map { count =>
            SpeechResponse(
              message = Some(s"Your request has been queued"),
              data = Some(
                SpeechResponseData(
                  urlCount = urlSet.size,
                  jobCount = count
                )
              )
            )
          }

        }
        .recover { case e: Exception =>
          logger.error(e.getMessage)

          SpeechResponse(
            errors = Seq(s"Your request can not queued: " + e.getMessage)
          )
        }
    }

  }

  def getLatestJob(): Future[Option[SpeechFileProcess]] = {
    speechRepo.getLatestJob()
  }

  def getLatestCompletedJob(): Future[Option[SpeechFileProcess]] = {
    speechRepo.getLatestCompletedJob()
  }

  def updateJob(speechFileProcess: SpeechFileProcess): Future[SpeechFileProcess] = {
    speechRepo.updateJob(speechFileProcess)
  }

  def addPoliticalSpeech(speech: Speech): Future[Boolean] = {
    speechRepo
      .addPoliticalSpeech(speech)
      .map(_ => true)
  }

  def addPoliticalSpeechFromSource(source: Source[PoliticParsed, NotUsed]): Future[Int] = {

    source
      //.throttle(1000, 1.seconds)
      //.buffer(500, OverflowStrategy.backpressure)
      .mapAsync(5) { row =>
        val partitionId = (1900 + row.dateOfSpeech.getYear) //TODO fix, not safe
        val speech      = Speech(-1, row.speaker, row.topic, row.wordCount, partitionId, row.dateOfSpeech)

        addPoliticalSpeech(speech)
          .map(_ => 1)

      }
      .runFold(0) { case (acc, count) =>
        if (acc > 0 && acc % 100 == 0) {
          logger.info(s"Processed row count [$acc]")
        }
        acc + count
      }
      .recover { case e: Exception =>
        logger.error("Error when data persist to DB", e)
        0
      }
  }

  def speechCounts(): Future[List[AggPoliticianSpeechCount]] = {

    speechRepo
      .speechCounts()
      .map { result =>
        result
          .map { row =>
            AggPoliticianSpeechCount(-1, row._1, row._2, row._3)
          }
          .filter(_.aggCount > 0)
          .sortBy(-_.aggCount)
      }
  }

  def speechTopics(): Future[List[AggPoliticianSpeechTopicCount]] = {
    //val currentDate      = new Date()
    //val currentTimestamp = new Timestamp(currentDate.getTime())

    speechRepo
      .speechTopics()
      .map { result =>
        result
          .map { row =>
            AggPoliticianSpeechTopicCount(-1L, row._1._1, row._1._2, row._2)
          }
          .filter(_.aggCount > 0)
          .sortBy(_.topicName)
      }
  }

  def speechWordCount(): Future[List[AggPoliticianSpeechWordCount]] = {
    speechRepo
      .speechWordCount()
      .map { result =>
        result
          .collect { row =>
            row._2 match {
              case Some(count) if count > 0 =>
                (row._1, count)
            }
          }
          .map { row =>
            AggPoliticianSpeechWordCount(-1L, row._1, row._2)
          }
          .filter(_.aggCount > 0)
      }
  }

  def getSpeechAggResult(
      mostYearMaybe: Option[String],
      mostTopicMaybe: Option[String]
  ): Future[SpeechAggResponse] = {

    val res1F = SpeechValidator.queryParamToYearFilter(mostYearMaybe) match {
      case Some(id) => speechRepo.getMostSpeechResultWithPartitionId(id)
      case None     => speechRepo.getMostSpeechResult()
    }
    val res2F = speechRepo.getMostSecurityResult(mostTopicMaybe.getOrElse("Innere Sicherheit"))
    val res3F = speechRepo.getLeastWordyResult()

    for {
      res1 <- res1F
      res2 <- res2F
      res3 <- res3F
    } yield {

      SpeechAggResponse(
        res1.map(_.politicianName),
        res2.map(_.politicianName),
        res3.map(_._1)
      )
    }

  }

  def addAggPoliticianSpeechCount(agg: AggPoliticianSpeechCount) = {
    speechRepo
      .addAggPoliticianSpeechCount(agg)
      .map(_ => true)
  }

  def addAggPoliticianSpeechTopicCount(agg: AggPoliticianSpeechTopicCount) = {
    speechRepo
      .addAggPoliticianSpeechTopicCount(agg)
      .map(_ => true)
  }

  def addAggPoliticianSpeechWordCount(agg: AggPoliticianSpeechWordCount) = {
    speechRepo
      .addAggPoliticianSpeechWordCount(agg)
      .map(_ => true)
  }

}
