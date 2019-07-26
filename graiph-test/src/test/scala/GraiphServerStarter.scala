import java.io.File

import cn.graiph.engine.GraiphDB

/**
  * Created by bluejoe on 2019/7/17.
  */
object GraiphServerStarter {
  def main(args: Array[String]) {
    GraiphDB.startServer(new File("./testdata/testdb"),
      new File("./testdata/neo4j.conf"));
  }
}
