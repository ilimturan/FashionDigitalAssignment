package org.ilimturan.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging
import spray.json.{JsObject, JsString}

class RoutesAggregator() extends StrictLogging with SprayJsonSupport {

  val routes: Route = Route.seal(endpoints)

  private def endpoints: Route =
    path("health") {
      get {
        complete(
          JsObject(
            "name"    -> JsString("Aggregator"),
            "version" -> JsString("1.0.0"),
            "status"  -> JsString("ok")
          )
        )
      }
    }

}
