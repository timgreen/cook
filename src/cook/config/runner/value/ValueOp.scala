package cook.config.runner.value

import cook.config.runner.EvalException

abstract class ValueOp(val name: String) {

  def eval(a: Value, b: Value): Value
}

object ValueOp {


  def eval(a: Value, op: String, b: Value): Value = ops.get(op) match {
    case Some(o) => o.eval(a, b)
    case None => error(op, a, b)
  }

  private[value]
  val ops = Map[String, ValueOp](
    "+"  -> PlusOp,
    "-"  -> MinusOp,
    "*"  -> StarOp,
    "/"  -> DivOp,
    "%"  -> ModOp,
    "++" -> IncOp,

    "<"  -> LtOp,
    ">"  -> GtOp,
    "<=" -> LeOp,
    ">=" -> GeOp,
    "==" -> EquEquOp,
    "!=" -> BangEquOp
  )

  def error(op: String, a: Value, b: Value): Value = {
    throw new EvalException("Unsupportted operation (%s %s %s)", a.typeName, op, b.typeName)
  }
}

object PlusOp extends ValueOp("+") {

  override def eval(a: Value, b: Value): Value = (a, b) match {
    case (NumberValue(ai), NumberValue(bi)) => NumberValue(ai + bi)
    case (ListValue(l), _) => ListValue(l :+ b)
    case (StringValue(as), StringValue(bs)) => StringValue(as + bs)
    case _ => ValueOp.error(name, a, b)
  }
}

object MinusOp extends ValueOp("-") {

  override def eval(a: Value, b: Value): Value = (a, b) match {
    case (NumberValue(ai), NumberValue(bi)) => NumberValue(ai - bi)
    case _ => ValueOp.error(name, a, b)
  }
}

object StarOp extends ValueOp("*") {

  override def eval(a: Value, b: Value): Value = (a, b) match {
    case (NumberValue(ai), NumberValue(bi)) => NumberValue(ai * bi)
    case _ => ValueOp.error(name, a, b)
  }
}

object DivOp extends ValueOp("/") {

  override def eval(a: Value, b: Value): Value = (a, b) match {
    case (NumberValue(ai), NumberValue(bi)) => NumberValue(ai / bi)
    case _ => ValueOp.error(name, a, b)
  }
}

object ModOp extends ValueOp("%") {

  override def eval(a: Value, b: Value): Value = (a, b) match {
    case (StringValue(as), ListValue(bl)) => StringValue(as.format(bl.map{ _.get } : _*))
    case _ => ValueOp.error(name, a, b)
  }
}

object IncOp extends ValueOp("++") {

  override def eval(a: Value, b: Value): Value = (a, b) match {
    case (ListValue(al), ListValue(bl)) => ListValue(al ++ bl)
    case _ => ValueOp.error(name, a, b)
  }
}

object LtOp extends ValueOp("<") {

  override def eval(a: Value, b: Value): Value = (a, b) match {
    case (NumberValue(ai), NumberValue(bi)) => BooleanValue(ai < bi)
    case (StringValue(as), StringValue(bs)) => BooleanValue(as < bs)
    case _ => ValueOp.error(name, a, b)
  }
}

object GtOp extends ValueOp(">") {

  override def eval(a: Value, b: Value): Value = (a, b) match {
    case (NumberValue(ai), NumberValue(bi)) => BooleanValue(ai > bi)
    case (StringValue(as), StringValue(bs)) => BooleanValue(as > bs)
    case _ => ValueOp.error(name, a, b)
  }
}

object LeOp extends ValueOp("<=") {

  override def eval(a: Value, b: Value): Value = (a, b) match {
    case (NumberValue(ai), NumberValue(bi)) => BooleanValue(ai <= bi)
    case (StringValue(as), StringValue(bs)) => BooleanValue(as <= bs)
    case _ => ValueOp.error(name, a, b)
  }
}

object GeOp extends ValueOp(">=") {

  override def eval(a: Value, b: Value): Value = (a, b) match {
    case (NumberValue(ai), NumberValue(bi)) => BooleanValue(ai >= bi)
    case (StringValue(as), StringValue(bs)) => BooleanValue(as >= bs)
    case _ => ValueOp.error(name, a, b)
  }
}

object EquEquOp extends ValueOp("==") {

  override def eval(a: Value, b: Value): Value = (a, b) match {
    case (NumberValue(ai), NumberValue(bi)) => BooleanValue(ai == bi)
    case (StringValue(as), StringValue(bs)) => BooleanValue(as == bs)
    case _ => ValueOp.error(name, a, b)
  }
}

object BangEquOp extends ValueOp("!=") {

  override def eval(a: Value, b: Value): Value = (a, b) match {
    case (NumberValue(ai), NumberValue(bi)) => BooleanValue(ai != bi)
    case (StringValue(as), StringValue(bs)) => BooleanValue(as != bs)
    case _ => ValueOp.error(name, a, b)
  }
}
