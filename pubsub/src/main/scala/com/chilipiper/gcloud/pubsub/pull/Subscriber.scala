package com.chilipiper.gcloud.pubsub.pull

import com.chilipiper.gcloud.pubsub.codec.PubSubDecoder
import com.chilipiper.gcloud.pubsub.interop._
import com.chilipiper.gcloud.pubsub.pull.Subscriber.Payload
import com.chilipiper.gcloud.pubsub.pull.SubscriberTyped.PayloadTyped
import com.google.api.core.ApiService
import com.google.cloud.pubsub.v1.{MessageReceiver, AckReplyConsumer => GAckReplyConsumer, Subscriber => GSubscriber}
import com.google.pubsub.v1.{ProjectSubscriptionName, PubsubMessage}
import zio._
import zio.blocking._
import zio.stream._

/**
 * Live subscription providing streaming capabilities
 */
trait Subscriber {
  def stream: ZStream[Any, Nothing, Payload]
  def state: UIO[ApiService.State]
  def subscriptionName: ProjectSubscriptionName
}

object Subscriber {

  type PayloadQueue = Queue[Payload]

  /**
   * Tuple containing message and mechanism to ack or nack the mesasge
   */
  trait Payload {
    def message: PubsubMessage
    def reply: AckReplyConsumer
  }

  /**
   * Subscribes and pulls elements into intermediary queue. Sharing resulting subscriber's
   * stream would result in competing consumers.
   */
  def subscribe(name: ProjectSubscriptionName, queueCapacity: Int = 32): ZManaged[Blocking, Throwable, Subscriber] = {
    val acq = for {
        runtime <- ZIO.runtime[Any]
        queue <- Queue.bounded[Payload](queueCapacity)
        receiver = new MessageReceiver {
          def receiveMessage(m: PubsubMessage, r: GAckReplyConsumer): Unit = {
            val payload: Payload = new Payload {
              val message: PubsubMessage = m
              val reply: AckReplyConsumer = AckReplyConsumer.fromGoogle(r)
            }
            val eff = queue.offer(payload)
            runtime.unsafeRunAsync_(eff)
          }
        }
        gs <- effectBlocking(GSubscriber.newBuilder(name, receiver).build())
        apiService <- effectBlocking(gs.startAsync())
      } yield (apiService, queue)

    val rel = (x: (ApiService, PayloadQueue)) =>
      for {
        _ <- ZIO.effect(x._1.stopAsync()).ignore
        _ <- x._2.shutdown
      } yield ()

    ZManaged.make(acq)(rel).map { case (apiService, queue) =>
      val sub: Subscriber = new Subscriber {
        def stream: ZStream[Any, Nothing, Payload] = ZStream.fromQueue(queue)
        def state: UIO[ApiService.State] = UIO(apiService.state)
        def subscriptionName: ProjectSubscriptionName = name
      }
      sub
    }
  }
}

/**
 * Typed version of subscriber, containing stream of either decoding failures or payloads
 */
trait SubscriberTyped[A] {
  def stream: ZStream[Any, PullError, PayloadTyped[A]]
  def state: UIO[ApiService.State]
  def subscriptionName: ProjectSubscriptionName
}

object SubscriberTyped {

  /**
   * Tuple containing message and mechanism to ack or nack the mesasge
   */
  trait PayloadTyped[+A] {
    def message: A
    def reply: AckReplyConsumer
  }

  /**
   * Subscribes and pulls elements into intermediary queue. Sharing resulting subscriber's
   * stream would result in competing consumers.
   */
  def subscribe[A: PubSubDecoder](name: ProjectSubscriptionName, queueCapacity: Int = 32): ZManaged[Blocking, PullError, SubscriberTyped[A]] = {
    for {
      untyped <- Subscriber.subscribe(name, queueCapacity).mapError(GenericPullFailed)
    } yield new SubscriberTyped[A] {
      override def stream: ZStream[Any, DeserializatonFailed, PayloadTyped[A]] = untyped.stream.mapM { x =>
        val decoded = PubSubDecoder[A].decode(x.message)
        val zio = decoded match {
          case Left(e) => ZIO.fail(DeserializatonFailed(x.message, e))
          case Right(m) =>
            val payload: PayloadTyped[A] = new PayloadTyped[A] {
              override def message: A = m
              override def reply: AckReplyConsumer = x.reply
            }
            ZIO.succeed(payload)
        }
        zio
      }
      override def state: UIO[ApiService.State] = untyped.state
      override def subscriptionName: ProjectSubscriptionName = untyped.subscriptionName
    }
  }
}
