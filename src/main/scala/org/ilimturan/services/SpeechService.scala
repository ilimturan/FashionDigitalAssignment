package org.ilimturan.services

import com.typesafe.scalalogging.StrictLogging
import org.ilimturan.enums.SPEECH_PROCESS_STATUS
import org.ilimturan.models.{SpeechFileProcess, SpeechRequest}
import org.ilimturan.repos.SpeechRepo
import org.ilimturan.validator.SpeechValidator
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

class SpeechService(speechRepo: SpeechRepo)(implicit ec: ExecutionContext) extends StrictLogging {

  def handleEvaluation(urls: Seq[String]): Future[Either[String, String]] = {

    logger.info(s"Received URLs: ${urls.mkString(", ")}")

    //TODO URL encode OR decode?
    val urlSet        = urls.distinct.map(str => str.trim)
    val validUrlSet   = urlSet.filter(url => SpeechValidator.isValidUrl(url))
    val invalidUrlSet = urlSet diff validUrlSet

    if (invalidUrlSet.nonEmpty || validUrlSet.isEmpty) {
      Future.successful(
        Left(
          s"Your request contains invalid urls, valid [${validUrlSet.mkString(",")}]  invalid [${invalidUrlSet.mkString(",")}] "
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
          Left(s"Your request can not queued")
        }
    }

  }

}
