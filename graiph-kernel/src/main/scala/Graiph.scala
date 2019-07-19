package cn.graiph.engine

import java.io.File
import java.util.Optional

import org.apache.commons.io.IOUtils
import org.neo4j.blob.utils.Logging
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.server.CommunityBootstrapper

import scala.collection.JavaConversions

/**
  * Created by bluejoe on 2019/7/17.
  */
object Graiph extends Logging {
  val logo = IOUtils.toString(this.getClass.getClassLoader.getResourceAsStream("logo.txt"), "utf-8");

  def printLogo(): Unit = {
    println(logo);
  }

  GraiphDBInjection.touch;

  def openDatabase(dbDir: File, propertiesFile: File): GraphDatabaseService = {
    val builder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(dbDir);
    builder.loadPropertiesFromFile(propertiesFile.getPath);

    logger.info(s"loading configuration from $propertiesFile");
    //bolt server is not required
    builder.setConfig("dbms.connector.bolt.enabled", "false");
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