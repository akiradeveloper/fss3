package akasha

import java.nio.file.{Path, Paths, Files}

import com.typesafe.config.{Config, ConfigFactory}

trait ServerConfig {
  def mountpoint: Path
  def ip: String
  def port: Int
}

object ServerConfig {
  def forProduction = ???

  def forTest = {
    val configRoot = ConfigFactory.load
    forConfig(configRoot)
  }

  def forConfig(configRoot: Config) = new ServerConfig {
    val config = configRoot.getConfig("akasha")

    val mp = Paths.get(config.getString("mountpoint"))
    if (Files.exists(mp)) {
      files.purgeDirectory(mp)
    }
    Files.createDirectory(mp)

    override def mountpoint = mp
    override def ip = config.getString("ip")
    override def port: Int = config.getInt("port")
  }
}
