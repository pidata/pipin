package io.pipin.web.server.jobs

import akka.http.scaladsl.server.Directives.{complete, get, pathEnd}
import akka.http.scaladsl.server.Route

/**
  * Created by libin on 2020/1/15.
  */
object JobRoute {
  def router(): Route = {
    pathEnd{
      get{
        complete("")
      }
    }
  }
}
