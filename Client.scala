import akka.actor._
import akka.actor.Actor
import akka.actor.Props
import scala.concurrent.duration._
import spray.client.pipelining.sendReceive
import spray.client.pipelining.sendReceive$default$3
import com.typesafe.config.ConfigFactory
import java.net.InetAddress
import akka.routing.RoundRobinRouter
import scala.util.Random
import scala.util.Random
import akka.actor._
import scala.math._
import scala.util.Random
import java.net.InetAddress
import akka.actor.Props
import akka.actor.Actor
import com.typesafe.config.ConfigFactory
import akka.routing.RoundRobinRouter
import scala.concurrent.duration._
import java.util._
import spray.client.pipelining.Post
import spray.client.pipelining.Get
import java.util.concurrent.TimeUnit
import scala.concurrent.Future
import spray.http.HttpResponse

object Client {

  def main(args: Array[String]) {
    val hostname = InetAddress.getLocalHost.getHostName

    implicit val system = ActorSystem("TwitterEngine")

    val noofUsers: Int = args(0).toInt
    val port: String = args(2).toString
    val ip: String = args(1).toString
       
    for(i<-1 to 10)
     system.actorOf(Props(new UserNode(ip,port,noofUsers,10,10)),name = "userNode"+i)   

    val statsCollector = system.actorOf(Props(new StatsCollector(ip,port)), name = "statsCollector")

    println("Stats collector created!!")
    
  }
}

class StatsCollector(ip:String,port:String) extends Actor {
  import context._
  private var scheduler: Cancellable = _

  override def preStart(): Unit = {
    import scala.concurrent.duration._

    scheduler = context.system.scheduler.schedule(Duration.create(100, TimeUnit.MILLISECONDS),
      Duration.create(1000, TimeUnit.MILLISECONDS), self, Stats)

  }

  override def postStop(): Unit = {
    scheduler.cancel()
  }
  implicit val system = context.system
  import system.dispatcher
  val pipeline = sendReceive

  def receive = {
    case Stats => {
      val reply: Future[HttpResponse] = pipeline(Get("http://"+ip+":"+port+"/statistics"))
      reply.foreach(
        response=>
         println(s"No.of Tweets :\n${response.entity.asString}")                                              
      )
    }
  }
}

class UserNode(ip:String,port:String, noofUsers: Int, startTime: Int, IntervalDuration: Int) extends Actor {
  var text: String = "cnasnoiasnflkasnfoiwqjfklanvoiasjvasknvcp9akvlkacnasnoiasnflkasnfoiwqjfklanvoiasjvasknvcp9akvlkacnasnoiasnflkasnfoiwqjfklanvoiasjvasknvcp9akvlkacnasnoiasnflkasnfoiwqjfklanvoiasjvasknvcp9akvlkacnasnoiasnflkasnfoiwqjfklanvoiasjvasknvcp9akvlkacnasnoiasnflkasnfoiwqjfklanvoiasjvasknvcp9akvlkacnasnoiasnflkasnfoiwqjfklanvoiasjvasknvcp9akvlkacnasnoiasnflkasnfoiwqjfklanvoiasjvasknvcp9akvlkacnasnoiasnflkasnfoiwqjfklanvoiasjvasknvcp9akvlkacnasnoiasnflkasnfoiwqjfklanvoiasjvasknvcp9akvlkacnasnoiasnflkasnfoiwqjfklanvoiasjvasknvcp9akvlkacnasnoiasnflkasnfoiwqjfklanvoiasjvasknvcp9akvlkacnasnoiasnflkasnfoiwqjfklanvoiasjvasknvcp9akvlkacnasnoiasnflkasnfoiwqjfklanvoiasjvasknvcp9akvlkacnasnoiasnflkasnfoiwqjfklanvoiasjvasknvcp9akvlkacnasnoiasnflkasnfoiwqjfklanvoiasjvasknvcp9akvlka"
  import context._
  private var scheduler: Cancellable = _

  override def preStart(): Unit = {
    import scala.concurrent.duration._

    scheduler = context.system.scheduler.schedule(Duration.create(startTime, TimeUnit.MILLISECONDS),
      Duration.create(IntervalDuration, TimeUnit.MILLISECONDS), self, Tweet)

  }

  override def postStop(): Unit = {
    scheduler.cancel()
  }

  implicit val system = context.system
  import system.dispatcher
  val pipeline = sendReceive
  var user = Random.nextInt(noofUsers)
  def receive = {
    case Tweet => {
      var start: Int = 0
      var end: Int = 0
      var i: Int = Random.nextInt(3)
      var tweet: String = ""
      (i) match {
        case 0 => {
          start = 1 + util.Random.nextInt(text.length() - 141)
          end = start + util.Random.nextInt(70)
          tweet = text.substring(start, end);
        }
        case 1 => {
          var rand: Int = 0
          do {
            rand = util.Random.nextInt(noofUsers)
          } while (rand != user)
          start = 1 + util.Random.nextInt(text.length() - 141)
          end = start + util.Random.nextInt(70 - rand.toString.length() - 3)
          tweet = "@" + rand.toString + "+" + text.substring(start, end);
        }
        case 2 => {
          var rand: Int = 0
          do {
            rand = util.Random.nextInt(noofUsers)
          } while (rand != user)
          start = 1 + util.Random.nextInt(text.length() - 141)
          end = start + util.Random.nextInt(70 - rand.toString.length() - 3)
          tweet = text.substring(start, end) + "@" + rand.toString;
        }
        case 3 => {
          // proxy ! RTweet
        }
      }
      pipeline(Post("http://"+ip+":"+port+"/tweet?msg=" + tweet + "&userID=" + user))
    }

    case Dummy => {
      var i: Int = Random.nextInt(4)
      (i) match {
        case 0 => {
          pipeline(Get("http://"+ip+":"+port+"/HomePage?userID=" + self.path.name))
        }
        case 1 => {
          pipeline(Get("http://"+ip+":"+port+"/Mentions?userID=" + self.path.name))
        }
        case 2 => {
          pipeline(Get("http://"+ip+":"+port+"/Activity?userID=" + self.path.name))
        }
        case 3 => {
          pipeline(Get("http://"+ip+":"+port+"/followers?userID=" + self.path.name))
        }
      }
    }

  }

}
