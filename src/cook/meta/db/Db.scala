package cook.meta.db

import cook.meta.Meta

trait Db {

  def open
  def close
  def clean
  def put(key: String, meta: Meta)
  def get(key: String): Meta
}
