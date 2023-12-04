package org.ilimturan.unit

import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.io.input.BOMInputStream
import org.ilimturan.enums.SPEECH_PROCESS_STATUS
import org.ilimturan.models.{Speech, SpeechFileProcess}
import org.ilimturan.parser.SpeechCsvParser
import org.ilimturan.repos.SpeechRepo
import org.ilimturan.services.SpeechService
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar

import java.util.Date
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
class SpeechServiceSpecs
    extends WordSpec
    with Matchers
    with MockitoSugar
    with ScalatestRouteTest
    with StrictLogging
    with ScalaFutures {

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(10.seconds.dilated)
  //override implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = scaled(Span(10, Seconds)))

  "run SpeechService mocked tests" should {

    val mockedRepo = mock[SpeechRepo]
    val service    = new SpeechService(mockedRepo)

    /*
    //TODO fix, it throw NPE
    "when handleEvaluation" in {

      val urlSet            = Seq(
        "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics1.csv"
      )
      val insertTime        = DateTime.now()
      val speechRequest     = SpeechRequest(1L, urls = urlSet.mkString(","), insertTime = insertTime)
      val speechFileProcess = SpeechFileProcess(
        id = 1L,
        requestId = 1L,
        url = urlSet.head,
        status = SPEECH_PROCESS_STATUS.READY,
        failReason = None,
        insertTime = insertTime
      )

      when(mockedRepo.addSpeechRequest(speechRequest)).thenReturn(Future.successful(speechRequest))
      when(mockedRepo.getLatestJob()).thenReturn(Future.successful(None))
      when(mockedRepo.addSpeechFileProcess(speechFileProcess)).thenReturn(Future.successful(speechFileProcess))

      val result = service.handleEvaluation(urlSet)
      result.futureValue.isRight shouldEqual true
    }
     */

    "when getLatestJob one row" in {

      val speechFileProcess = SpeechFileProcess(
        id = 1L,
        requestId = 1L,
        url = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics1.csv",
        status = SPEECH_PROCESS_STATUS.READY,
        failReason = None,
        insertTime = DateTime.now()
      )

      when(mockedRepo.getLatestJob()).thenReturn(Future.successful(Some(speechFileProcess)))

      val result = service.getLatestJob()
      result.futureValue.get shouldEqual speechFileProcess
    }

    "when getLatestJob empty" in {

      when(mockedRepo.getLatestJob()).thenReturn(Future.successful(None))

      val result = service.getLatestJob()
      result.futureValue shouldEqual None
    }

    "when updateJob" in {

      val speechFileProcess = SpeechFileProcess(
        id = 1L,
        requestId = 1L,
        url = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics1.csv",
        status = SPEECH_PROCESS_STATUS.FAILED,
        failReason = Some("for test"),
        insertTime = DateTime.now()
      )

      when(mockedRepo.updateJob(speechFileProcess)).thenReturn(Future.successful(speechFileProcess))

      val result = service.updateJob(speechFileProcess)
      result.futureValue shouldEqual speechFileProcess

    }

    "when addPoliticalSpeech" in {

      val speech = Speech(
        id = 99,
        politicianName = "Test politicianName",
        topicName = "Test topicName",
        wordCount = 77,
        partitionId = 2000,
        speechDate = new Date()
      )

      when(mockedRepo.addPoliticalSpeech(speech)).thenReturn(Future.successful(true))

      val result = service.addPoliticalSpeech(speech)
      result.futureValue shouldEqual true
    }

    "when addPoliticalSpeechFromSource" in {

      val parser         = new SpeechCsvParser()
      val inputStream    = getClass.getResourceAsStream("/politics.csv")
      val bomInputStream = new BOMInputStream(inputStream)
      val source         = parser.toAkkaSource(bomInputStream)

      when(mockedRepo.addPoliticalSpeech(any[Speech])).thenReturn(Future.successful(true))

      val result = service.addPoliticalSpeechFromSource(source)
      result.futureValue shouldEqual 4
    }

  }
}
