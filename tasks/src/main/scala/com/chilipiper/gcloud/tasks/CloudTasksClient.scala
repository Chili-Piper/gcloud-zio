package com.chilipiper.gcloud.tasks

import com.google.cloud.tasks.v2.{CloudTasksClient => JCloudTasksClient, _}
import com.google.iam.v1._
import zio.blocking._
import zio.{Queue => _, Task => _, _}
import scala.collection.JavaConverters._

trait CloudTasksClient {
  def createQueue(request: CreateQueueRequest): ZIO[Blocking, Throwable, Queue]
  def getQueue(request: GetQueueRequest): ZIO[Blocking, Throwable, Queue]
  def updateQueue(request: UpdateQueueRequest): ZIO[Blocking, Throwable, Queue]
  def deleteQueue(request: DeleteQueueRequest): ZIO[Blocking, Throwable, Unit]
  def purgeQueue(request: PurgeQueueRequest): ZIO[Blocking, Throwable, Queue]
  def pauseQueue(request: PauseQueueRequest): ZIO[Blocking, Throwable, Queue]
  def resumeQueue(request: ResumeQueueRequest): ZIO[Blocking, Throwable, Queue]
  def listQueues(request: ListQueuesRequest): ZIO[Blocking, Throwable, List[Queue]]

  def getIamPolicy(request: GetIamPolicyRequest): ZIO[Blocking, Throwable, Policy]
  def setIamPolicy(request: SetIamPolicyRequest): ZIO[Blocking, Throwable, Policy]
  def testIamPermissions(request: TestIamPermissionsRequest): ZIO[Blocking, Throwable, TestIamPermissionsResponse]

  def getTask(request: GetTaskRequest): ZIO[Blocking, Throwable, Task]
  def createTask(request: CreateTaskRequest): ZIO[Blocking, Throwable, Task]
  def deleteTask(request: DeleteTaskRequest): ZIO[Blocking, Throwable, Unit]
  def runTask(request: RunTaskRequest): ZIO[Blocking, Throwable, Task]
  def listTasks(request: ListTasksRequest): ZIO[Blocking, Throwable, List[Task]]

  def isShutdown: ZIO[Any, Throwable, Boolean]
  def isTerminated: ZIO[Any, Throwable, Boolean]
}

object CloudTasksClient {

  def apply(settings: CloudTasksSettings): ZManaged[Any, Throwable, CloudTasksClient] = {
    val acq = ZIO.effect(JCloudTasksClient.create(settings))
    val rel = shutdown[JCloudTasksClient]((x:JCloudTasksClient) => x.shutdown())
    ZManaged.make(acq)(rel)
      .map { client =>
        val ctc: CloudTasksClient = new CloudTasksClient {
          def createQueue(request: CreateQueueRequest): ZIO[Blocking, Throwable, Queue] = effectBlocking(client.createQueue(request))
          def getQueue(request: GetQueueRequest): ZIO[Blocking, Throwable, Queue] = effectBlocking(client.getQueue(request))
          def updateQueue(request: UpdateQueueRequest): ZIO[Blocking, Throwable, Queue] = effectBlocking(client.updateQueue(request))
          def deleteQueue(request: DeleteQueueRequest): ZIO[Blocking, Throwable, Unit] = effectBlocking(client.deleteQueue(request))
          def purgeQueue(request: PurgeQueueRequest): ZIO[Blocking, Throwable, Queue] = effectBlocking(client.purgeQueue(request))
          def pauseQueue(request: PauseQueueRequest): ZIO[Blocking, Throwable, Queue] = effectBlocking(client.pauseQueue(request))
          def resumeQueue(request: ResumeQueueRequest): ZIO[Blocking, Throwable, Queue] = effectBlocking(client.resumeQueue(request))
          def listQueues(request: ListQueuesRequest): ZIO[Blocking, Throwable, List[Queue]] = effectBlocking(client.listQueues(request).iterateAll.asScala.toList)

          def getIamPolicy(request: GetIamPolicyRequest): ZIO[Blocking, Throwable, Policy] = effectBlocking(client.getIamPolicy(request))
          def setIamPolicy(request: SetIamPolicyRequest): ZIO[Blocking, Throwable, Policy] = effectBlocking(client.setIamPolicy(request))
          def testIamPermissions(request: TestIamPermissionsRequest): ZIO[Blocking, Throwable, TestIamPermissionsResponse] = effectBlocking(client.testIamPermissions(request))

          def getTask(request: GetTaskRequest): ZIO[Blocking, Throwable, Task] = effectBlocking(client.getTask(request))
          def createTask(request: CreateTaskRequest): ZIO[Blocking, Throwable, Task] = effectBlocking(client.createTask(request))
          def deleteTask(request: DeleteTaskRequest): ZIO[Blocking, Throwable, Unit] = effectBlocking(client.deleteTask(request))
          def runTask(request: RunTaskRequest): ZIO[Blocking, Throwable, Task] = effectBlocking(client.runTask(request))
          def listTasks(request: ListTasksRequest): ZIO[Blocking, Throwable, List[Task]] = effectBlocking(client.listTasks(request).iterateAll.asScala.toList)

          def isShutdown: ZIO[Any, Throwable, Boolean] = ZIO.effect(client.isShutdown)
          def isTerminated: ZIO[Any, Throwable, Boolean] = ZIO.effect(client.isTerminated)
        }
        ctc
      }
  }

  def default: ZManaged[Any, Throwable, CloudTasksClient] = apply(CloudTasksSettings.newBuilder.build)

}
