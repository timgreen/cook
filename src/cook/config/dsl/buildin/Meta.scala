package cook.config.dsl.buildin

import cook.target.Target
import cook.util.HashManager

import scala.tools.nsc.io.Path

trait Meta {

  def filesToMeta(files: List[Path]): Target.TargetInputMeta = {
    files.map { f =>
      f.path -> HashManager.hash(f)
    } toMap
  }
}
