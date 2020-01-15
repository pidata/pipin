package io.pipin.core

import akka.actor.ActorSystem

/**
  * Created by libin on 2020/1/9.
  */
object PipinSystem {
  val actorSystem:ActorSystem = ActorSystem("pipin")
}
