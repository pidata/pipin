package io.pipin.core.util

import scala.util.Random

/**
  * Created by libin on 2020/1/9.
  */
object UUID {
  def apply(): String =  Hashing.fnvHashStr(Random.nextString(8))
}
