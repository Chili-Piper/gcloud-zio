package googlepubsubz

import com.chilipiper.gcloud.pubsub.SubscriptionAdminClient
import com.chilipiper.gcloud.pubsub.pull.Subscriber
import com.google.pubsub.v1.{ProjectSubscriptionName, ProjectTopicName, PushConfig}
import zio.{App, IO, ZEnv, ZIO}
import zio.duration._

object PullSubscribeTopic extends App {
  val Topic = ProjectTopicName.of("kadekm-push-test", "my-topic")
  val Sub = ProjectSubscriptionName.of("kadekm-push-test", "subscription1")

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {

    val adminSubProg = SubscriptionAdminClient.default.use(subscribe)
    val subProg = Subscriber.subscribe(Sub).use(pull)

    val prog = adminSubProg.ignore *> subProg

    prog.either.map {
      case Left(err) =>
        err.printStackTrace()
        -1
      case Right(_) => 0
    }
  }

  private def subscribe(client: SubscriptionAdminClient) = {
    client.createSubscription(Sub, Topic, PushConfig.newBuilder.build(), 10.seconds)
  }

  // process 3 elements in parallel
  private def pull(subscriber: Subscriber) = {
    subscriber.stream.mapMPar(3) { x =>
      ZIO.sleep(1500.millis) *> // fake long running process
        IO.effectTotal(println(x.message.getData.toStringUtf8)) *>
        x.reply.ack
    }
    .runDrain
  }

}
