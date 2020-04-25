package io.pipin.core.ext

import akka.Done
import org.slf4j.Logger

import scala.concurrent.{ExecutionContext, Future, Promise}

/**
  * Created by libin on 2020/1/5.
  */
class EntitySink(log:Logger) {
  def update(entity: Entity):String = {
    ""
  }

  def asyncUpdate(entity: Entity, promise:Promise[String])(implicit executor: ExecutionContext): Unit = {
    Future{
      update(entity)
    }.onComplete{
      result =>
        promise.complete(result)
        Done
    }
  }

}
