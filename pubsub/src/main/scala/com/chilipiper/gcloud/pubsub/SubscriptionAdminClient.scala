package com.chilipiper.gcloud.pubsub

import com.google.cloud.pubsub.v1.{SubscriptionAdminSettings, SubscriptionAdminClient => GSubscriptionAdminClient}
import com.google.pubsub.v1.{ProjectSubscriptionName, ProjectTopicName, PushConfig, Subscription}
import com.chilipiper.gcloud.pubsub.interop.GrpcInterops._
import zio.blocking._
import zio.duration._
import zio._


/**
 * Client for managing Subscriptions
 */
trait SubscriptionAdminClient {
  def settings: SubscriptionAdminSettings
  def createSubscription(name: ProjectSubscriptionName, topic: ProjectTopicName, pushConfig: PushConfig, ackTimeout: Duration): RIO[Blocking, Option[Subscription]]
  def deleteSubscription(name: ProjectSubscriptionName): RIO[Blocking, Unit]
  def modifyPushConfig(subscription: ProjectSubscriptionName, pushConfig: PushConfig): RIO[Blocking, Unit]
}

object SubscriptionAdminClient {
  def default: ZManaged[Any, Throwable, SubscriptionAdminClient] = apply(SubscriptionAdminSettings.newBuilder.build)

  def apply(settings: SubscriptionAdminSettings): ZManaged[Any, Throwable, SubscriptionAdminClient] = {
    val acq = ZIO.effect(GSubscriptionAdminClient.create(settings))
    val rel = (x: GSubscriptionAdminClient) => ZIO.effect(x.shutdown()).ignore

    ZManaged.make(acq)(rel).map { client =>
      new SubscriptionAdminClient {
        def settings: SubscriptionAdminSettings = client.getSettings

        def createSubscription(name: ProjectSubscriptionName, topic: ProjectTopicName, pushConfig: PushConfig, duration: Duration): RIO[Blocking, Option[Subscription]] = {
          effectBlocking(client.createSubscription(name, topic, pushConfig, duration.asScala.toSeconds.toInt))
            .ignoreIfExists
        }

        def deleteSubscription(name: ProjectSubscriptionName): RIO[Blocking, Unit] = {
          effectBlocking(client.deleteSubscription(name))
        }

        def modifyPushConfig(subscription: ProjectSubscriptionName, pushConfig: PushConfig): RIO[Blocking, Unit] = {
          effectBlocking(client.modifyPushConfig(subscription, pushConfig))
        }
      }
    }
  }
}
