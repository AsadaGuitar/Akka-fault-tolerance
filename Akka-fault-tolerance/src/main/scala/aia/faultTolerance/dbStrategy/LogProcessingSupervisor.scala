package aia.faultTolerance

package dbStrategy{

  import aia.faultTolerance.dbStrategy.ExceptionDefine.{ColumnNotFoundException, DbBrokenConnectionException, DbNodeDownException, DiskError}
  import akka.actor.SupervisorStrategy.Stop
  import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, AllForOneStrategy, OneForOneStrategy, Props, SupervisorStrategy, Terminated, actorRef2Scala}



  object LogProcessingApp extends App{
    implicit val system: ActorSystem = ActorSystem()

    val logProcessingSupervisor =
      system.actorOf(LogProcessingSupervisor.props(Vector(
        "jdbc:mysql://localhost:3306/failure",
        "jdbc:mysql://localhost:3306/failure",
        "jdbc:mysql://localhost:3306/scala"
        )))

    logProcessingSupervisor ! LogProcessingSupervisor.Items(Seq("Hello","World"))
  }

  object LogProcessingSupervisor{
    def props(urlList: Vector[String]): Props =
      Props(new LogProcessingSupervisor(urlList))
    case class Items(items: Seq[String])
  }

  class LogProcessingSupervisor(urlList: Vector[String]) extends Actor{
    import LogProcessingSupervisor._

    val logProcessor: ActorRef =
      context.actorOf(LogProcessor.props(urlList))
    context.watch(logProcessor)

    override def supervisorStrategy: SupervisorStrategy = AllForOneStrategy(){
      case _: DiskError => Stop
    }
    override def receive: Receive = {
      case Items(items) => logProcessor ! LogProcessor.AddItems(items)
      case Terminated(_) => context.system.terminate() //logProcessorの停止を受信
    }
  }


  object LogProcessor {
    def props(urlList: Vector[String]): Props = Props(new LogProcessor(urlList))
    case class AddItems(items: Seq[String])
  }

  class LogProcessor(urlList: Vector[String]) extends Actor with ActorLogging {
    import LogProcessor._

    val url: String = urlList.head
    var tails: Vector[String] = urlList.tail

    //子アクターが例外、エラーを出した際に開始
    override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
      case _: DbNodeDownException => Stop
      case _: ColumnNotFoundException => Stop
      case _: DbBrokenConnectionException => Stop //Restartの場合はアクターシステムから取り除かれるだけで終了しない為Stopを選択
    }

    var writer: ActorRef = context.actorOf(DbAccessory.props(url))
    context.watch(writer)

    var ItemsBox: Seq[String] = _

    def sendItems (items: Seq[String]): Unit = {
      items.map(x => DbAccessory.create(x)).foreach(writer ! _)
    }
    //通常フロー
    override def receive: Receive = {
      case AddItems(items) =>
        ItemsBox = items
        sendItems(ItemsBox)
      case Terminated(actorRef) =>
        log.warning(s"Actor Terminated $actorRef")
        if(tails.nonEmpty){
          val newUrl = tails.head
          tails = tails.tail
          writer = context.actorOf(DbAccessory.props(newUrl))
          context.watch(writer)
          self ! AddItems(ItemsBox)
        }
        else {
          log.info("All Database url is Could not able.")
          throw new DiskError
        }

    }
  }

  object DbAccessory {
    def props(url: String): Props = Props(new DbAccessory(url))
    case class create(item: String)
  }

  class DbAccessory(url: String) extends Actor {
    import DbAccessory._

    val con: ItemsDbAccessory = new ItemsDbAccessory(url)

    override def receive: Receive = {
      case create(item) => sender() ! con.create(item)
    }
    override def postStop(): Unit ={
      if(con.connection.isDefined) {
        con.close()
      }
    }
  }


}


