package io.pipin.core.importer

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{Framing, Source}
import akka.util.ByteString
import org.bson.Document
import io.pipin.core.Converters._

import scala.concurrent.ExecutionContext

/**
  * Created by libin on 2020/1/6.
  */
trait Importer {
  def stream(source:Source[ByteString, Any] = Source.empty)(implicit executor: ExecutionContext, materializer:Materializer): Source[Document,Any]
}


class CSVImporter extends Importer{
  override def stream(source: Source[ByteString, Any])(implicit executor: ExecutionContext, materializer:Materializer): Source[Document, Any] = {
    source.via(CsvParsing.lineScanner())
      .via(CsvToMap.toMap()).map{
      (line: Map[String, ByteString]) =>
        new Document(line.map{
          case (k:String, v:ByteString)=>
            (k, v.utf8String)
        })
    }
  }
}



class JsonImporter extends Importer{
  override def stream(source: Source[ByteString, Any])(implicit executor: ExecutionContext, materializer:Materializer): Source[Document, Any] = {
    source.via(Framing.delimiter(ByteString("\n"), 1024)).map(_.utf8String)
      .map(Document.parse)
  }
}