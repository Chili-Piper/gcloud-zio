package com.chilipiper.gcloud.pubsub.codec

import com.google.pubsub.v1.PubsubMessage

/**
 * Encoder of custom data into pub sub messages.
 */
trait PubSubEncoder[A] {
  def encode(a: A): PubsubMessage

  final def contramap[B](f: B => A): PubSubEncoder[B] = { b =>
    encode(f(b))
  }
}

object PubSubEncoder {
  @inline def apply[A](implicit ev: PubSubEncoder[A]): PubSubEncoder[A] = ev
}

/**
 * Decoder of pub sub messages into custom type.
 */
trait PubSubDecoder[A] {
  def decode(a: PubsubMessage): PubSubDecoder.Result[A]

  final def map[B](f: A => B): PubSubDecoder[B] = { b =>
    decode(b).map(f)
  }
}

object PubSubDecoder {
  type Result[A] = Either[Error, A]

  @inline def apply[A](implicit ev: PubSubDecoder[A]): PubSubDecoder[A] = ev

  final case class Error(message: String) extends Exception(s"Failed deserializing payload - $message")
}

trait PubSubCodec[A] extends PubSubEncoder[A] with PubSubDecoder[A]

object PubSubCodec {
  @inline def apply[A](implicit ev: PubSubCodec[A]): PubSubCodec[A] = ev
}
