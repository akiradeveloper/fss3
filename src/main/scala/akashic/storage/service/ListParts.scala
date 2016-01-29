package akashic.storage.service

import akashic.storage.patch.Part
import akashic.storage.{files, server}
import akashic.storage.service.Error.Reportable
import com.twitter.finagle.http.Request
import io.finch._

import scala.xml.NodeSeq

object ListParts {
  val matcher = get(keyMatcher / paramExists("uploadId") ?
    param("uploadId") ?
    paramOption("part-number-marker").as[Int] ?
    paramOption("max-parts").as[Int] ?
    extractRequest).as[t]
  val endpoint = matcher { a: t => a.run }
  case class t(bucketName: String, keyName: String,
               uploadId: String,
               partNumberMarker: Option[Int],
               maxParts: Option[Int],
               req: Request) extends Task[Output[NodeSeq]] {
    def name = "List Parts"
    def resource = Resource.forObject(bucketName, keyName)
    def runOnce = {
      val bucket = findBucket(server.tree, bucketName)
      val key = findKey(bucket, keyName)
      val upload = findUpload(key, uploadId)

      val startNumber = partNumberMarker match {
        case Some(a) => a
        case None => 0
      }

      val listMaxLen = maxParts match {
        case Some(a) => a
        case None => 1000
      }

      val emitList0 = upload.listParts
        .dropWhile (_.id < startNumber)

      val truncated = emitList0.size > listMaxLen

      val emitList: Seq[Part] = emitList0.take(listMaxLen)

      val nextPartNumberMarker = emitList.lastOption match {
        case Some(a) => a.id
        case None => 0
      }

      val acl = Acl.fromBytes(upload.acl.readBytes)

      val ownerId = acl.owner

      // If the initiator is an AWS account, this element provides the same information as the Owner element.
      // If the initiator is an IAM User, then this element provides the user ARN and display name.
      val initiatorId = ownerId

      def xmlPart(part: Part): NodeSeq = {
        val filePath = part.unwrap.filePath
        <Part>
          <PartNumber>{part.id}</PartNumber>
          <LastModified>{dates.format000Z(files.lastDate(filePath))}</LastModified>
          <ETag>{files.computeMD5(filePath)}</ETag>
          <Size>{files.fileSize(filePath)}</Size>
        </Part>
      }

      val xml =
        <ListPartsResult>
          <Bucket>{bucketName}</Bucket>
          <Key>{keyName}</Key>
          <UploadId>{uploadId}</UploadId>
          <Initiator>
            <ID>{initiatorId}</ID>
            <DisplayName>{server.users.getUser(initiatorId).get.displayName}</DisplayName>
          </Initiator>
          <Owner>
            <ID>{ownerId}</ID>
            <DisplayName>{server.users.getUser(ownerId).get.displayName}</DisplayName>
          </Owner>
          <StorageClass>STANDARD</StorageClass>
          { partNumberMarker match { case Some(a) => <PartNumberMarker>{a}</PartNumberMarker>; case None => NodeSeq.Empty } }
          <NextPartNumberMarker>{nextPartNumberMarker}</NextPartNumberMarker>
          { maxParts match { case Some(a) => <MaxParts>{a}</MaxParts>; case None => NodeSeq.Empty } }
          <IsTruncated>{truncated}</IsTruncated>
          { for (part <- emitList) yield xmlPart(part) }
        </ListPartsResult>

      Ok(xml)
        .withHeader(X_AMZ_REQUEST_ID -> requestId)
    }
  }
}
