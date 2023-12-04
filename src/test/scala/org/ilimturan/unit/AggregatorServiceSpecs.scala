package org.ilimturan.unit

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.typesafe.scalalogging.StrictLogging
import org.ilimturan.enums.SPEECH_PROCESS_STATUS
import org.ilimturan.models.{
  AggPoliticianSpeechCount,
  AggPoliticianSpeechTopicCount,
  AggPoliticianSpeechWordCount,
  SpeechFileProcess
}
import org.ilimturan.services.{AggregatorService, SpeechService}
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class AggregatorServiceSpecs
    extends WordSpec
    with Matchers
    with MockitoSugar
    with ScalatestRouteTest
    with StrictLogging
    with ScalaFutures {

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = scaled(Span(10, Seconds)))

  implicit val actorSystem = ActorSystem("AggregatorServiceSpecs")
  implicit val mat         = Materializer(actorSystem)
  implicit val ec          = actorSystem.dispatcher

  "run SpeechConsumer tests" should {

    val mockedSpeechService = mock[SpeechService]
    val service             = new AggregatorService(mockedSpeechService)

    "when there is no agg job" in {

      when(mockedSpeechService.getLatestCompletedJob()).thenReturn(Future.successful(None))

      val initialDelayDuration = 0.seconds
      val intervalDuration     = 10.seconds

      val result = service
        .getTickSource(initialDelayDuration, intervalDuration)
        .runWith(Sink.head)
      //.map(_.sum)

      result.futureValue shouldEqual 0
    }

    "when there is one job and successfully agg" in {

      val random = scala.util.Random

      val lastCompletedJob = SpeechFileProcess(1L, 1L, "test", SPEECH_PROCESS_STATUS.COMPLETED, None, DateTime.now())

      val aggPoliticianSpeechCountList = (1L to 90L).map { id =>
        AggPoliticianSpeechCount(
          id = id,
          politicianName = random.nextString(20),
          partitionId = 2000 + random.nextInt(20),
          aggCount = random.nextLong(20)
        )
      }.toList

      val aggPoliticianSpeechTopicCountList = (1L to 80L).map { id =>
        AggPoliticianSpeechTopicCount(
          id = id,
          politicianName = random.nextString(20),
          topicName = "test",
          aggCount = random.nextLong(20)
        )
      }.toList

      val aggPoliticianSpeechWordCountList = (1L to 110L).map { id =>
        AggPoliticianSpeechWordCount(
          id = id,
          politicianName = random.nextString(20),
          aggCount = random.nextLong(20)
        )
      }.toList

      when(mockedSpeechService.getLatestCompletedJob())
        .thenReturn(Future.successful(Some(lastCompletedJob)))

      when(mockedSpeechService.speechCounts())
        .thenReturn(Future.successful(aggPoliticianSpeechCountList))

      when(mockedSpeechService.speechTopics())
        .thenReturn(Future.successful(aggPoliticianSpeechTopicCountList))

      when(mockedSpeechService.speechWordCount())
        .thenReturn(Future.successful(aggPoliticianSpeechWordCountList))

      //---- speechService
      when(mockedSpeechService.addAggPoliticianSpeechCount(any[AggPoliticianSpeechCount]))
        .thenReturn(Future.successful(true))

      when(mockedSpeechService.addAggPoliticianSpeechTopicCount(any[AggPoliticianSpeechTopicCount]))
        .thenReturn(Future.successful(true))

      when(mockedSpeechService.addAggPoliticianSpeechWordCount(any[AggPoliticianSpeechWordCount]))
        .thenReturn(Future.successful(true))

      val initialDelayDuration = 1.seconds
      val intervalDuration     = 10.seconds

      val result = service
        .getTickSource(initialDelayDuration, intervalDuration)
        .runWith(Sink.head)

      val resultCount =
        aggPoliticianSpeechCountList.size + aggPoliticianSpeechTopicCountList.size + aggPoliticianSpeechWordCountList.size
      result.futureValue shouldEqual resultCount
    }

  }

}
