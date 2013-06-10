package cook.meta.db

import cook.meta.Meta
import cook.path.Path

import com.sleepycat.je.Database
import com.sleepycat.je.DatabaseConfig
import com.sleepycat.je.DatabaseEntry
import com.sleepycat.je.Environment
import com.sleepycat.je.EnvironmentConfig
import com.sleepycat.je.LockMode
import com.sleepycat.je.OperationStatus

object BerkeleyDbImpl extends Db {

  private var _db: Database = _

  def dbDir = Path().metaDir

  override def open {
    dbDir.createDirectory()
    val envConf = new EnvironmentConfig
    envConf.setAllowCreate(true)
    val dbEnv = new Environment(dbDir.jfile, envConf)
    val dbConf = new DatabaseConfig
    dbConf.setAllowCreate(true)
    _db = dbEnv.openDatabase(null, "meta", dbConf)
  }

  override def close {
    if (_db ne null) {
      _db.close
      _db = null
    }
  }

  private implicit def string2databaseEntry(s: String) =
    new DatabaseEntry(s.getBytes("UTF-8"))

  private implicit def meta2databaseEntry(meta: Meta) =
    new DatabaseEntry(meta.toBytes)

  override def get(key: String): Meta = {
    val data = new DatabaseEntry
    _db.get(null, key, data, LockMode.DEFAULT) match {
      case OperationStatus.SUCCESS =>
        Meta.fromBytes(data.getData)
      case _ =>
        new Meta
    }
  }

  override def put(key: String, meta: Meta) {
    _db.put(null, key, meta)
  }
}
