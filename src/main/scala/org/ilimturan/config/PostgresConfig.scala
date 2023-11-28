package org.ilimturan.config

import com.typesafe.config.{Config, ConfigFactory}

object PostgresConfig {

  val config   = ConfigFactory.load("postgres")

  val dbPostgresConfig: Config = config.getConfig("postgres")
}
