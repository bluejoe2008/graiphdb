
import java.io.File

import org.apache.commons.io.FileUtils
import org.neo4j.blob.BlobFactory
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.kernel.impl.InstanceContext
import org.neo4j.kernel.impl.blob.BlobStorage

/**
  * Created by bluejoe on 2019/4/13.
  */
trait TestBase {
  val testBaseDir = "./graiph-blob-storage/"
  val testDbDir = new File(testBaseDir+"/testdata/testdb");
  val testConfPath = new File(testBaseDir+"/testdata/neo4j.conf").getPath;


  def setupNewDatabase(dbdir: File = testDbDir, conf: String = testConfPath): Unit = {
    FileUtils.deleteDirectory(dbdir);
    //create a new database
    val db = openDatabase(dbdir, conf);
    //clear blob storage
    val blobStorage = InstanceContext.of(db).get[BlobStorage]
    blobStorage.iterator().foreach(x=>{blobStorage.delete(x._1)})

    val tx = db.beginTx();
    //create a node
    val node1 = db.createNode();

    node1.setProperty("name", "bob");
    node1.setProperty("age", 40);

    //with a blob property
    node1.setProperty("photo", BlobFactory.fromFile(new File(testBaseDir+"/testdata/test.png")));
    //blob array
    node1.setProperty("album", (0 to 5).map(x => BlobFactory.fromFile(new File(testBaseDir+"/testdata/test.png"))).toArray);

    val node2 = db.createNode();
    node2.setProperty("name", "alex");
    //with a blob property
    node2.setProperty("photo", BlobFactory.fromFile(new File(testBaseDir+"/testdata/test1.png")));
    node2.setProperty("age", 10);

    //node2.createRelationshipTo(node1, RelationshipType.withName("dad"));

    tx.success();
    tx.close();
    db.shutdown();
  }

  def setupNewDatabaseWithoutData(dbdir: File = testDbDir, conf: String = testConfPath): Unit = {
    FileUtils.deleteDirectory(dbdir);
    //create a new database
    val db = openDatabase(dbdir, conf);
    //clear blob storage
    val blobStorage = InstanceContext.of(db).get[BlobStorage]
    blobStorage.iterator().foreach(x=>{blobStorage.delete(x._1)})
    db.shutdown();
  }

  def openDatabase(dbdir: File = testDbDir, conf: String = testConfPath): GraphDatabaseService = {
    val builder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(dbdir);
    builder.loadPropertiesFromFile(conf);
    //bolt server is not required
    builder.setConfig("dbms.connector.bolt.enabled", "false");
    builder.newGraphDatabase();

  }
}
