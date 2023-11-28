package org.ilimturan.config

import com.typesafe.config.ConfigFactory

object ProducerConfig {

  val config   = ConfigFactory.load("producer")
  val httpHost = config.getString("http.host")
  val httpPort = config.getInt("http.port")

}
