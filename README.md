<!-- vscode-markdown-toc -->
* [Building GraiphDB](#BuildingGraiphDB)
* [Quick start](#Quickstart)
	* [STEP 1. download package](#STEP1.downloadpackage)
	* [STEP 2. start server](#STEP2.startserver)
	* [STEP 3. connect remote GraiphServer](#STEP3.connectremoteGraiphServer)
	* [STEP 4. querying on GraiphDB](#STEP4.queryingonGraiphDB)
* [CypherPlus](#CypherPlus)
	* [BLOB literals](#BLOBliterals)
	* [property extration](#propertyextration)
	* [semantic comparison](#semanticcomparison)
* [developers' manual](#developersmanual)
	* [connecting remote GraiphServer](#connectingremoteGraiphServer)
	* [using embedded graiph database](#usingembeddedgraiphdatabase)
* [handling BLOBs](#handlingBLOBs)

<!-- vscode-markdown-toc-config
	numbering=false
	autoSave=true
	/vscode-markdown-toc-config -->
<!-- /vscode-markdown-toc -->

<img src="https://github.com/cas-bigdatalab/graiphdb/blob/master/docs/logo.png?raw=true" width=300>

[![GitHub releases](https://img.shields.io/github/release/grapheco/graiph-dist.svg)](https://github.com/grapheco/graiph-dist/releases)
[![GitHub downloads](https://img.shields.io/github/downloads/grapheco/graiph-dist/total.svg)](https://github.com/grapheco/graiph-dist/releases)
[![GitHub issues](https://img.shields.io/github/issues/grapheco/graiphdb.svg)](https://github.com/grapheco/graiphdb/issues)
[![GitHub forks](https://img.shields.io/github/forks/grapheco/graiphdb.svg)](https://github.com/grapheco/graiphdb/network)
[![GitHub stars](https://img.shields.io/github/stars/grapheco/graiphdb.svg)](https://github.com/grapheco/graiphdb/stargazers)
[![GitHub license](https://img.shields.io/github/license/grapheco/graiphdb.svg)](https://github.com/grapheco/graiphdb/blob/master/LICENSE)

# GraiphDB
GraiphDB is an AI native graph database.

GraiphDB uses graiph-neo4j, which enables Neo4j with BLOB handling functions.

## <a name='BuildingGraiphDB'></a>Building GraiphDB

```
mvn clean install
```

this will install all artifacts in local maven repository.

## <a name='Quickstart'></a>Quick start

### <a name='STEP1.downloadpackage'></a>STEP 1. download package
visit https://github.com/grapheco/graiph-dist/releases to get GraiphDB binary distributions.

unpack `graiph-server-x.x.zip` in your local directory, e.g. `/usr/local/`.

`cd /usr/local/graiph-server-x.x`

### <a name='STEP2.startserver'></a>STEP 2. start server

* `bin/neo4j console`: start a graiph server
* `bin/neo4j start`: start a graiph server silently

Note: Once successfully startup, you will see following infos:

```
[17:12:24:866] DEBUG CypherInjection$     :: injecting cypher expression extensions...
2019-07-31 09:12:25.339+0000 WARN  WARNING! Deprecated configuration options used. See manual for details
2019-07-31 09:12:25.341+0000 WARN  dbms.allow_format_migration has been replaced with dbms.allow_upgrade.
2019-07-31 09:12:25.342+0000 WARN  dbms.active_database is deprecated.
2019-07-31 09:12:25.353+0000 INFO  ======== Graiph (Neo4j-3.5.6-BLOB) ========
   _____             _____       _     _____  ____
  / ____|      /\   |_   _|     | |   |  __ \|  _ \
 | |  __ _ __ /  \    | |  _ __ | |__ | |  | | |_) |
 | | |_ | '__/ /\ \   | | | '_ \| '_ \| |  | |  _ <
 | |__| | | / ____ \ _| |_| |_) | | | | |__| | |_) |
  \_____|_|/_/    \_\_____| .__/|_| |_|_____/|____/
                          | |
                          |_|
```

### <a name='STEP3.connectremoteGraiphServer'></a>STEP 3. connect remote GraiphServer

clients communicate with GraiphServer via `Cypher` over Bolt protocol.

* `bin/cypher-shell`: open a graiph client to a remote server

Also, you may visit `http://localhost:7474`  to browse graph data in `neo4j-browser`.

### <a name='STEP4.queryingonGraiphDB'></a>STEP 4. querying on GraiphDB

in `cypher-shell` or `neo4j-browser`, users can input `Cypher` commands to query on GraiphDB.

```
create (bluejoe:Person {name: 'bluejoe', mail:'bluejoe2008@gmail.com', photo: <https://bluejoe2008.github.io/bluejoe3.png>, car: <http://s5.sinaimg.cn/middle/005AE7Quzy7rL9U08Pa24&690>}) 

```
this command will create a Person node with a BLOB property, which content come from the Web URL. If you like, `<file://...>` or `<ftp://...>` is ok.

the `match` command returns:
```
neo4j@<default_database>> match (n) return n;
+--------------------------------------------------------------------------------------------------------------------------------+
| n                                                                                                                              |
+--------------------------------------------------------------------------------------------------------------------------------+
| (:Person {name: "bluejoe", photo: BoltBlobValue(blob=blob(length=250159,mime-type=image/png)), mail: "bluejoe2008@gmail.com", car: BoltBlobValue(blob=blob(length=36379,mime-type=image/jpeg))}) |
+--------------------------------------------------------------------------------------------------------------------------------+
```

in `neo4j-browser`, a BLOB property will be displayed as an image icon:

<img src="https://github.com/cas-bigdatalab/graiphdb/blob/master/docs/neo4j-browser.png?raw=true" width="500">

NOTE: if user/password is required, try default values: `neo4j`/`neo4j`.

## <a name='CypherPlus'></a>CypherPlus

GraiphDB enhances `Cypher` grammar, naming CypherPlus. CypherPlus allows writing BLOB literals in query commands, also it allows semantic operations on properties, especially BLOB properties.

### <a name='BLOBliterals'></a>BLOB literals

`BlobLiteral` is defined in Cypher grammar in form of:
`<schema://path>`

Following schema is ok:
* file: local files in Neo4j server
* http
* https
* ftp
* base64: path should be a BASE64 encoding string, for example: \<base64://dGhpcyBpcyBhbiBleGFtcGxl\> represents a string with content `this is an example`

Next code illustrates how to use blob in Cypher query:
```
return <https://bluejoe2008.github.io/bluejoe3.png>
```

more details, see https://github.com/bluejoe2008/graiph-neo4j/blob/cypher-extension/README.md

### <a name='propertyextration'></a>property extration

```
neo4j@<default_database>> match (n {name:'bluejoe'}) return n.photo->mime, n.car->width;
+------------------------------+
| n.photo->mime | n.car->width |
+------------------------------+
| "image/png"   | 640          |
+------------------------------+
```
retrieving plate number of the car:
```
neo4j@<default_database>> match (n {name:'bluejoe'}) return n.car->plateNumber;
+--------------------+
| n.car->plateNumber |
+--------------------+
| "苏B56789"          |
+--------------------+
```

NOTE: some semantic operation requires an AIPM service at 10.0.86.128 (modify this setting in neo4j.conf), if it is unavailable, exceptions will be thrown:

```
neo4j@<default_database>> match (n {name:'bluejoe'}) return n.car->plateNumber;
Failed connect to http://10.0.86.128:8081
```

### <a name='semanticcomparison'></a>semantic comparison

CypherPlus allows semantic comparison on two properties.

Following example query compares two text:
```
neo4j@<default_database>> return 'abc' :: 'abcd', 'abc' ::jaccard 'abcd', 'abc' ::jaro 'abcd', 'hello world' ::cosine 'bye world';
+--------------------------------------------------------------------------------------------------------+
| 'abc' :: 'abcd'    | 'abc' ::jaccard 'abcd' | 'abc' ::jaro 'abcd' | 'hello world' ::cosine 'bye world' |
+--------------------------------------------------------------------------------------------------------+
| 0.9416666805744172 | 0.5                    | 0.9416666805744172  | 0.5039526306789696                 |
+--------------------------------------------------------------------------------------------------------+
```

A good idea is to determine if a person appear in another photo:

```
return <http://s12.sinaimg.cn/mw690/005AE7Quzy7rL8kA4Nt6b&690> ~:0.5 <http://s15.sinaimg.cn/mw690/005AE7Quzy7rL8j2jlIee&690>
```

## <a name='developersmanual'></a>developers' manual
### <a name='connectingremoteGraiphServer'></a>connecting remote GraiphServer

import `graiph-client-all` dependency first:
```
   <dependency>
      <groupId>com.github.grapheco</groupId>
      <artifactId>graiph-client-all</artifactId>
      <version>0.1.0</version>
   </dependency>
```
use object `RemoteGraiph.connect()`:

* def connect(url: String, user: String = "", pass: String = ""): CypherService

`CypherService` has a set of methods:
* def queryObjects[T: ClassTag](queryString: String, fnMap: (Record => T)): Iterator[T];

* def execute[T](f: (Session) => T): T;

* def executeQuery[T](queryString: String, fn: (StatementResult => T)): T;

* def executeQuery[T](queryString: String, params: Map[String, AnyRef], fn: (StatementResult => T)): T;

* def executeUpdate(queryString: String);

* def executeUpdate(queryString: String, params: Map[String, AnyRef]);

* def executeQuery[T](queryString: String, params: Map[String, AnyRef])

* def querySingleObject[T](queryString: String, fnMap: (Record => T)): T

* def querySingleObject[T](queryString: String, params: Map[String, AnyRef], fnMap: (Record => T)): T

A simple example:
```
    val conn = RemoteGraiph.connect("bolt://localhost:7687", "neo4j", "123");
    //a non-blob
    val (node, name, age) = conn.querySingleObject("match (n) where n.name='bob' return n, n.name, n.age", (result: Record) => {
      (result.get("n").asNode(), result.get("n.name").asString(), result.get("n.age").asInt())
    });
```

more example code, see https://github.com/grapheco/graiph-dist/tree/master/graiph-client-test

### <a name='usingembeddedgraiphdatabase'></a>using embedded graiph database

import `graiph-database-all` dependency first:

```
   <dependency>
      <groupId>com.github.grapheco</groupId>
      <artifactId>graiph-database-all</artifactId>
      <version>0.1.0</version>
   </dependency>
```

using object `GraiphDB`:

* def openDatabase(dbDir: File, propertiesFile: File): GraphDatabaseService

*  def connect(dbs: GraphDatabaseService): CypherService

An example of `openDatabase`:
```
   val db = GraiphDB.openDatabase(new File("./testdb"), new File("./neo4j.conf"));
   val tx = db.beginTx();
   //create a node
   val node1 = db.createNode();

   node1.setProperty("name", "bob");
   node1.setProperty("age", 40);

   //with a blob property
   node1.setProperty("photo", Blob.fromFile(new File("./testdata/test.png")));
   ...
```

An example of `connect`:
```
   val db = GraiphDB.openDatabase(new File("./testdb"), new File("./neo4j.conf"));
   val conn = GraiphDB.connect(db);
   //a non-blob
    val (node, name, age) = conn.querySingleObject("match (n) where n.name='bob' return n, n.name, n.age", (result: Record) => {
      (result.get("n").asNode(), result.get("n.name").asString(), result.get("n.age").asInt())
    });
```
YES! `GraiphDB.connect()` returns a `CypherService` too, just like that of `RemoteGraiph.connect()`.

more example code, see https://github.com/grapheco/graiph-dist/tree/master/graiph-database-test

## <a name='handlingBLOBs'></a>handling BLOBs

graiph-neo4j enhances Neo4j with a set of blob operation functions which makes it possible and convenient to store and use the BLOB in neo4j.

more details, see https://github.com/bluejoe2008/graiph-neo4j/blob/cypher-extension/blob.md