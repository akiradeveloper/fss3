package akashic.storage.patch

import java.nio.file.Path

case class Version(root: Path) extends Patch {
  def key = Key(root.getParent.getParent)
  val data = Data(root.resolve("data"))
  val meta = Data(root.resolve("meta"))
  val acl = Data(root.resolve("acl"))
}
