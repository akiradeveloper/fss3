package akashic.storage

import java.net.{URLEncoder, URLDecoder}

import akashic.storage.patch.Version
import cats.Eval
import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.http.Request
import com.twitter.io.Buf
import com.twitter.util.Future

import io.finch._
import shapeless.HNil

import scala.xml.NodeSeq

package object service {
  // first appearance wins
  implicit class _Option[A](unwrap: Option[A]) {
    def `<+`(other: Option[A]): Option[A] = 
      unwrap match {
        case Some(a) => unwrap
        case None => other
      }
  }

  implicit class _Output[A](unwrap: Output[A]) {
    def append(list: HeaderList.t): Output[A] = list.unwrap.foldLeft(unwrap) { (acc, a) => acc.withHeader(a) }
  }

  case class ParamExists(name: String) extends Endpoint[HNil] {
    private[this] val hnilFutureOutput: Eval[Future[Output[HNil]]] = Eval.now(Future.value(Output.payload(HNil)))
    def apply(input: Input): Endpoint.Result[HNil] =
      if (input.request.containsParam(name)) Some((input, hnilFutureOutput))
      else None
  }
  def paramExists(name: String): Endpoint[HNil] = ParamExists(name)

  def mkString(stream: AsyncStream[Buf]): Future[String] =
    stream.foldLeft("") { (acc, x) => acc + Buf.Utf8.unapply(x).get }

  def mkByteArray(stream: AsyncStream[Buf]): Future[Array[Byte]] =
    stream.foldLeft(Array[Byte]()) { (acc, buf) => acc ++ Buf.ByteArray.Owned.extract(buf) }

  def mkBuf(node: NodeSeq): Buf = Buf.Utf8(node.toString)

  val X_AMZ_REQUEST_ID = "x-amz-request-id"
  val X_AMZ_VERSION_ID = "x-amz-version-id"
  val X_AMZ_DELETE_MARKER = "x-amz-delete-marker"

  def quoteString(raw: String): String = s""""${raw}""""
  val extractRequest = RequestReader { req: Request => req }

  def encodeKeyName(keyName: String): String = URLEncoder.encode(keyName, "UTF-8")
  def decodeKeyName(keyName: String): String = URLDecoder.decode(keyName, "UTF-8")
  val keyMatcher = string / io.finch.strings.map(a => encodeKeyName(a.mkString("/")))
}
