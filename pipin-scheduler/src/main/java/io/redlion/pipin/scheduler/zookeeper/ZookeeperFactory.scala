package io.redlion.pipin.scheduler.zookeeper

import org.apache.curator.RetryPolicy
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.state.ConnectionState
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.zookeeper.{CreateMode, KeeperException, ZooDefs}
import java.util.Collections

import com.sun.tools.jconsole.JConsoleContext
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.sslconfig.util.ConfigLoader
import org.slf4j.LoggerFactory

/**
  * Created by libin on 2020/1/15.
  */

/*
*
*/
class ZookeeperFactory(hosts:String, monopoly:String) {


  private val zkTools: CuratorFramework = _
  private val nameSpace:String = ""

  private val logger = LoggerFactory.getLogger("ZookeeperFactory")

  private var connectionState:String = ""

  private var sessionId:Long = 0l

  private val (separator, queue_node) = ("/", "queue")


  private var monopolyQueueNode: String = ""


  def connection(): Unit = {
    val retryPolicy = new ExponentialBackoffRetry(1000, Integer.MAX_VALUE)
    val zkTools: CuratorFramework = CuratorFrameworkFactory.builder.connectString(hosts).namespace(nameSpace).retryPolicy(retryPolicy).build
    zkTools.start
  }

  /**
    * 连接状态监听
    */
  def addListener(): Unit = {
    zkTools.getConnectionStateListenable.addListener((client: CuratorFramework, newState: ConnectionState) => {
      if (newState == ConnectionState.CONNECTED) {
        logger.info("连接...")
        connectionState = "CONNECTED"
        try {
          sessionId = zkTools.getZookeeperClient.getZooKeeper.getSessionId
          registerMonopolyQueue()
        } catch {
          case e: Exception =>
            logger.error("注册独占队列失败")
        }
      }
      if (newState == ConnectionState.RECONNECTED) {
        logger.info("连接重新")
        connectionState = "CONNECTED"
        try
            if (sessionId ne zkTools.getZookeeperClient.getZooKeeper.getSessionId) registerMonopolyQueue()
        catch {
          case e: Exception =>
            logger.error("注册独占队列失败")
        }
      }
      if (newState == ConnectionState.LOST) {
        logger.warn("连接丢失")
        connectionState = "LOST"
      }
      if (newState == ConnectionState.SUSPENDED) {
        logger.info("连接暂停")
        connectionState = "SUSPENDED"
      }
      if (newState == ConnectionState.READ_ONLY) {
        logger.info("只读")
        connectionState = "READ_ONLY"
      }
    })
  }


  /**
    * 注册独占队列
    */
  @throws[Exception]
  private def registerMonopolyQueue() = {

    if (zkTools.checkExists.watched.forPath(monopoly) == null) {
      zkTools.create.withMode(CreateMode.PERSISTENT).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath(monopoly)

    }
    if (monopolyQueueNode == null || (monopolyQueueNode != null && zkTools.checkExists.forPath(monopolyQueueNode) == null)) {
      monopolyQueueNode = zkTools.create.withMode(CreateMode.EPHEMERAL_SEQUENTIAL).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath(monopoly + separator + queue_node)

    }
  }

  /**
    * 获得独占锁的执行权限
    *
    * @return 执行权限标识
    * @throws KeeperException
    * @throws InterruptedException
    */
  @throws[Exception]
  def getMonopolyLock: Boolean = {
    var flag = false
    if (connectionState != null && (connectionState.equals("CONNECTED") || connectionState.equals("RECONNECTED"))) {
      val nodes = zkTools.getChildren.watched.forPath(monopoly)
      if (nodes.size > 0) {
        Collections.sort(nodes)
        //是否在队列的第一位
        if ((separator + monopoly + separator + nodes.get(0)).equals(monopolyQueueNode)) flag = true
      }
    }
    flag
  }

}

object ZookeeperFactory{
  val config:Config = ConfigFactory.load()
  def apply(monopoly: String): ZookeeperFactory = {
    val zookeeperFactory = new ZookeeperFactory(config.getString("zookeeper.hosts"), monopoly)
    zookeeperFactory.addListener()
    zookeeperFactory
  }
}
