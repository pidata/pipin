package io.pipin.web.server

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, MediaTypes}
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.util.FastFuture
import akka.util.ByteString
import io.pipin.core.domain.Dto
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.{read, write}

import scala.reflect.{ClassTag, classTag}

/**
  * Created by libin on 2020/5/13.
  */
trait RestJsonSupport {
  implicit val formats = DefaultFormats
  implicit def jsonByteStringUnmarshaller[T: Manifest]: Unmarshaller[HttpEntity, T] = {
    Unmarshaller.byteStringUnmarshaller.forContentTypes(ContentTypes.`application/json`).andThen(byteStringUnmarshaller)
  }

  implicit def byteStringUnmarshaller[T: Manifest]: Unmarshaller[ByteString, T] ={
    Unmarshaller.withMaterializer[ByteString, T](_ => _ => { bs =>
      FastFuture.successful(parse(bs.utf8String).extract[T])
    })
  }

  private def writeToJson(anyRef: Dto): String ={
    anyRef match {
      case _ => write(anyRef)
    }
  }

  implicit def jsonMarshaller: ToEntityMarshaller[Dto] =
    Marshaller.StringMarshaller.wrap(MediaTypes.`application/json`)(writeToJson)
}
