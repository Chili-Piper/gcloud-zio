package com.chilipiper.gcloud.pubsub.codec

import com.google.pubsub.v1.PubsubMessage

/**
 * Encoder of custom data into pub sub messages.
 */
trait PubSubEncoder[A] {
  def encode(a: A): PubsubMessage

  final def contramap[B](f: B => A): PubSubEncoder[B] = PubSubEncoder[B] { b =>
    encode(f(b))
  }
}

object PubSubEncoder {
  def apply[A](f: A => PubsubMessage): PubSubEncoder[A] = new PubSubEncoder[A] {
    def encode(a: A): PubsubMessage = f(a)
  }
}

/**
 * Decoder of pub sub messages into custom type.
 */
trait PubSubDecoder[A] {
  def decode(a: PubsubMessage): Either[String, A]

  final def map[B](f: A => B): PubSubDecoder[B] = PubSubDecoder[B] { b =>
    decode(b).map(f)
  }
}

object PubSubDecoder {
  def apply[A](f: PubsubMessage => Either[String, A]): PubSubDecoder[A] = new PubSubDecoder[A] {
    def decode(a: PubsubMessage): Either[String, A] = f(a)
  }
}

trait PubSubCodec[A] extends PubSubEncoder[A] with PubSubDecoder[A]