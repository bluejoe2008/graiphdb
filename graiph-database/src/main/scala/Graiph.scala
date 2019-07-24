package cn.graiph.engine

import java.io.File
import java.util.Optional

import cn.graiph.{CustomPropertyProvider, CypherPluginRegistry, ValueMatcher}
import org.apache.commons.io.IOUtils
import org.neo4j.blob.utils.Logging
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.kernel.impl.blob.{BlobPropertyStoreServiceContext, BlobPropertyStoreServicePlugin, BlobPropertyStoreServicePlugins}
import org.neo4j.server.CommunityBootstrapper
import org.springframework.context.support.FileSystemXmlApplicationContext

import scala.collection.JavaConversions

/**
  * Created by bluejoe on 2019/7/17.
  */
object Graiph extends Logging {
  val logo = IOUtils.toString(this.getClass.getClassLoader.getResourceAsStream("logo.txt"), "utf-8");

  BlobPropertyStoreServicePlugins.add(new SemanticOperatorPlugin());

  def printLogo(): Unit = {
    println(logo);
  }

  CypherInjection.touch;

  def openDatabase(dbDir: File, propertiesFile: File): GraphDatabaseService = {
    val builder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(dbDir);
    builder.loadPropertiesFromFile(propertiesFile.getPath);

    logger.info(s"loading configuration from $propertiesFile");
    //bolt server is not required
    builder.setConfig("dbms.connector.bolt.enabled", "false");
    builder.setConfig("config.file.path", propertiesFile.getAbsolutePath);
    builder.newGraphDatabase();
  }

  //TODO: CypherService over GraphDatabaseService
  def connect(dbs: GraphDatabaseService): CypherService = {
    new LocalGraphService(dbs);
  }

  def connect(url: String, user: String = "", pass: String = ""): CypherService = {
    new BoltService(url, user, pass);
  }

  def startServer(dbDir: File, configFile: File, configOverrides: Map[String, String] = Map()): GraiphServer = {
    val server = new GraiphServer(dbDir, configFile, configOverrides);
    server.start();
    server;
  }
}

class GraiphServer(dbDir: File, configFile: File, configOverrides: Map[String, String] = Map()) {
  val server = new CommunityBootstrapper();

  def start(): Unit = {
    Graiph.printLogo();
    server.start(dbDir, Optional.of(configFile),
      JavaConversions.mapAsJavaMap(configOverrides + ("config.file.path" -> configFile.getAbsolutePath)));
  }

  def shutdown(): Unit = {
    server.stop();
  }
}

class SemanticOperatorPlugin extends BlobPropertyStoreServicePlugin with Logging {
  override def init(ctx: BlobPropertyStoreServiceContext): Unit = {
    val configuration = ctx.configuration;
    val cypherPluginRegistry = configuration.getRaw("blob.plugins.conf").map(x => {
      val xml = new File(x);

      val path =
        if (xml.isAbsolute) {
          xml.getPath
        }
        else {
          val configFilePath = configuration.getRaw("config.file.path")
          if (configFilePath.isDefined) {
            new File(new File(configFilePath.get).getParentFile, x).getAbsoluteFile.getCanonicalPath
          }
          else {
            xml.getAbsoluteFile.getCanonicalPath
          }
        }

      logger.info(s"loading semantic plugins: $path");
      val appctx = new FileSystemXmlApplicationContext("file:" + path);
      appctx.getBean[CypherPluginRegistry](classOf[CypherPluginRegistry]);
    }).getOrElse(new CypherPluginRegistry());

    val customPropertyProvider = cypherPluginRegistry.createCustomPropertyProvider(configuration);
    val valueMatcher = cypherPluginRegistry.createValueComparatorRegistry(configuration);

    ctx.instanceContext.put[CustomPropertyProvider](customPropertyProvider);
    ctx.instanceContext.put[ValueMatcher](valueMatcher);
  }

  override def stop(ctx: BlobPropertyStoreServiceContext): Unit = {

  }

  override def start(ctx: BlobPropertyStoreServiceContext): Unit = {

  }
}