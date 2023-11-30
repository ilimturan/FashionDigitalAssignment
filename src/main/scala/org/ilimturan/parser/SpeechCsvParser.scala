package org.ilimturan.parser

import akka.NotUsed
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{Source, StreamConverters}
import akka.stream.{ActorAttributes, Materializer, Supervision}
import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.io.input.BOMInputStream
import org.ilimturan.models.PoliticParsed

import java.text.SimpleDateFormat
import java.util.Date
import scala.concurrent.ExecutionContext
import scala.util.Try

class SpeechCsvParser()(implicit
    ec: ExecutionContext,
    mat: Materializer
) extends StrictLogging {

  private val delimiter  = ','.toByte
  private val escapeChar = '\"'.toByte
  private val dateFormat = new SimpleDateFormat("yyyy-MM-dd")

  private val deciderParse: Supervision.Decider = { case e: Exception =>
    logger.error("Decider exc CSV parse: " + e)
    Supervision.Resume
  }

  def toAkkaSource(
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
