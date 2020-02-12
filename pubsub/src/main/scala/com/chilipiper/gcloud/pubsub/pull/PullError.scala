package com.chilipiper.gcloud.pubsub.pull

import com.chilipiper.gcloud.pubsub.interop.AckReplyConsumer
import com.google.pubsub.v1.PubsubMessage

sealed trait PullError extends Exception
final case class DeserializatonFailed(payload: PubsubMessage, reply: AckReplyConsumer, exception: Exception) extends PullError
final case class GenericPullFailed(throwable: Throwable) extends PullError
