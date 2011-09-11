package com.example

import unfiltered.request._
import unfiltered.response._
import util._
import org.clapper.avsl.Logger

/** unfiltered plan */
class Scalatra extends unfiltered.filter.Plan {

  private val handlers = collection.mutable.Map[String,Function0[AnyRef]]()

  protected val _request    = new DynamicVariable[HttpRequest[_]](null)

  def get(r:String)( f: => AnyRef) = {
    val p = () => f
    handlers += (r -> p)
  }
  implicit def request = _request value

  protected def executeRoutes(req: HttpRequest[_]):ResponseFunction[javax.servlet.http.HttpServletResponse] =  {
    //proper matching logic should come here, for now it's very simplistic
    val handler = handlers.keys.filter(req.uri.startsWith(_))
    handler.headOption.map(handlers(_).asInstanceOf[ResponseFunction[javax.servlet.http.HttpServletResponse]]).getOrElse ( NotFound ~> ResponseString("could not find handler"))
  }
  val logger = Logger(classOf[App])

  def intent = {
    case req @ _  =>  _request.withValue(req) {
        executeRoutes(req)
    }
  }
}
class App extends Scalatra {
  get ("/hello") {
    Ok ~> ResponseString("hello world, hello:"+request.toString)
  }
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
