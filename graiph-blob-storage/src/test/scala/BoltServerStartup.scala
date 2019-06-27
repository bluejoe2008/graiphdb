
import java.io.File
import java.util.Optional

import org.neo4j.server.CommunityBootstrapper

import scala.collection.JavaConversions

/**
  * Created by bluejoe on 2019/4/17.
  */
object BoltServerStartup {
  def main(args: Array[String]) {
    val server = new CommunityBootstrapper();
    val test = new BoltCypherTest();
    test.setupNewDatabase(new File("./testdata/testdb/data/databases/graph.db"));

    server.start(test.testDbDir, Optional.of(new File(test.testConfPath)),
      JavaConversions.mapAsJavaMap(Map("config.file.path" -> new File(test.testConfPath).getAbsolutePath)));
  }
}
