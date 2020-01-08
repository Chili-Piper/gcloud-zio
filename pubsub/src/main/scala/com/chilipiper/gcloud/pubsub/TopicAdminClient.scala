package com.chilipiper.gcloud.pubsub

import com.google.cloud.pubsub.v1.{TopicAdminSettings, TopicAdminClient => GTopicAdminClient}
import com.google.iam.v1.Policy
import com.google.pubsub.v1.{ProjectTopicName, Topic}
import zio._
import zio.blocking._

/**
 * Client for managing topics
 */
trait TopicAdminClient {
  def createTopic(topic: Topic): RIO[Blocking, Topic]
  def createTopic(topic: ProjectTopicName): RIO[Blocking, Topic]
  def deleteTopic(topic: ProjectTopicName): RIO[Blocking, Unit]
  def setIamPolicy(resource: String, policy: Policy): RIO[Blocking, Policy]
  def settings: TopicAdminSettings
}

object TopicAdminClient {

    def default: ZManaged[Any, Throwable, TopicAdminClient] = {
      apply(TopicAdminSettings.newBuilder.build)
    }

    def apply(settings: TopicAdminSettings): ZManaged[Any, Throwable, TopicAdminClient] = {
      val acq = ZIO.effect(GTopicAdminClient.create(settings))
      val rel = shutdown[GTopicAdminClient]

      ZManaged.make(acq)(rel).map { client =>
        new TopicAdminClient {
          def settings: TopicAdminSettings = {
            client.getSettings
          }

          def createTopic(topic: Topic): RIO[Blocking, Topic] = {
            effectBlocking(client.createTopic(topic))
          }

          def createTopic(topic: ProjectTopicName): RIO[Blocking, Topic] = {
            effectBlocking(client.createTopic(topic))
          }

          def setIamPolicy(resource: String, policy: Policy): RIO[Blocking, Policy] = {
            effectBlocking(client.setIamPolicy(resource, policy))
          }

          def deleteTopic(topic: ProjectTopicName): RIO[Blocking, Unit] = {
            effectBlocking(client.deleteTopic(topic))
          }
        }
      }
    }
}

