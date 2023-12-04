package org.ilimturan.unit

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.io.input.BOMInputStream
import org.ilimturan.consumer.SpeechConsumer
import org.ilimturan.enums.SPEECH_PROCESS_STATUS
import org.ilimturan.models.{PoliticParsed, SpeechFileProcess, TemporaryDownloadData}
import org.ilimturan.parser.SpeechCsvParser
import org.ilimturan.services.{DownloadService, SpeechService}
import org.joda.time.DateTime
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar

import java.util.Date
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class SpeechConsumerSpecs
    extends WordSpec
    with Matchers
    with MockitoSugar
    with SprayJsonSupport
    with StrictLogging
    with ScalaFutures {

  // for futureValue timeout
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = scaled(Span(5, Seconds)))

  implicit val actorSystem = ActorSystem("SpeechConsumerSpecs")
  implicit val mat         = Materializer(actorSystem)
  implicit val ec          = actorSystem.dispatcher

  "run SpeechConsumer tests" should {

    val mockedSpeechService   = mock[SpeechService]
    val mockedSpeechCsvParser = mock[SpeechCsvParser]
    val mockedDownloadService = mock[DownloadService]

    val consumer = new SpeechConsumer(mockedSpeechService, mockedSpeechCsvParser, mockedDownloadService)

    "when there is no job" in {

      when(mockedSpeechService.getLatestJob()).thenReturn(Future.successful(None))

      val initialDelayDuration = 0.seconds
      val intervalDuration     = 10.seconds

      val result = consumer
        .getTickSource(initialDelayDuration, intervalDuration)
        .runWith(Sink.head)
      //.map(_.sum)

      result.futureValue shouldEqual 0
    }

    "when there is one job and can not download" in {

      val insertTime        = DateTime.now()
      val speechFileProcess = SpeechFileProcess(
        id = 101L,
        requestId = 11L,
        url = "hts://fid-recruiting.s3-eu-west-1.amazonaws.com/politics1.csv", //Wrong url
        status = SPEECH_PROCESS_STATUS.READY,
        failReason = None,
        insertTime = insertTime
      )

      val speechFileProcessUpdated = speechFileProcess.copy(
        status = SPEECH_PROCESS_STATUS.FAILED,
        failReason = Some("Invalid Url")
      )

      when(mockedSpeechService.getLatestJob())
        .thenReturn(Future.successful(Some(speechFileProcess)))
      when(mockedSpeechService.updateJob(speechFileProcessUpdated))
        .thenReturn(Future.successful(speechFileProcessUpdated))
      when(mockedDownloadService.download(speechFileProcess.url))
        .thenReturn(Future.successful(Left("Invalid Url")))

      val initialDelayDuration = 0.seconds
      val intervalDuration     = 10.seconds

      val result = consumer
        .getTickSource(initialDelayDuration, intervalDuration)
        .runWith(Sink.head)
      //.map(_.sum)

      result.futureValue shouldEqual 0
    }

    "when there is one job and successfully download and parse" in {

      //val resourceName = "/test/resources/politics.csv"
      //val resourceName = getClass.getResource("/politics.csv")
      val inputStream    = getClass.getResourceAsStream("/politics.csv")
      val bomInputStream = new BOMInputStream(inputStream)

      //val str = scala.io.Source.fromInputStream(bomInputStream).mkString

      val insertTime        = DateTime.now()
      val speechFileProcess = SpeechFileProcess(
        id = 201L,
        requestId = 21L,
        url = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv",
        status = SPEECH_PROCESS_STATUS.READY,
        failReason = None,
        insertTime = insertTime
      )

      val speechFileProcessUpdated1 = speechFileProcess.copy(
        status = SPEECH_PROCESS_STATUS.DOWNLOADED
      )
      val speechFileProcessUpdated2 = speechFileProcess.copy(
        status = SPEECH_PROCESS_STATUS.PARSING
      )
      val speechFileProcessUpdated3 = speechFileProcess.copy(
        status = SPEECH_PROCESS_STATUS.COMPLETED
      )

      when(mockedSpeechService.getLatestJob())
        .thenReturn(Future.successful(Some(speechFileProcess)))

      when(mockedDownloadService.download(speechFileProcess.url))
        .thenReturn(Future.successful(Right(TemporaryDownloadData(bomInputStream, None))))

      when(mockedSpeechService.updateJob(speechFileProcessUpdated1))
        .thenReturn(Future.successful(speechFileProcessUpdated1))
      when(mockedSpeechService.updateJob(speechFileProcessUpdated2))
        .thenReturn(Future.successful(speechFileProcessUpdated2))
      when(mockedSpeechService.updateJob(speechFileProcessUpdated3))
        .thenReturn(Future.successful(speechFileProcessUpdated3))

      val dataIterator: Iterator[PoliticParsed] = Iterator(
        PoliticParsed("aaa", "tttt", new Date(), 1),
        PoliticParsed("bbb", "tttt", new Date(), 1),
        PoliticParsed("ccc", "tttt", new Date(), 1),
        PoliticParsed("dddd", "tttt", new Date(), 1)
      )

      val sourceReturn = Source.fromIterator(() => dataIterator)

      when(mockedSpeechCsvParser.toAkkaSource(bomInputStream))
        .thenReturn(sourceReturn)
      when(mockedSpeechService.addPoliticalSpeechFromSource(sourceReturn))
        .thenReturn(Future.successful(4))

      val initialDelayDuration = 0.seconds
      val intervalDuration     = 10.seconds

      val result = consumer
        .getTickSource(initialDelayDuration, intervalDuration)
        .runWith(Sink.head)
      //.map(_.sum)

      result.futureValue shouldEqual 4
    }

  }
}
