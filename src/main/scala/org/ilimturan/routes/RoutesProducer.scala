package org.ilimturan.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging
import org.ilimturan.services.SpeechService
import spray.json.{JsObject, JsString}

class RoutesProducer(speechService: SpeechService) extends StrictLogging with SprayJsonSupport {

  val routes: Route = Route.seal(endpoints)

  private def endpoints =
    path("health") {
      get {
        complete(
          JsObject(
            "name"    -> JsString("FashionDigitalAssignment"),
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
