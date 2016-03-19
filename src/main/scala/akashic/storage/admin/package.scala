package akashic.storage

import akka.event.Logging
import akka.http.scaladsl.server.directives.{LoggingMagnet, DebuggingDirectives}
import org.slf4j.LoggerFactory
import akka.http.scaladsl.server.Directives._

package object admin {
  val logger = LoggerFactory.getLogger(getClass)
  // implicit val logger = Logging.getLogger(system, getClass)
  val apiLogger = DebuggingDirectives.logRequestResult("admin")
}
