package com.example

import unfiltered.request._
import unfiltered.response._

import org.clapper.avsl.Logger

/** unfiltered plan */
class Scalatra extends unfiltered.filter.Plan {

  private val handlers = collection.mutable.Map[String,Function1[HttpResponse]]

  protected val _request    = new DynamicVariable[HttpRequest[_]](null)

  implicit def request = _request value

  protected def executeRoutes =  {
    
  }
  val logger = Logger(classOf[App])

  def intent = {
    case req @ _  =>  _request.withValue(req) {
        executeRoutes
    }
    
  }
}
class App extends Scalatra {
  
}


/** embedded server */
object Server {
  val logger = Logger(Server.getClass)
  def main(args: Array[String]) {
    val http = unfiltered.jetty.Http.anylocal // this will not be necessary in 0.4.0
    http.context("/assets") { _.resources(new java.net.URL(getClass().getResource("/www/css"), ".")) }
      .filter(new App).run({ svr =>
        unfiltered.util.Browser.open(http.url)
      }, { svr =>
        logger.info("shutting down server")
      })
  }
}