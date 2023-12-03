package org.ilimturan.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.scaladsl.StreamConverters
import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.io.input.BOMInputStream
import org.ilimturan.models.TemporaryDownloadData

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class DownloadService(implicit ec: ExecutionContext, system: ActorSystem) extends StrictLogging {

  //TODO check "ETag" or "Last mod"
  def download(downloadUrl: String): Future[Either[String, TemporaryDownloadData]] = {

    val maybeRequest = Try {
      HttpRequest(uri = downloadUrl) //downloadUrl could be invalid
    }.toOption

    maybeRequest match {
      case Some(request) =>
        Http()
          .singleRequest(request)
          .map { response =>
            val statusCodeInt = response.status.intValue()
            if (statusCodeInt >= 200 && statusCodeInt <= 299) {

              val contentType = response.entity.contentType
              val inputStream = response.entity.dataBytes
                .runWith(StreamConverters.asInputStream())

              logger.info(s"Download completed, statusCode [$statusCodeInt], url [$downloadUrl]")
              Right(TemporaryDownloadData(new BOMInputStream(inputStream), Some(contentType)))

            } else {
              logger.warn(s"HttpError, statusCode [$statusCodeInt], url [$downloadUrl]")
              Left(s"HttpError, statusCode [$statusCodeInt], url [$downloadUrl]")
            }
          }
          .recover { case e: Exception =>
            logger.error(s"Error while downloading $downloadUrl", e)
            Left(s"Error while downloading")
          }
      case None          =>
        logger.warn(s"Invalid Url: $downloadUrl")
        Future.successful(Left("Invalid Url"))
    }
  }

}
