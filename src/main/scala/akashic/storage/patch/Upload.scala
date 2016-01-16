package akashic.storage.patch

import java.nio.file.{Files, Path}

case class Upload(root: Path) extends Patch {
  val parts = root.resolve("parts")
  def partPath(n: Int): Path = parts.resolve(n.toString)
  def part(n: Int) = Part(partPath(n))
  val meta = Data(root.resolve("meta"))
  val acl = Data(root.resolve("acl"))
  override def init {
    Files.createDirectory(parts)
    Files.createDirectory(meta.root)
    meta.init
    Files.createDirectory(acl.root)
    acl.init
  }
  def reservedVersionId: Int = {
    root.toString.split("-")(0).toInt
  }
  def findPart(partNumber: Int): Option[Part] = {
    val path = partPath(partNumber)
    if (Files.exists(path) && Part(path).committed) {
      Some(Part(path))
    } else {
      None
    }
  }
}
