package io.pipin.core.domain
import java.nio.charset.Charset

import ch.qos.logback.classic.{LoggerContext, PatternLayout}
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.rolling.{RollingFileAppender, SizeBasedTriggeringPolicy, TimeBasedRollingPolicy}
import ch.qos.logback.core.util.FileSize
import ch.qos.logback.core.{Appender, FileAppender}
import org.slf4j.LoggerFactory

/**
  * Created by libin on 2020/1/3.
  */
class Workspace (id:String){
  val workDir = s"workspaces/$id"
  val logFile:String = s"$workDir/logs/app.log"

  val logAppender: Appender[ILoggingEvent] = {

    val context: LoggerContext = new LoggerContext()
    context.setName(s"workspace-$id")
    val patternLayoutEncoder:PatternLayoutEncoder = new PatternLayoutEncoder()
    patternLayoutEncoder.setPattern("%d %-5level # [%thread] %logger{0}: %msg%n")
    patternLayoutEncoder.setContext(context)
    patternLayoutEncoder.setCharset(Charset.forName("UTF-8"))
    patternLayoutEncoder.start()

    val fileAppender = new RollingFileAppender[ILoggingEvent]
    val rollingPolicy = new TimeBasedRollingPolicy[ILoggingEvent]
    rollingPolicy.setFileNamePattern("app.%d{yyyy-MM-dd}.log.zip")
    rollingPolicy.setMaxHistory(30)
    val triggeringPolicy = new SizeBasedTriggeringPolicy[ILoggingEvent]
    triggeringPolicy.setMaxFileSize(FileSize.valueOf("5MB"))
    fileAppender.setFile(logFile)
    fileAppender.setName(s"workspace-$id")
    fileAppender.setContext(context)
    fileAppender.setRollingPolicy(rollingPolicy)
    fileAppender.setTriggeringPolicy(triggeringPolicy)

    fileAppender.setEncoder(patternLayoutEncoder)
    fileAppender.setImmediateFlush(true)
    fileAppender.start()
    context.start()
    fileAppender
  }

  def getLogger(className:String): org.slf4j.Logger ={
    val logger = logAppender.getContext.asInstanceOf[LoggerContext].getLogger(className)
    logger.addAppender(logAppender)
    logger
  }

}

object Workspace {
  def apply(id: String): Workspace = new Workspace(id)
}
