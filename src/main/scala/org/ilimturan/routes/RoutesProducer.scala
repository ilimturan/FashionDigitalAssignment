package org.ilimturan.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging
import org.ilimturan.services.SpeechService
import spray.json._
import org.ilimturan.models.Protocols._

class RoutesProducer(speechService: SpeechService) extends StrictLogging with SprayJsonSupport {

  val routes: Route = Route.seal(endpoints)

  private def endpoints: Route =
    path("health") {
      get {
        complete(
          JsObject(
            "name"    -> JsString("Producer"),
            "version" -> JsString("1.0.0"),
            "status"  -> JsString("ok")
          )
        )
      }
    } ~ path("evaluation") {
      get {
        parameters("url".as[String].*) { urls =>
          complete(
            speechService
              .handleEvaluation(urls.toSeq)
          )

        }
      }
    }

}
