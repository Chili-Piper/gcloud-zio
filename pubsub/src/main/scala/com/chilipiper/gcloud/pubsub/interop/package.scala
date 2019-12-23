package com.chilipiper.gcloud.pubsub

import zio._
import zio.interop.javaz

import com.google.api.core.ApiFuture
import zio.blocking.Blocking


package object interop {

  implicit class FutureOps(private val taskObj: Task.type) extends AnyVal {
    def fromApiFuture[A](f: () => ApiFuture[A]): RIO[Blocking, A] = javaz.fromFutureJava(UIO(f()))
  }
}
