package io.pipin.web.server.projects

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

/**
  * Created by libin on 2020/1/15.
  */
object ProjectRoute {
  def router(): Route = {
    pathEnd{
      get{
        complete("")
      }
    }
  }
}
