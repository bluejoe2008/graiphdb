package cn.graiph.storage

import java.io.File

import scala.collection.JavaConversions._
import scala.concurrent.forkjoin.ForkJoinPool
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder.ModifyableColumnFamilyDescriptor
import org.apache.hadoop.hbase.client.TableDescriptorBuilder.ModifyableTableDescriptor
import org.apache.hadoop.hbase.client._
import org.neo4j.blob.{BlobId,Blob}
import org.neo4j.blob.utils.Logging
import org.neo4j.kernel.impl.Configuration
import org.neo4j.kernel.impl.blob.BlobStorage

import cn.graiph.storage.util.HBaseUtils



class HBaseBlobValueStorage  extends BlobStorage with Logging {
  private var _table: Table = _
  private var conn: Connection = _
  private var _hbaseUtil:HBaseUtils = _

  override def initialize(storeDir: File, conf: Configuration): Unit = {
    // connect to hbase
    val hbaseConf = HBaseConfiguration.create()
    val zkQ = conf.getRaw("blob.storage.hbase.zookeeper.quorum").getOrElse("localhost")
    val zkNode = conf.getRaw("blob.storage.hbase.zookeeper.znode.parent").getOrElse("/hbase-unsecure")

    hbaseConf.set("hbase.zookeeper.quorum", zkQ)
    hbaseConf.set("zookeeper.znode.parent", zkNode)
    HBaseAdmin.available(hbaseConf)

    //val columnCount = conf.getRaw("blob.storage.hbase.table.column_count").getOrElse("100").toInt
    //    _hbaseUtil = new HBaseUtils(columnCount) //暂不提供配置
    _hbaseUtil = new HBaseUtils()

    logger.info("successfully initial the connection to the zookeeper")

    // get HTable to save blob
    val tableNameStr = conf.getRaw("blob.storage.hbase.table").getOrElse("BlobTable")
    val tableName = TableName.valueOf(tableNameStr)
    conn = ConnectionFactory.createConnection(hbaseConf)
    val admin = conn.getAdmin
    if (!admin.tableExists(tableName)) {
      if(conf.getRaw("blob.storage.hbase.auto_create_table").getOrElse("false").toBoolean){
        admin.createTable(new ModifyableTableDescriptor(tableName)
          .setColumnFamily(new ModifyableColumnFamilyDescriptor(_hbaseUtil.columnFamily)))
        logger.info(s"table created: $tableName")
      }
      else{
        logger.error(s"table is not existed: $tableName")
        throw new Exception("Error: Table is not existed.")
      }

    }
    _table = conn.getTable(tableName, ForkJoinPool.commonPool())
    if(_table.getDescriptor().getColumnFamily(_hbaseUtil.columnFamily) == null){
      admin.addColumnFamily(tableName,new ModifyableColumnFamilyDescriptor(_hbaseUtil.columnFamily))
    }
    logger.info(s"table is gotten : $tableName")
  }

  override def disconnect(): Unit = {
    _table.close()
    conn.close()
    logger.info(s"HBase connect is closed")
  }


  private def generateId(): BlobId = {
    _hbaseUtil.generateId()
  }

  override def saveBatch(blobs: Iterable[Blob]) = {
    blobs.map(blob => {
      save(blob)
    } )
  }

  override def save(blob: Blob): BlobId = {
    val bid = generateId()
    _table.put(_hbaseUtil.buildPut(blob, bid))
    logger.debug(s"saved blob: ${bid.asLiteralString()}")
    bid;
  }


  override def loadBatch(ids: Iterable[BlobId]): Iterable[Option[Blob]] = {
    ids.map(id => load(id));
  }


  override def load(bid: BlobId): Option[Blob] = {
    val res = _table.get(_hbaseUtil.buildBlobGet(bid))
    if (!res.isEmpty) {
      val blob = _hbaseUtil.buildBlobFromGetResult(res).head._2
      logger.debug(s"loaded blob: ${bid.asLiteralString()}")
      Some(blob)
    }
    else None
  }

  override def deleteBatch(ids: Iterable[BlobId]) = {
    ids.foreach { id =>
      delete(id)
    }
  }

  override def delete(blobId: BlobId): Unit = {
    _table.delete(_hbaseUtil.buildDelete(blobId))
    logger.debug(s"deleted blob: ${blobId.asLiteralString()}");
  }


  override def iterator(): Iterator[(BlobId, Blob)] = {
    val rsc: ResultScanner = _table.getScanner(_hbaseUtil.buildScan())
    var blobs = List[(BlobId,Blob)]()
    rsc.map(rs => {
      blobs =  blobs::: _hbaseUtil.buildBlobFromGetResult(rs)

    })
    blobs.iterator
  }
}