package io.pipin.core.util

/**
  * Created by libin on 2020/1/5.
  */
object Hashing {
  def fnvHash(`object`: String): Long = {
    var hash = 0xcbf29ce484222325L
    for (value <- `object`.getBytes) {
      hash = hash ^ value
      val FNV_PRIME = 0x100000001b3L
      hash = hash * FNV_PRIME
    }
    hash
  }

  def fnvHashStr(`object`: String): String = {
    val array = Array[String]("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "-", "_")
    var hash = fnvHash(`object`)
    val buffer = new StringBuilder
    while ( {
      buffer.length < 8
    }) {
      val index = (hash & 63).toInt
      buffer.append(array(index))
      hash >>= 6
    }
    buffer.toString
  }
}
