package cn.aipm.test
import org.neo4j.blob.utils.Configuration

class ConfigTemp extends Configuration{
  override def getRaw(name: String): Option[String] = {
    val configs = Map("aipm.http.host.url"->"http://aipm-host:8081/")
    configs.get(name)
  }
}

class TestBase {
  val config = new ConfigTemp()
}
