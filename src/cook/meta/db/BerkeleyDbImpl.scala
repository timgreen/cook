package cook.meta.db

import cook.meta.Meta
import cook.path.Path

import com.sleepycat.je.Database
import com.sleepycat.je.DatabaseConfig
import com.sleepycat.je.Environment
import com.sleepycat.je.EnvironmentConfig

object BerkeleyDbImpl extends Db {

  private var _db: Database = _

  def dbFile = Path().metaDbFile.jfile

  override def open {
    val envConf = new EnvironmentConfig
    envConf.setAllowCreate(true)
    val dbEnv = new Environment(dbFile, envConf)
    val dbConf = new DatabaseConfig
    dbConf.setAllowCreate(true)
    _db = dbEnv.openDatabase(null, "meta", dbConf)
  }

  override def close {
    _db.close
  }

  override def get(key: String): Meta = {
    // TODO(timgreen):
    null
  }

  override def put(key: String, meta: Meta) {
    // TODO(timgreen):
  }
}
