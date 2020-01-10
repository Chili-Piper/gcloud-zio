package com.chilipiper.gcloud.pubsub

import com.google.api.core.ApiFuture
import zio._
import zio.blocking.Blocking
import zio.interop.javaz

import scala.language.implicitConversions


package object interop {
  implicit class FutureOps(private val taskObj: Task.type) extends AnyVal {
    def fromApiFuture[A](f: => ApiFuture[A]): RIO[Blocking, A] = javaz.fromFutureJava(UIO(f))
  }
}
