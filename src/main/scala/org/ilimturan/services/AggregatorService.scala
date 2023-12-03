package org.ilimturan.services

import akka.actor.Cancellable
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.scalalogging.StrictLogging
import org.ilimturan.models.{AggPoliticianSpeechCount, AggPoliticianSpeechTopicCount, AggPoliticianSpeechWordCount}

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}

class AggregatorService(speechService: SpeechService)(implicit ec: ExecutionContext, mat: Materializer)
    extends StrictLogging {

  //TODO this is in-memory solution
  var lastProcessedId: Long = -1L

  def init() = {
    val initialDelayDuration = 1.seconds
    val intervalDuration     = 1.minutes

    getTickSource(initialDelayDuration, intervalDuration)
      .runWith(Sink.ignore)
    true
  }

  def getTickSource(
      initialDelayDuration: FiniteDuration,
      intervalDuration: FiniteDuration
  ): Source[Int, Cancellable] = {
    Source
      .tick(initialDelayDuration, intervalDuration, ())
      .mapAsync(1) { _ =>
        //This is tricky solution "are we need agg"
        speechService
          .getLatestCompletedJob()
          .map {
            case Some(job) if (job.id > lastProcessedId) =>
              logger.info("Starting agg")
              lastProcessedId = job.id
              Some(job)
            case _                                       =>
              logger.info(s"No need agg, last id [$lastProcessedId]")
              None
          }
          .collect { case Some(job) =>
            job
          }
      }
      .mapAsync(1) { _ =>
        val speechCountsAggF    = speechService.speechCounts()
        val speechTopicsAggF    = speechService.speechTopics()
        val speechWordCountAggF = speechService.speechWordCount()

        val resultF = for {
          speechCountsAgg    <- speechCountsAggF
          speechTopicsAgg    <- speechTopicsAggF
          speechWordCountAgg <- speechWordCountAggF
        } yield {
          val agg1F = speechCountsAggSource(speechCountsAgg)
          val agg2F = speechTopicsAggSource(speechTopicsAgg)
          val agg3F = speechWordCountAggSource(speechWordCountAgg)

          for {
            agg1 <- agg1F
            agg2 <- agg2F
            agg3 <- agg3F
          } yield {
            logger.info(s"Agg completed, agg1 count [${agg1}], agg2 count [${agg2}], agg3 count [${agg3}]")
            agg1 + agg2 + agg3
          }

        }

        resultF.flatten
          .recover { case e: Exception =>
            logger.error("Error when calculating agg result", e)
            0
          }
      }
  }

  def speechCountsAggSource(speechCountsAgg: List[AggPoliticianSpeechCount]): Future[Int] = {
    Source
      .fromIterator(() => speechCountsAgg.iterator)
      .mapAsync(2) { row =>
        speechService
          .addAggPoliticianSpeechCount(row)
          .map(_ => 1)
      }
      .runFold(0) { case (acc, count) =>
        if (acc > 0 && acc % 100 == 0) {
          logger.info(s"Processed agg count [$acc]")
        }
        acc + count
      }
  }

  def speechTopicsAggSource(speechTopicsAgg: List[AggPoliticianSpeechTopicCount]): Future[Int] = {
    Source
      .fromIterator(() => speechTopicsAgg.iterator)
      .mapAsync(2) { row =>
        speechService
          .addAggPoliticianSpeechTopicCount(row)
          .map(_ => 1)
      }
      .runFold(0) { case (acc, count) =>
        if (acc > 0 && acc % 100 == 0) {
          logger.info(s"Processed agg count [$acc]")
        }
        acc + count
      }
  }

  def speechWordCountAggSource(speechWordCountAgg: List[AggPoliticianSpeechWordCount]): Future[Int] = {
    Source
      .fromIterator(() => speechWordCountAgg.iterator)
      .mapAsync(2) { row =>
        speechService
          .addAggPoliticianSpeechWordCount(row)
          .map(_ => 1)
      }
      .runFold(0) { case (acc, count) =>
        if (acc > 0 && acc % 100 == 0) {
          logger.info(s"Processed agg count [$acc]")
        }
        acc + count
      }
  }

}
