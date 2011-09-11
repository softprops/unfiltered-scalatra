package com.example

import unfiltered.request._
import unfiltered.response._
import org.clapper.avsl.Logger
import util._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/** unfiltered plan */
trait Scalatra[Req,Res] {

  private lazy val handlers = collection.mutable.Map[String,Function0[ResponseFunction[Res]]]()

  private lazy val _request = new DynamicVariable[HttpRequest[_]](null)

  def get(r:String)( f: => ResponseFunction[Res]) = {
    val p = () => f
    handlers += (r -> p)
  }
  implicit def request = _request value

  protected def executeRoutes(req: HttpRequest[_]):ResponseFunction[Res] =  {
    //proper matching logic should come here, for now it's matching from left to right
    val handler = handlers.keys.filter(req.uri.startsWith(_))
    handler.lastOption map(handlers(_)()) getOrElse ( NotFound ~> ResponseString("could not find handler"))
  }
  val logger = Logger(classOf[App])

  //capture all requests
  def intent: unfiltered.Cycle.Intent[Req,Res] = {
    case req @ _  =>  _request.withValue(req) {
      executeRoutes(req)
    }
  }
}

/**
* would be nice to utilize unfiltered.filter.Plan.Intent and 
* get rid of [HttpServletRequest,HttpServletResponse] but it should be OK for now
*/
class App extends unfiltered.filter.Plan with Scalatra[HttpServletRequest,HttpServletResponse] {

  get ("/hello") {
     ResponseString("hello world, hello request:"+request.toString)
  }

  get ("/") {
     ResponseString("hello index page!")
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
