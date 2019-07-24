
import java.io.{File, FileInputStream}

import org.apache.commons.io.IOUtils
import org.neo4j.graphdb.Node
import org.neo4j.kernel.impl.InstanceContext
import org.neo4j.kernel.impl.blob.BlobStorage
import org.neo4j.blob.{Blob}
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.collection.JavaConversions

class GraphDbBlobMassTest extends FunSuite with BeforeAndAfter with TestBase {

  before {
    setupNewDatabaseWithoutData();
  }


  test("test muti blob R/W/D using API") {
    val nodeNum = 50;

    //load database
    val db2 = openDatabase();

    val tx1 = db2.beginTx();
    //create 100 node, each node has 5 blob properties

    for(i<- 1 to nodeNum){
      val node1 = db2.createNode();
      node1.setProperty("name", "bob"+i.toString);
      node1.setProperty("age", 40);
      node1.setProperty("sex", "男");
      //with 5 blob property
      node1.setProperty("photo1", Blob.fromFile(new File(testBaseDir+"/testdata/test.png")));
      node1.setProperty("photo2", Blob.fromFile(new File(testBaseDir+"/testdata/test1.png")));
      node1.setProperty("photo3", Blob.fromFile(new File(testBaseDir+"/testdata/test2.jpg")));
      node1.setProperty("desc1", Blob.fromFile(new File(testBaseDir+"/testdata/test.csv")));
      node1.setProperty("desc2", Blob.fromFile(new File(testBaseDir+"/testdata/test.csv")));

      tx1.success();
    }
    tx1.close();

    assert(InstanceContext.of(db2).get[BlobStorage].iterator().size == nodeNum*5);


    val tx2 = db2.beginTx();
    val it = db2.getAllNodes().iterator();
    for(i <- 101 to 110){
      val v1: Node = it.next();
      v1.delete();
      tx2.success();
    }
    tx2.close();

    assert(InstanceContext.of(db2).get[BlobStorage].iterator().size == (nodeNum-10)*5);


    val tx3 = db2.beginTx();
    val it3 = db2.getAllNodes().iterator();
    for(i <- 1 to 10){
      val v1: Node = it3.next();
      v1.setProperty("photo5", Blob.fromFile(new File(testBaseDir+"/testdata/test.png")));
      v1.setProperty("photo6", Blob.fromFile(new File(testBaseDir+"/testdata/test1.png")));
      tx3.success();
    }
    tx3.close();

    assert(InstanceContext.of(db2).get[BlobStorage].iterator().size == (nodeNum-10)*5+20);


    val tx4 = db2.beginTx();
    val it4 = db2.getAllNodes().iterator();
    for(i <- 10 to 14){
      val v1: Node = it4.next();
      val blob = v1.getProperty("photo1").asInstanceOf[Blob];

      assert(new File(testBaseDir+"/testdata/test.png").length() == blob.length);

      assert(new File(testBaseDir+"/testdata/test.png").length() == blob.offerStream {
        IOUtils.toByteArray(_).length
      });

      assert(IOUtils.toByteArray(new FileInputStream(
        new File(s"$testBaseDir/testdata/test.png"))) ===
        blob.toBytes());
      tx4.success();
    }
    tx4.close();


    db2.shutdown();
  }

}
