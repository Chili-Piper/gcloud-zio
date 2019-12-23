package com.chilipiper.gcloud.pubsub

import com.google.api.gax.batching.BatchingSettings
import com.google.cloud.pubsub.v1.Publisher
import com.google.pubsub.v1.{PubsubMessage, TopicName}
import com.chilipiper.gcloud.pubsub.codec.PubSubEncoder
import zio.{RIO, Task, ZIO, ZManaged}
import interop._
import zio.blocking.Blocking

/**
 * Client for publishing GCloud native message type.
 */
trait PublisherClient {
  def publish(message: PubsubMessage): RIO[Blocking, String]
  def topicName: TopicName
  def batchingSettings: BatchingSettings
}

  object PublisherClient {
    def apply(topicName: TopicName): ZManaged[Blocking, Throwable, PublisherClient] = {
      val acq = ZIO.effect(Publisher.newBuilder(topicName).build())
      val rel = (x: Publisher) => ZIO.effect(x.shutdown()).ignore

      ZManaged.make(acq)(rel)
        .map { publisher =>
          new PublisherClient {
            def publish(message: PubsubMessage): RIO[Blocking, String] = {
              Task.fromApiFuture(() => publisher.publish(message))
            }

            def topicName: TopicName = {
              publisher.getTopicName
            }

            def batchingSettings: BatchingSettings = {
              publisher.getBatchingSettings
            }

            def publishAllOutstanding: Task[Unit] = {
              Task.effect(publisher.publishAllOutstanding())
            }
          }
        }
    }
  }

/**
 * Typed publisher for messages
 */
trait PublisherClientTyped[-A] {
  def publish(message: A): RIO[Blocking, String]
  def topicName: TopicName
  def batchingSettings: BatchingSettings
}

object PublisherClientTyped {
  def make[A: PubSubEncoder](topicName: TopicName): ZManaged[Blocking, Throwable, PublisherClientTyped[A]] = {
    for {
      untyped <- PublisherClient(topicName)
    } yield fromPublisherClient(untyped, topicName)
  }

  def fromPublisherClient[A](publisherClient: PublisherClient, topicName: TopicName)(implicit ev: PubSubEncoder[A]): PublisherClientTyped[A] = {
    new PublisherClientTyped[A] {
      override def publish(message: A): RIO[Blocking, String] = {
        val encoded = ev.encode(message)
        publisherClient.publish(encoded)
      }

      override def topicName: TopicName = publisherClient.topicName
      override def batchingSettings: BatchingSettings = publisherClient.batchingSettings
    }
  }

}
