package com.chilipiper.gcloud.pubsub

import com.google.protobuf.ByteString
import com.google.pubsub.v1.{ProjectTopicName, PubsubMessage}
import zio._

object PrepareTopic extends App {
  val Topic = ProjectTopicName.of("kadekm-push-test", "my-topic")

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {

    val topicProg = TopicAdminClient.default
      .use(prepareTopic)

    val publishProg = PublisherClient(Topic)
      .use(publish)

    val prog =
      topicProg.ignore *>
        publishProg

    prog.either.map {
      case Left(err) =>
        err.printStackTrace()
        -1
      case Right(_) => 0
    }
  }

  private def prepareTopic(client: TopicAdminClient) = {
    client.createTopic(Topic)
  }

  private def publish(publisher: PublisherClient) = {
    val xs = (1 to 30).map { id =>
      val data = ByteString.copyFromUtf8(s"Message with id $id")
      val msg = PubsubMessage.newBuilder().setData(data).build()
      publisher.publish(msg)
    }
    ZIO.collectAll(xs)
  }
}
