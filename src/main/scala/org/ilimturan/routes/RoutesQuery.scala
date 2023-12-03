package org.ilimturan.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, NotAcceptable}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import com.typesafe.scalalogging.StrictLogging
import org.ilimturan.exceptions.SpeechExceptions.ValidationError
import org.ilimturan.models.MySpeechAggResponse._
import org.ilimturan.services.SpeechService
import spray.json._

class RoutesQuery(speechService: SpeechService) extends StrictLogging with SprayJsonSupport {

  val routes: Route = Route.seal(endpoints)

  private def endpoints: Route =
    path("health") {
      get {
        complete(
          JsObject(
            "name"    -> JsString("Query"),
            "version" -> JsString("1.0.0"),
            "status"  -> JsString("ok")
          )
        )
      }
    } ~ path("report") {
      get {
        parameters("mostYear".?, "mostTopic".?) { (mostYear, mostTopic) =>
          complete(
            speechService
              .getSpeechAggResult(mostYear, mostTopic)
          )
        }
      }
    }

  implicit def customExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e: ValidationError =>
        extractUri { _ =>
          logger.error(e.message)
          complete(HttpResponse(NotAcceptable, entity = e.message + " : " + e.data.map(dataStr => s"[$dataStr]")))
        }
      case e: Exception       =>
        extractUri { _ =>
          logger.error(e.getMessage)
          complete(HttpResponse(InternalServerError, entity = e.getMessage))
        }
    }

}
