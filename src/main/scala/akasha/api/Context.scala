package akasha.api

case class Context(tree: Tree, users: UserTable, requestId: String, callerId: Option[String], resource: String)
extends GetService {
  def failWith(e: akasha.Error.t) {
    throw akasha.Exception(self, e)
  }
}
