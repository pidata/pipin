package io.pipin.core.ext


/**
  * Created by libin on 2020/1/5.
  */
trait Converter {
  def convert(doc: java.util.Map[String, Object]): java.util.Map[String,java.util.Map[String,Object]]
}
