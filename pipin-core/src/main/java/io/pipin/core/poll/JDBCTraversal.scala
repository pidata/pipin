package io.pipin.core.poll

import java.sql.{Connection, DriverManager, PreparedStatement}

import akka.actor.ActorSystem
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{Source, SourceQueueWithComplete}
import com.typesafe.config.{Config, ConfigFactory}
import io.pipin.core.settings.PollSettings
import org.bson.Document
import org.slf4j.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConverters._
import scala.util.{Failure, Success}

/**
  * Created by libin on 2020/4/21.
  */

/*
*
*/
abstract class JDBCTraversal(val uri:String,
                             val pageParameter:String,
                             val pageStartFrom:Int = 0,
                             pollSettings:PollSettings
                   )( implicit val actorSystem: ActorSystem,  implicit val log: Logger) extends Traversal{

  private val config:Config = ConfigFactory.load().getConfig("jdbc")

  val docSource: Source[Document, SourceQueueWithComplete[Document]] = Source.queue[Document](Integer.MAX_VALUE, OverflowStrategy.fail)

  override def stream()(implicit executor: ExecutionContext, materializer: Materializer): Source[Document, Any] = docSource



  override def start(queue: Any)(implicit executor: ExecutionContext, materializer: Materializer): Unit = {
    queue match {
      case q:SourceQueueWithComplete[Document] =>
        fetchData(q).onComplete{
          case Success(_)=>
            q.complete()
          case Failure(e) =>
            q.fail(e)
        }
    }
  }



  def fetchData(q: SourceQueueWithComplete[Document])(implicit executor: ExecutionContext): Future[String] = {
    log.info(sql)
    Future{
      val resultSet = preparedStatement.executeQuery()
      while (resultSet.next()){
        val map = fields.map{
          field =>
            resultSet.getObject(field) match {
              case v:java.math.BigDecimal =>
                (field, java.lang.Double.valueOf(v.doubleValue()))
              case _ =>
                (field, resultSet.getObject(field))
            }
        }.toSeq.toMap
        q.offer(new Document(map.asJava))
      }
      "ok"
    }

  }


  def jdbcConnection(): Connection = {
    Class.forName(jdbcDriver).newInstance()
    val conn = DriverManager.getConnection(jdbcUrl, jdbcUserName, jdbcPassword)
    conn
  }

  def jdbcDriver:String = config.getString("driver")

  def jdbcPassword:String = config.getString("password")

  def jdbcUserName: String = config.getString("username")

  def jdbcUrl:String = config.getString("url")

  def sql:String = "select "+ fields.reduce((a, b)=> a + "," + b) + " from " + table

  def preparedStatement:PreparedStatement = { jdbcConnection().prepareStatement(sql)}

  def fields:Array[String]

  def table:String

}
