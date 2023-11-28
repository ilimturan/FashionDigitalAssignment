package org.ilimturan.config

import com.typesafe.config.ConfigFactory

object RestConfig {

  val config   = ConfigFactory.load("rest")
  val httpHost = config.getString("http.host")
  val httpPort = config.getInt("http.port")

}
