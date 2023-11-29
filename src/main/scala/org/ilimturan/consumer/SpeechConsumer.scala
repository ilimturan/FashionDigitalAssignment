package org.ilimturan.consumer

import akka.NotUsed
import akka.actor.Cancellable
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{Sink, Source, StreamConverters}
import akka.stream.{ActorAttributes, Materializer, OverflowStrategy, Supervision}
import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.io.input.BOMInputStream
import org.ilimturan.enums.SPEECH_PROCESS_STATUS
import org.ilimturan.models.{PoliticParsed, PoliticalSpeech}
import org.ilimturan.services.{DownloadService, SpeechService}

import java.text.SimpleDateFormat
import java.util.Date
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class SpeechConsumer(speechService: SpeechService, downloadService: DownloadService)(implicit
    ec: ExecutionContext,
    mat: Materializer
) extends StrictLogging {

  private val delimiter  = ','.toByte
  private val escapeChar = '\"'.toByte
  private val dateFormat = new SimpleDateFormat("yyyy-MM-dd")

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

                case Right(data) =>
                  //file downloaded, lets parse it
                  val updateJobDownloadedF =
                    speechService.updateJob(job.copy(status = SPEECH_PROCESS_STATUS.DOWNLOADED))
                  val source               = toAkkaSource(data.inputStream)
                  val updateJobParsingF    = speechService.updateJob(job.copy(status = SPEECH_PROCESS_STATUS.PARSING))
                  val sourceResultF        = parseSource(source)

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

  private def parseSource(source: Source[PoliticParsed, NotUsed]) = {
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

  private val deciderParse: Supervision.Decider = { case e: Exception =>
    logger.error("Decider exc CSV parse: " + e)
    Supervision.Resume
  }

  private def toAkkaSource(
      bomInputStream: BOMInputStream
  ): Source[PoliticParsed, NotUsed] = {

    StreamConverters
      .fromInputStream(in = () => bomInputStream, chunkSize = 8196 * 16)
      .via(CsvParsing.lineScanner(delimiter = delimiter, maximumLineLength = Int.MaxValue, escapeChar = escapeChar))
      .withAttributes(ActorAttributes.supervisionStrategy(deciderParse))
      .via(CsvToMap.toMapAsStringsCombineAll(customFieldValuePlaceholder = Option("missing")))
      .mapMaterializedValue(_ => NotUsed)
      .map { offerMap =>
        Try {

          val offerMapFixed = offerMap.map { case (k, v) =>
            k.trim -> v.trim
          }.toMap

          PoliticParsed(
            speaker = offerMapFixed
              .get("Redner")
              .map(cleanWhitespaces)
              .filter(_.nonEmpty)
              .getOrElse("INVALID"),
            topic = offerMapFixed
              .get("Thema")
              .map(cleanWhitespaces)
              .filter(_.nonEmpty)
              .getOrElse("INVALID"),
            dateOfSpeech = offerMapFixed
              .get("Datum")
              .map(cleanWhitespaces)
              .filter(_.nonEmpty)
              .map(dateFormat.parse(_))
              .getOrElse(new Date()),
            wordCount = offerMapFixed
              .get("Wörter")
              .map(cleanWhitespaces)
              .filter(_.nonEmpty)
              .map(_.toInt)
              .getOrElse(0)
          )
        }.toOption
      }
      .collect { case Some(row) =>
        //TODO filter invalid
        row
      }
  }

  def cleanWhitespaces(text: String): String = {
    text
      .replaceAll("(\\r|\\n)", " ")
      .trim
      .replaceAll("\\s+", " ")
  }

}
