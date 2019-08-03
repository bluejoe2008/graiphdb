<img src="https://github.com/cas-bigdatalab/graiphdb/blob/master/docs/logo.png?raw=true" width=300>

# GraiphDB
an AI native graph database, GraiphDB uses graiph-neo4j, which enables Neo4j with BLOB handling fuctions.

# Building GraiphDB

```
mvn clean install
```

this will install all artifacts in local maven repository.

# Quick start

## STEP 1: download package
visit https://github.com/grapheco/graiph-dist/releases to get GraiphDB binary distributions.

unpack `graiph-server-x.x.zip` in your local directory, e.g. `/usr/local/`.

`cd /usr/local/graiph-server-x.x`

## STEP 2: start server

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

## STEP 3: connect remote GraiphServer

clients communicate with GraiphServer via Cypher over Bolt protocol.

* `bin/cypher-shell`: open a graiph client to a remote server

Also, you may visit `http://localhost:7474`  to browse graph data in `neo4j-browser`.

## querying GraiphDB

in `cypher-shell` or `neo4j-browser`, users can input Cypher commands to query GraiphDB.

```
create (bluejoe:Person {name: 'bluejoe', mail:'bluejoe2008@gmail.com', photo: <https://bluejoe2008.github.io/bluejoe3.png>, car: <http://s5.sinaimg.cn/middle/005AE7Quzy7rL9U08Pa24&690>}) 

match (n) return n
```
this command will create a Person node with a BLOB property, which content come from the Web URL. If you like, `<file://...>` or `<ftp://...>` is ok.

the `match` command returns:
```
+--------------------------------------------------------------------------------------------------------------------------------+
| n                                                                                                                              |
+--------------------------------------------------------------------------------------------------------------------------------+
| (:Person {name: "bluejoe", photo: BoltBlobValue(blob=blob(length=250159,mime-type=image/png)), mail: "bluejoe2008@gmail.com", car: BoltBlobValue(blob=blob(length=36379,mime-type=image/jpeg))}) |
+--------------------------------------------------------------------------------------------------------------------------------+
```

in `neo4j-browser`, a BLOB property will be displayed as an image icon:

<img src="https://github.com/cas-bigdatalab/graiphdb/blob/master/docs/neo4j-browser.png?raw=true" width="500">

NOTE: if user/password is required, try default values: `neo4j`/`neo4j`.

# CypherPlus

GraiphDB enhances Cypher grammar, naming CypherPlus. CypherPlus allows writing BLOB literals in query commands, also it allows semantic operations on properties, especially BLOB properties.

## BLOB literal

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

## property extration

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

## semantic comparison

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

# APIs: connecting remote GraiphServer

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

NOTE: you may download `graiph-connector-0.1.jar` from https://github.com/grapheco/graiph-dist/releases first.

# APIs: using embedded graiph database

using object `GraiphDB`:

* def openDatabase(dbDir: File, propertiesFile: File): GraphDatabaseService

*  def connect(dbs: GraphDatabaseService): CypherService

# handling BLOBs

graiph-neo4j enhances Neo4j with a set of blob operation functions which makes it possible and convenient to store and use the BLOB in neo4j.

more details, see https://github.com/bluejoe2008/graiph-neo4j/blob/cypher-extension/README.md