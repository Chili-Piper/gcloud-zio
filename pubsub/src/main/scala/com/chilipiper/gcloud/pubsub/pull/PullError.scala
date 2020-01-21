package com.chilipiper.gcloud.pubsub.pull

import com.google.pubsub.v1.PubsubMessage

sealed trait PullError extends Throwable
final case class DeserializatonFailed(payload: PubsubMessage, exception: Exception) extends PullError
final case class GenericPullFailed(throwable: Throwable) extends PullError
