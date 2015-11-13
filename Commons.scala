import java.util.HashSet
import java.util.ArrayList

sealed trait TweetMessage
case object Tweet extends TweetMessage
case object RTweet extends TweetMessage
case object Dummy extends TweetMessage
case object Stats extends TweetMessage
case class Tweet(tweet: String) extends TweetMessage
case class Addfollower(follow: Int) extends TweetMessage
case class TweetFromUser(tweet: String, userID: String) extends TweetMessage
case class RTweetFromUser(userID: String) extends TweetMessage
case class GetUserHomePage(userID: String) extends TweetMessage
case class GetMentionsPage(userID: String) extends TweetMessage
case class GetUserActivity(userID: String) extends TweetMessage
case class GetUserFollowers(userID: String) extends TweetMessage
case object GetStats extends TweetMessage
case class GetFollowersToUpdate(senderID: String, tid: Long, parResult: ParseReturn) extends TweetMessage
case object GetFollowing extends TweetMessage
case class GetHomePage(senderID: String) extends TweetMessage
case class GHomePage(senderID: String) extends TweetMessage
case class GActivity(senderID: String) extends TweetMessage
case class GMentions(senderID: String) extends TweetMessage
case class GFollowers(senderID: String) extends TweetMessage
case object GetMentions extends TweetMessage
case class GetNextTID(senderID: String, tweetString: String) extends TweetMessage
case class GTweets(list: ArrayList[Long]) extends TweetMessage
case class SendTID(senderID: String, tid: Long, tweetString: String) extends TweetMessage
case class SendFollowersToUpdate(senderID: String, tid: Long, parResult: ParseReturn, set: HashSet[Int]) extends TweetMessage
case class UpdateMentionsNGetFollowers(senderID: String, tid: Long, set: HashSet[Int]) extends TweetMessage
case class SendFollowing(set: HashSet[Int]) extends TweetMessage
case class HomePage(senderID: String, list: ArrayList[Long]) extends TweetMessage
case class UpdateUserHome(tid: Long) extends TweetMessage
case class UpdateUserMentions(tID: Long) extends TweetMessage
case class SendInterSectionSet(senderID: String, tid: Long, intersectionSet: HashSet[Int]) extends TweetMessage
case class UpdateUserHomeNActivity(tid: Long) extends TweetMessage
case class UpdateMasterMap(tid: Long, tweet: String) extends TweetMessage
case class UpdateUserInfo(senderID: String, tid: Long, parResult: ParseReturn) extends TweetMessage
case class UpdateUserInfoRT(senderID: String) extends TweetMessage
case class UpdateFollowing(noofFollowing: Int, totalUsers: Int) extends TweetMessage
case class UpdateFollowers(noofFollowers: Int, totalUsers: Int) extends TweetMessage
case class GetUserInfo(userID: String) extends TweetMessage
case class UserDetails(info: UserInfo) extends TweetMessage
case object GetUserDetails extends TweetMessage

