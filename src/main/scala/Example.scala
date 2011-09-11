package com.example

import unfiltered.request._
import unfiltered.response._
import org.clapper.avsl.Logger
import util._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}


trait ImplicitResponses {
  implicit def str2responseFn(str: String) =
    ResponseString(str)
  implicit def xml2html(xml: scala.xml.NodeSeq) =
    Html(xml)
}

trait Scalatra[Req,Res] {

  //this is used for all request methods for now
  private lazy val handlers = collection.mutable.Map[String,Function0[ResponseFunction[Res]]]()

  private lazy val _request = new DynamicVariable[HttpRequest[_]](null)

  def get(r:String)( f: => ResponseFunction[Res]) = {
    val p = () => f
    handlers += (r -> p)
  }
  implicit def request = _request value

  protected def executeRoutes(req: HttpRequest[_]):ResponseFunction[Res] =  {
    //TODO:proper matching logic should come here, for now it's matching all request methods from right to left
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
class App extends unfiltered.filter.Plan with Scalatra[HttpServletRequest,HttpServletResponse]
with ImplicitResponses {

  get ("/html") {
    <html>
      <head></head>
      <body>Hello html</body>
    </html>
  }

  get ("/hello") {
     "hello world, hello request:"+request.toString
  }

  get ("/") {
     "hello index page!"
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
