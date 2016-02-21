package akashic.storage.service

import java.nio.file.Files

import akashic.storage.server
import akashic.storage.patch._
import akashic.storage.service.Acl.Grant
import akashic.storage.service.Error.Reportable
import akka.http.scaladsl.model.{StatusCodes, HttpEntity, HttpRequest}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

object PutBucket {
  val matcher =
    put &
    extractBucket &
    optionalHeaderValueByName("x-amz-acl") &
    extractGrantsFromHeaders &
    extractRequest
  val route = matcher.as(t)(_.run)
  case class t(bucketName: String,
               cannedAcl: Option[String],
               grantsFromHeaders: Iterable[Grant],
               req: HttpRequest) extends AuthorizedAPI {
    def name = "PUT Bucket"
    def resource = Resource.forBucket(bucketName)
    def runOnce = {
      val dest = server.tree.bucketPath(bucketName)

      if (Files.exists(dest)) {
        val bucket = findBucket(server.tree, bucketName)
        val bucketAcl = Acl.fromBytes(bucket.acl.read)
        if (bucketAcl.owner == callerId) {
          failWith(Error.BucketAlreadyOwnByYou())
        } else {
          failWith(Error.BucketAlreadyExists())
        }
      }

      Commit.once(dest) { patch =>
        val bucketPatch = patch.asBucket
        bucketPatch.init

        Commit.replaceData(bucketPatch.acl) { patch =>
          val dataPatch = patch.asData
          val grantsFromCanned = (cannedAcl <+ Some("private")).map(Acl.CannedAcl.forName(_, callerId, callerId)).map(_.makeGrants).get
          dataPatch.write(Acl.t(callerId, grantsFromCanned ++ grantsFromHeaders).toBytes)
        }

        Commit.replaceData(bucketPatch.versioning) { patch =>
          val dataPatch = patch.asData
          dataPatch.write(Versioning.t(Versioning.UNVERSIONED).toBytes)
        }
      }

      val headers = ResponseHeaderList.builder
        .withHeader(X_AMZ_REQUEST_ID, requestId)
        .build

      complete(StatusCodes.OK, headers, HttpEntity.Empty)
    }
  }
}
