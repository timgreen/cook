package cook.actor

import cook.config.Config
import cook.config.ConfigRef
import cook.ref.FileRef
import cook.ref.TargetRef

//
case object Tick

// to ConfigManager
case class GetConfig(cookFileRef: FileRef)
case class ConfigLoaded(refName: String, config: Config)
case class FindConfigRef(configRef: ConfigRef)

// to ConfigLoader
case object PreFetchRootConfigRef
case class LoadConfig(configRef: ConfigRef)
case class LoadConfigSuccess(refName: String, config: Config)

// to TargetManager
case class GetTarget(targetRef: TargetRef)

// to ConfigRefManager
case class GetConfigRef(cookFileRef: FileRef)
case class LoadConfigRef(refName: String, cookFileRef: FileRef)
case class ConfigRefLoaded(refName: String, cookFileRef: ConfigRef)
