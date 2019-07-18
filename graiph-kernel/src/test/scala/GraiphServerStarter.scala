import java.io.File

import cn.graiph.engine.Graiph

/**
  * Created by bluejoe on 2019/7/17.
  */
object GraiphServerStarter {
  def main(args: Array[String]) {
    Graiph.startServer(new File("./testdata/testdb/data/databases/graph.db"),
      new File("./testdata/neo4j.conf"));
  }
}
