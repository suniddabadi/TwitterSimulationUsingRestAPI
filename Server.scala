import java.net.InetAddress
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import scala.collection.JavaConversions._
import scala.util.Random
import akka.pattern.ask
import akka.dispatch.Futures
import com.typesafe.config.ConfigFactory
import akka.actor._
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.routing.RoundRobinRouter
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import spray.routing.SimpleRoutingApp
import spray.json._
import com.google.gson.Gson
import scala.concurrent.Await
import akka.util.Timeout

class UserInfo extends Actor {

  var followers: HashSet[Int] = new HashSet[Int]
  var following: HashSet[Int] = new HashSet[Int]
  var activity: ArrayList[Long] = new ArrayList[Long]
  var mentions: ArrayList[Long] = new ArrayList[Long]
  var homepage: ArrayList[Long] = new ArrayList[Long]
  var gson: Gson = new Gson();
  def getHomPage(): ArrayList[Long] = {
    return homepage
  }
  def getMentions(): ArrayList[Long] = {
    return mentions
  }
  def getActivity(): ArrayList[Long] = {
    return activity
  }
  def getFollowers(): HashSet[Int] = {
    return followers
  }
  def getFollowing(): HashSet[Int] = {
    return following
  }
  def addToFollowers(follow: Int) = {
    followers.add(follow)
  }
  def addToHomePage(tweetID: Long) = {
    homepage.add(tweetID)
  }
  def addToActivity(tweetID: Long) = {
    activity.add(tweetID)
  }
  def addToMentions(tweetID: Long) = {
    mentions.add(tweetID)
  }

  def receive = {

    case Addfollower(follow: Int) => {
      addToFollowers(follow)
    }
    case UpdateUserHome(tid) => {
      addToHomePage(tid)
    }
    case UpdateUserMentions(tID) => {
      addToMentions(tID)
    }
    case UpdateMentionsNGetFollowers(senderID, tid, set) => {
      addToMentions(tid)
      var interSection: HashSet[Int] = new HashSet[Int]
      for (i: Int <- set.toIterator) {
        var temp = i
        if (this.followers.contains(temp)) {
          interSection.add(temp.toInt)
        }
      }
      sender ! interSection
    }
    case UpdateUserHomeNActivity(tID) => {
      addToHomePage(tID)
      addToActivity(tID)
    }
    case UpdateFollowing(noofFollowing: Int, totalUsers: Int) => {
      for (i <- 1 to noofFollowing) {
        following.add(Random.nextInt(totalUsers))
      }
    }
    case UpdateFollowers(noofFollowers: Int, totalUsers: Int) => {
      for (i <- 1 to noofFollowers) {
        followers.add(Random.nextInt(totalUsers))
      }
    }
    case GetFollowersToUpdate(senderID, tid, parResult) => {
      sender ! getFollowers()
    }
    case GHomePage(senderID) => {
      implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))
      val master = context.system.actorFor("akka://TwitterEngine/user/" + "master")
      val future = Await.result(master ? GTweets(getHomPage()), timeout.duration).asInstanceOf[HashMap[Long, String]]
      val str: String = gson.toJson(future)
      sender ! str
    }
    case GActivity(senderID) => {
      implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))
      val master = context.system.actorFor("akka://TwitterEngine/user/" + "master")
      val future = Await.result(master ? GTweets(getActivity()), timeout.duration).asInstanceOf[HashMap[Long, String]]
      val str: String = gson.toJson(future)
      sender ! str
    }
    case GMentions(senderID) => {
      implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))
      val master = context.system.actorFor("akka://TwitterEngine/user/" + "master")
      val future = Await.result(master ? GTweets(getMentions()), timeout.duration).asInstanceOf[HashMap[Long, String]]
      val str: String = gson.toJson(future)
      sender ! str
    }
    case GFollowers(senderID) => {
      val str: String = gson.toJson(getFollowers())
      //println(str)
      sender ! str
    }

  }
}

object Server extends App with SimpleRoutingApp {

  val hostname = InetAddress.getLocalHost.getHostName

  val noofUsers = Integer.parseInt(args(0))

  println(noofUsers)

  implicit val system = ActorSystem("TwitterEngine")

  val workerRouter = system.actorOf(
    Props[WorkerActor].withRouter(RoundRobinRouter(64)), name = "workerRouter")

  //Create UserInfo Objects & Update the following lists
  // 10% of users will have  <10 followers && <10 following
  for (i <- 0 to (noofUsers / 10)) {
    var temp = system.actorOf(Props[UserInfo], name = "" + i)
    val following = 1 + Random.nextInt(10)
    val followers = 1 + Random.nextInt(10)
    temp ! UpdateFollowers(followers, noofUsers)
    temp ! UpdateFollowing(following, noofUsers)
  }
  // 10% of users will have  <100 followers && <100 following
  for (i <- (noofUsers / 10) + 1 to 2 * (noofUsers / 10)) {
    var temp = system.actorOf(Props[UserInfo], name = "" + i)
    val following = (0.02f * noofUsers).toInt + Random.nextInt(10)
    val followers = (0.02f * noofUsers).toInt + Random.nextInt(10)
    temp ! UpdateFollowers(followers, noofUsers)
    temp ! UpdateFollowing(following, noofUsers)
  }
  // 50% of users will have  <1000 followers && <1000 following
  for (i <- 2 * (noofUsers / 10) + 1 to 7 * (noofUsers / 10)) {
    var temp = system.actorOf(Props[UserInfo], name = "" + i)
    val following = (0.2f * noofUsers).toInt + Random.nextInt(25)
    val followers = (0.2f * noofUsers).toInt + Random.nextInt(25)
    temp ! UpdateFollowers(followers, noofUsers)
    temp ! UpdateFollowing(following, noofUsers)
  }
  // 20% of users will have  <2500 followers && <2500 following
  for (i <- 7 * (noofUsers / 10) + 1 to 9 * (noofUsers / 10)) {
    var temp = system.actorOf(Props[UserInfo], name = "" + i)
    val following = (0.5f * noofUsers).toInt + Random.nextInt(50)
    val followers = (0.5f * noofUsers).toInt + Random.nextInt(50)
    temp ! UpdateFollowers(followers, noofUsers)
    temp ! UpdateFollowing(following, noofUsers)
  }
  // 10% of users will have  <5000 followers && <5000 following
  for (i <- 9 * (noofUsers / 10) + 1 to noofUsers) {
    var temp = system.actorOf(Props[UserInfo], name = "" + i)
    val following = (0.9f * noofUsers).toInt + Random.nextInt(100)
    val followers = (0.9f * noofUsers).toInt + Random.nextInt(100)
    temp ! UpdateFollowers(followers, noofUsers)
    temp ! UpdateFollowing(following, noofUsers)
  }

  var noofTweets = 0
  val master = system.actorOf(Props[Master], name = "master")

  startServer(interface = "192.168.0.18", port = 1234) {
    get {
      path("twitter") {
        complete {
          "Welcome to Twitter!!"
        }
      }
    } ~
      post {
        path("addFollower") {
          parameter("userID", "follow".as[Int]) { (userID, follow) =>
            complete {
               val tempUser = system.actorFor("akka://TwitterEngine/user/" + userID)
               tempUser ! Addfollower(follow)
              "Done !!"
            }
          }
        }
      } ~
      post {
        path("tweet") {
          parameter("msg", "userID") { (msg, userID) =>
            noofTweets += 1
            complete {
              workerRouter ! TweetFromUser(msg, userID.toString)
              "Done !!"
            }
          }
        }
      } ~
      get {
        path("HomePage") {
          parameter("userID") { userID =>
            complete {
              val tempUser = system.actorFor("akka://TwitterEngine/user/" + userID)
              implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))
              val future = Await.result(tempUser ? GHomePage(userID), timeout.duration).asInstanceOf[String]
              future
            }
          }
        }
      } ~
      get {
        path("Activity") {
          parameter("userID") { userID =>
            complete {
              val tempUser = system.actorFor("akka://TwitterEngine/user/" + userID)
              implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))
              val future = Await.result(tempUser ? GActivity(userID), timeout.duration).asInstanceOf[String]
              future
            }
          }
        }
      } ~
      get {
        path("Mentions") {
          parameter("userID") { userID =>
            complete {
              val tempUser = system.actorFor("akka://TwitterEngine/user/" + userID)
              implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))
              val future = Await.result(tempUser ? GMentions(userID), timeout.duration).asInstanceOf[String]
              future
            }
          }
        }
      } ~
      get {
        path("followers") {
          parameter("userID") { userID =>
            complete {
              val tempUser = system.actorFor("akka://TwitterEngine/user/" + userID)
              implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))
              val future = Await.result(tempUser ? GFollowers(userID), timeout.duration).asInstanceOf[String]
              future
            }
          }
        }
      } ~
      get {
        path("statistics") {
          complete {
            "No.of Tweets : " + noofTweets.toString
          }
        }
      }
  }
}

class Master extends Actor {

  var TIDMap: HashMap[Long, String] = new HashMap[Long, String]
  var tid: Long = 0;
  var count: Long = 0;
  def receive = {
    case UpdateMasterMap(tid: Long, tweet: String) => {
      updateTIDMap(tid, tweet)
    }
    case GetNextTID(senderID, tweetString) => {
      var id = getNextTID()
      updateTIDMap(id, tweetString)
      sender ! id
    }
    case GTweets(list) => {
      var map: HashMap[Long, String] = new HashMap[Long, String]
      for (i: Long <- list.toIterator) {
        map.put(i, this.TIDMap.get(i))
      }
      sender ! map
    }

  }

  def getNextTID(): Long = {
    tid = tid + 1;
    return tid;
  }

  def updateTIDMap(key: Long, value: String): Unit = {
    TIDMap.put(key, value)
  }

  def getTweet(TID: Int): String = {
    return TIDMap.get(TID)
  }

}

class ParseReturn(par: Int, rep: Int) {
  var x: Int = par
  var y: Int = rep
  def getX(): Int = { return x }
  def getY(): Int = { return y }
}

class WorkerActor extends Actor {
  val worker = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(4)), name = "worker")

  def receive = {
    case TweetFromUser(tweet, userID) => {
      val master = context.system.actorFor("akka://TwitterEngine/user/master");
      implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))
      val tid = Await.result(master ? GetNextTID(userID, tweet), timeout.duration).asInstanceOf[Long]
      worker ! UpdateUserInfo(userID, tid, parseTweet(tweet))
    }
    case RTweetFromUser(userID) => {
      worker ! UpdateUserInfoRT(userID)
    }
  }

  def parseTweet(tweet: String): ParseReturn = {
    var response: ParseReturn = null
    var ch: Array[Char] = tweet.toCharArray()
    var i: Int = 0;
    if (ch.length != 0) {
      if (ch(0) == '@') {
        i = i + 1;
        while (i < ch.length && ch(i) != ' ') {
          i = i + 1;
        }
        var y: Int = 1;
        try {
          y = Integer.parseInt(tweet.substring(1, i))
        } catch {
          case e: NumberFormatException => {
            return new ParseReturn(0, 0)
          }
        }
        response = new ParseReturn(2, y) // case 2 to update the intersection
      }
      if (response == null) {
        i = 0;
        while (i < ch.length && ch(i) != '@') { i = i + 1 }
        if (i == ch.length) response = new ParseReturn(0, 0) // no mentions.
        else {
          var j = i
          while (j < ch.length && ch(j) != ' ') {
            j = j + 1;
          }
          var y: Int = 0
          try {
            y = Integer.parseInt(tweet.substring(i + 1, j))
          } catch {
            case e: NumberFormatException => {
              return new ParseReturn(0, 0);
            }
          }
          response = new ParseReturn(1, y); // case 1
        }
      }
    } else {
      response = new ParseReturn(-1, -1);
    }
    return response;
  }
}

class Worker extends Actor {
  def receive = {
    case UpdateUserInfo(senderID: String, tid: Long, parResult: ParseReturn) => {
      var sentUser = context.system.actorFor("akka://TwitterEngine/user/" + senderID)
      sentUser ! UpdateUserHomeNActivity(tid)
      var x = parResult.getX()
      if (x != -1) {
        implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))
        val set = Await.result(sentUser ? GetFollowersToUpdate(senderID, tid, parResult), timeout.duration).asInstanceOf[HashSet[Int]]
        var x = parResult.getX()
        var y = parResult.getY()
        if (x == 0 || x == 1) {
          for (i: Int <- set.toIterator) {
            var temp = i
            var tempUser = context.system.actorFor("akka://TwitterEngine/user/" + temp)
            tempUser ! UpdateUserHome(tid)
          }
        }
        val yuser = context.system.actorFor("akka://TwitterEngine/user/" + y)
        if (x == 1) {
          yuser ! UpdateUserMentions(tid)
        }
        if (x == 2) {
          implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))
          val intersectionSet = Await.result(yuser ? UpdateMentionsNGetFollowers(senderID, tid, set), timeout.duration).asInstanceOf[HashSet[Int]]
          for (i: Int <- intersectionSet.toIterator) {
            var temp = i
            var tempUser = context.system.actorFor("akka://TwitterEngine/user/" + temp)
            tempUser ! UpdateUserHome(tid)
          }
        }
      }
    }
    case UpdateUserInfoRT(senderID) => {
      var sentUser = context.system.actorFor("akka://TwitterEngine/user/" + senderID)
      implicit val timeout = Timeout(Duration(10, TimeUnit.SECONDS))
      val list = Await.result(sentUser ? GetHomePage(senderID), timeout.duration).asInstanceOf[ArrayList[Long]]
      var n = list.size()
      if (n > 0) {
        var rand = Random.nextInt(n)
        val randTID = list(rand)
        sentUser ! UpdateUserHomeNActivity(randTID)
        val parResult = new ParseReturn(0, 0)
        val set = Await.result(sentUser ? GetFollowersToUpdate(senderID, randTID, parResult), timeout.duration).asInstanceOf[HashSet[Int]]
        for (i: Int <- set.toIterator) {
          var temp = i
          var tempUser = context.system.actorFor("akka://TwitterEngine/user/" + temp)
          tempUser ! UpdateUserHome(randTID)
        }
      }
    }
  }
}
