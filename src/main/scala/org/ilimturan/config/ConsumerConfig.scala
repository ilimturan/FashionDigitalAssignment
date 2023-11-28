package org.ilimturan.config

import com.typesafe.config.ConfigFactory

object ConsumerConfig {

  val config   = ConfigFactory.load("consumer")
  val httpHost = config.getString("http.host")
  val httpPort = config.getInt("http.port")

}
