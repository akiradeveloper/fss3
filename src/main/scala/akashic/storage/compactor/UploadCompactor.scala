package akashic.storage.compactor

import akashic.storage.Server
import akashic.storage.patch.Upload

case class UploadCompactor(unwrap: Upload) extends Compactable {
  override def compact = {
    unwrap.listParts.filter(_.committed).map(PartCompactor(_))
  }
}