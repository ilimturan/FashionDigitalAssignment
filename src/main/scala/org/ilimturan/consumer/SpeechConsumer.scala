package org.ilimturan.consumer

import akka.NotUsed
import akka.actor.Cancellable
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import com.typesafe.scalalogging.StrictLogging
import org.ilimturan.enums.SPEECH_PROCESS_STATUS
import org.ilimturan.models.{PoliticParsed, PoliticalSpeech}
import org.ilimturan.parser.SpeechCsvParser
import org.ilimturan.services.{DownloadService, SpeechService}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class SpeechConsumer(speechService: SpeechService, speechCsvParser: SpeechCsvParser, downloadService: DownloadService)(
    implicit
    ec: ExecutionContext,
    mat: Materializer
) extends StrictLogging {

  def init() = {
    tickSource
      .to(Sink.ignore)
      .run()
    true
  }

  //This is simulate consumer, and consume DB
  private val tickSource: Source[Int, Cancellable] = Source
    .tick(1.seconds, 5.seconds, ())
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
                  val sourceResultF        = persistPoliticParsedSource(source)

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
            logger.info("There is no job, waiting")
            Future.successful(0)
        }

    }

  private def persistPoliticParsedSource(source: Source[PoliticParsed, NotUsed]): Future[Int] = {
    source
      .throttle(1000, 1.seconds)
      .buffer(500, OverflowStrategy.backpressure)
      .mapAsync(5) { row =>
        val politicalSpeech = PoliticalSpeech(-1, row.speaker, row.topic, row.wordCount, row.dateOfSpeech)

        speechService
          .addPoliticalSpeech(politicalSpeech)
          .map(_ => 1)

      }
      .runFold(0) { case (acc, x) =>
        if (acc > 0 && acc % 100 == 0) {
          logger.info(s"Processed row count [$acc]")
        }
        acc + x
      }
  }

}
