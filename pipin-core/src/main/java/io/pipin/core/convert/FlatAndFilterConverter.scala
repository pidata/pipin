package io.pipin.core.convert

import java.util

import io.pipin.core.ext.Converter
import org.bson.Document

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Created by libin on 2020/3/23.
  */

/*
*
*/
class FlatAndFilterConverter(entity:String, fields:Array[String] = Array.empty) extends Converter{
  override def convert(doc: util.Map[String, Object]): util.Map[String, util.Map[String, Object]] = {
    val result:mutable.Map[String,AnyRef] = mutable.Map()
    doc.asScala.map{
      case (k:String, v:Document) =>
        flat(k, v, result)
      case (k, v:util.List[String]) =>
        result.put(k, v.toArray().fold("")( (a,b) => s"$a,$b"))
      case (k, v) =>
        result.put(k, v)
    }



    val obj = new util.HashMap[String,util.Map[String, Object]]()
    if(null!=fields && fields.length>0){
      obj.put(entity, result.filterKeys(fields.contains).asJava)
    }else{
      obj.put(entity, result.asJava)
    }

    obj
  }

  def flat(prex:String, doc:Document, result:mutable.Map[String,AnyRef]): Unit ={
    doc.asScala.map{
      case (k:String, v:Document) =>
        flat(s"${prex}_$k", v, result)
      case (k, v:util.List[String]) =>
        result.put(s"${prex}_$k", v.toArray().fold("")( (a,b) => s"$a,$b"))
      case (k, v) =>
        result.put(s"${prex}_$k", v)
    }
  }
}
