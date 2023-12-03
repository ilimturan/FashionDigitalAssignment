package org.ilimturan.config

import com.typesafe.config.ConfigFactory

object AggregatorConfig {

  val config   = ConfigFactory.load("aggregator")
  val httpHost = config.getString("http.host")
  val httpPort = config.getInt("http.port")

}
