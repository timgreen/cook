package cook

package object meta {

  /**
   * Meta is a grouped name to hash map.
   *
   * e.g.
   * <code>
   *   group1: {
   *     name1: hash1,
   *     name2: hash2,
   *     ...
   *   },
   *   group2: {
   *     ...
   *   },
   *   ...
   * </code>
   */
  type Meta = Map[String, Map[String, String]]
}
