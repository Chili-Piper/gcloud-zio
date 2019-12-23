package com.chilipiper.gcloud.pubsub.interop

import com.google.api.gax.rpc.AlreadyExistsException
import io.grpc.Status
import zio.ZIO

object GrpcInterops {

  implicit class ZIOGrpc[R, E, A](private val zio: ZIO[R, E, A]) extends AnyVal {
    /** throwing exists exception is fine */
    def ignoreIfExists: ZIO[R, E, Option[A]] =
      zio.map(x => Some(x))
        .catchSome {
          case _: AlreadyExistsException => ZIO.succeed(None)
          case e: io.grpc.StatusException if e.getStatus == Status.ALREADY_EXISTS => ZIO.succeed(None)
        }
  }

}
