package akasha.service

trait Task[T] {
  def doRun: T
  def run: T = {
    try {
      doRun
    } catch {
      case e: Error.Exception => throw e
      case _: Throwable => run
    }
  }
}