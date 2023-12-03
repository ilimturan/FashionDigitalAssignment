package org.ilimturan.consumer

import akka.actor.Cancellable
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.scalalogging.StrictLogging
import org.ilimturan.enums.SPEECH_PROCESS_STATUS
import org.ilimturan.parser.SpeechCsvParser
import org.ilimturan.services.{DownloadService, SpeechService}

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}

class SpeechConsumer(speechService: SpeechService, speechCsvParser: SpeechCsvParser, downloadService: DownloadService)(
    implicit
    ec: ExecutionContext,
    mat: Materializer
) extends StrictLogging {

  def init() = {
    val initialDelayDuration = 1.seconds
    val intervalDuration     = 1.minutes
    getTickSource(initialDelayDuration, intervalDuration)
      .runWith(Sink.ignore)
    true
  }

  //This is simulate consumer, and consume DB
  def getTickSource(initialDelayDuration: FiniteDuration, intervalDuration: FiniteDuration): Source[Int, Cancellable] =
    Source
      .tick(initialDelayDuration, intervalDuration, ())
      //TODO overflow strategy
      .mapAsync(1) { _ =>
        //Get one row from DB
        speechService
          .getLatestJob()
          .flatMap {
            case Some(job) =>
              downloadService
                .download(job.url)
                .flatMap {
                  case Left(msg) =>
                    //Can not download file
                    speechService
                      .updateJob(job.copy(status = SPEECH_PROCESS_STATUS.FAILED, failReason = Some(msg)))
                      .map(_ => 0)

                  case Right(data) => //TOOD check content type
                    //file downloaded, lets parse it
                    val updateJobDownloadedF =
                      speechService.updateJob(job.copy(status = SPEECH_PROCESS_STATUS.DOWNLOADED))
                    val source               = speechCsvParser.toAkkaSource(data.inputStream)
                    val updateJobParsingF    = speechService.updateJob(job.copy(status = SPEECH_PROCESS_STATUS.PARSING))
                    val sourceResultF        = speechService.addPoliticalSpeechFromSource(source)

                    val finalResultF = for {
                      _            <- updateJobDownloadedF
                      sourceResult <- sourceResultF
                      _            <- updateJobParsingF
                    } yield sourceResult

                    finalResultF
                      .flatMap { rowCount =>
                        speechService
                          .updateJob(job.copy(status = SPEECH_PROCESS_STATUS.COMPLETED))
                          .map(_ => rowCount)
                      }
                      .recoverWith { case e: Exception =>
                        speechService
                          .updateJob(job.copy(status = SPEECH_PROCESS_STATUS.FAILED, failReason = Some(e.getMessage)))
                          .map(_ => 0)
                      }

                }
            case None      =>
              logger.info("There is no job for consuming, waiting")
              Future.successful(0)
          }

      }

}
