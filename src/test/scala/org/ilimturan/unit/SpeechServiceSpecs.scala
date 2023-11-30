package org.ilimturan.unit

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.stream.Materializer
import akka.testkit.TestDuration
import com.typesafe.scalalogging.StrictLogging
import org.ilimturan.enums.SPEECH_PROCESS_STATUS
import org.ilimturan.models.{PoliticalSpeech, SpeechFileProcess}
import org.ilimturan.repos.SpeechRepo
import org.ilimturan.services.SpeechService
import org.joda.time.DateTime
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar

import java.util.Date
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
class SpeechServiceSpecs
    extends WordSpec
    with Matchers
    with MockitoSugar
    with BeforeAndAfterAll
    with ScalatestRouteTest
    with SprayJsonSupport
    with StrictLogging
    with ScalaFutures {

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(30.seconds.dilated)


  implicit val actorSystem = ActorSystem("ConsumerSpeech")
  implicit val mat         = Materializer(actorSystem)
  implicit val ec          = actorSystem.dispatcher

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

      val politicalSpeech = PoliticalSpeech(
        id = 99,
        politicianName = "Test politicianName",
        topicName = "Test topicName",
        wordCount = 77,
        speechDate = new Date
      )

      when(mockedRepo.addPoliticalSpeech(politicalSpeech)).thenReturn(Future.successful(politicalSpeech))

      val result = service.addPoliticalSpeech(politicalSpeech)
      result.futureValue shouldEqual politicalSpeech
    }

  }
}
