package com.chilipiper.gcloud

import com.google.api.gax.core.BackgroundResource
import zio.{Task, UIO}

package object pubsub {
  def shutdown[A <: BackgroundResource]: A => UIO[Unit] =
    resource => Task(resource.shutdown()).ignore

  def shutdown[A](shutdown: A => Unit): A => UIO[Unit] =
    resource => Task(shutdown(resource)).ignore
}
