package cook.actor

import cook.config.ConfigRef
import cook.ref.FileRef

import scala.concurrent.Future
import scala.util.Try

trait ConfigRefVerifier {

  def passCycleCheck(configRef: ConfigRef): Future[Try[Boolean]]
}
