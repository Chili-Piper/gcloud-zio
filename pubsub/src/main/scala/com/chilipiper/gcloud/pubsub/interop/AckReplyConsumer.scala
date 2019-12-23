package com.chilipiper.gcloud.pubsub.interop

import com.google.cloud.pubsub.v1.{AckReplyConsumer => GAckReplyConsumer}
import zio._
import zio.blocking._

final case class AckReplyConsumer(ack: RIO[Blocking, Unit], nack: RIO[Blocking, Unit])

object AckReplyConsumer {
  def fromGoogle(x: GAckReplyConsumer): AckReplyConsumer = {
    val ack = effectBlocking(x.ack())
    val nack = effectBlocking(x.nack())
    AckReplyConsumer(ack, nack)
  }

}
