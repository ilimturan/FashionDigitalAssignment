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

  def handleEvaluation(urls: Seq[String]): Future[Either[String, String]] = {

    logger.info(s"Received URLs: ${urls.mkString(", ")}")

    //TODO URL encode OR decode?
    val urlSet        = urls.distinct.map(str => str.trim)
    val validUrlSet   = urlSet.filter(url => SpeechValidator.isValidUrl(url))
    val invalidUrlSet = urlSet diff validUrlSet

    if (invalidUrlSet.nonEmpty) {
      Future.successful(
        Left(
          s"Your request contains invalid urls, valid [${validUrlSet.mkString(",")}], invalid [${invalidUrlSet.mkString(",")}]"
        )
      )
    } else if (validUrlSet.isEmpty) {
      Future.successful(
        Left(
          s"Your request url's empty"
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
            Right(s"Your request has been queued, url count [${urlSet.size}], job count [$count] ")
          }

        }
        .recover { case e: Exception =>
          logger.error(e.getMessage)
          Left(s"Your request can not queued: " + e.getMessage)
        }
    }

  }

  def getLatestJob(): Future[Option[SpeechFileProcess]] = {
    speechRepo.getLatestJob()
  }

  def updateJob(speechFileProcess: SpeechFileProcess): Future[SpeechFileProcess] = {
    speechRepo.updateJob(speechFileProcess)
  }

  def addPoliticalSpeech(politicalSpeech: PoliticalSpeech): Future[PoliticalSpeech] = {
    speechRepo.addPoliticalSpeech(politicalSpeech)
  }

  def addPoliticalSpeechFromSource(source: Source[PoliticParsed, NotUsed]): Future[Int] = {

    println("addPoliticalSpeechFromSource")
    source
      //.throttle(1000, 1.seconds)
      //.buffer(500, OverflowStrategy.backpressure)
      .mapAsync(5) { row =>
        val politicalSpeech = PoliticalSpeech(-1, row.speaker, row.topic, row.wordCount, row.dateOfSpeech)

        addPoliticalSpeech(politicalSpeech)
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

}
