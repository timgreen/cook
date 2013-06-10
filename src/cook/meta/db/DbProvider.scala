package cook.meta.db

object DbProvider {

  implicit val db: Db = BerkeleyDbImpl
}
