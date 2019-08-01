<img src="https://github.com/cas-bigdatalab/graiphdb/blob/master/logo.png?raw=true" width=300>

# GraiphDB
an AI native graph database, GraiphDB uses graiph-neo4j, which enables Neo4j with BLOB handling fuctions.

# building GraiphDB

```
mvn clean install
```

# Quick start

## STEP 1: download
visit https://github.com/grapheco/graiph-dist/releases to get GraiphDB binary distributions:

`graiph-server-0.1.zip`

unpack it in your local directory.

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

## STEP 3: connecting remote GraiphServer

* `bin/cypher-shell`: open a graiph client to a remote server

Also, you may visit `http://localhost:7474` to browse graph data.

# APIs: connecting remote GraiphServer

use object `RemoteGraiph`:

* def connect(url: String, user: String = "", pass: String = ""): CypherService

`CypherService` has several methods:
* def queryObjects[T: ClassTag](queryString: String, fnMap: (Record => T)): Iterator[T];

* def execute[T](f: (Session) => T): T;

* def executeQuery[T](queryString: String, fn: (StatementResult => T)): T;

* def executeQuery[T](queryString: String, params: Map[String, AnyRef], fn: (StatementResult => T)): T;

* def executeUpdate(queryString: String);

* def executeUpdate(queryString: String, params: Map[String, AnyRef]);

* def executeQuery[T](queryString: String, params: Map[String, AnyRef])

* def querySingleObject[T](queryString: String, fnMap: (Record => T)): T

* def querySingleObject[T](queryString: String, params: Map[String, AnyRef], fnMap: (Record => T)): T

`graiph-connector-0.1.jar`: client SDK of GraiphServer.

# APIs: using embedded graiph database

using object `GraiphDB`:

* def openDatabase(dbDir: File, propertiesFile: File): GraphDatabaseService

*  def connect(dbs: GraphDatabaseService): CypherService