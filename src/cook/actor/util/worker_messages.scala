package cook.actor.util

// to WorkerTokenManager
case object AskWorkerToken
case class ReturnWorkerToken(token: Int)
// from WorkerTokenManager
case class AssignWorkerToken(token: Int)

