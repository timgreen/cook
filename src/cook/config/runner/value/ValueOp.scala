package cook.config.runner.value

import cook.config.runner.EvalException

abstract class ValueOp(val op: String) {

  def eval(a: Value, b: Value): Value = ValueOp.error(a, op, b)

  protected def rn(a: Value, b: Value) = "(%s %s %s)".format(a.name, op, b.name)
}

object ValueOp {


  def eval(a: Value, op: String, b: Value): Value = ops.get(op) match {
    case Some(o) => o.eval(a, b)
    case None => error(a, op, b)
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

  def error(a: Value, op: String, b: Value): Value = {
    throw new EvalException("Unsupportted operation (%s %s %s)", a.typeName, op, b.typeName)
  }
}

object PlusOp extends ValueOp("+") {

  override def eval(a: Value, b: Value): Value = {
    val resultName = rn(a, b)
    (a, b) match {
      case (NumberValue(_, ai), NumberValue(_, bi)) => NumberValue(resultName, ai + bi)
      case (ListValue(_, l), _) => ListValue(resultName, l :+ b)
      case (StringValue(_, as), StringValue(_, bs)) => StringValue(resultName, as + bs)
      case (TargetLabelValue(_, tl), StringValue(_, s)) =>
        FileLabelValue(resultName, tl.outputDir.getAbsolutePath + s)
      case (FileLabelValue(_, fl), StringValue(_, s)) =>
        FileLabelValue(resultName, fl.file.getAbsolutePath + s)
      case _ => super.eval(a, b)
    }
  }
}

object MinusOp extends ValueOp("-") {

  override def eval(a: Value, b: Value): Value = {
    val resultName = rn(a, b)
    (a, b) match {
      case (NumberValue(_, ai), NumberValue(_, bi)) => NumberValue(resultName, ai - bi)
      case _ => super.eval(a, b)
    }
  }
}

object StarOp extends ValueOp("*") {

  override def eval(a: Value, b: Value): Value = {
    val resultName = rn(a, b)
    (a, b) match {
      case (NumberValue(_, ai), NumberValue(_, bi)) => NumberValue(resultName, ai * bi)
      case _ => super.eval(a, b)
    }
  }
}

object DivOp extends ValueOp("/") {

  override def eval(a: Value, b: Value): Value = {
    val resultName = rn(a, b)
    (a, b) match {
      case (NumberValue(_, ai), NumberValue(_, bi)) => NumberValue(resultName, ai / bi)
      case _ => super.eval(a, b)
    }
  }
}

object ModOp extends ValueOp("%") {

  override def eval(a: Value, b: Value): Value = {
    val resultName = rn(a, b)
    (a, b) match {
      case (StringValue(_, as), ListValue(_, bl)) =>
        StringValue(resultName, as.format(bl.map{ _.get } : _*))
      case _ => super.eval(a, b)
    }
  }
}

object IncOp extends ValueOp("++") {

  override def eval(a: Value, b: Value): Value = {
    val resultName = rn(a, b)
    (a, b) match {
      case (ListValue(_, al), ListValue(_, bl)) => ListValue(resultName, al ++ bl)
      case _ => super.eval(a, b)
    }
  }
}

object LtOp extends ValueOp("<") {

  override def eval(a: Value, b: Value): Value = {
    val resultName = rn(a, b)
    (a, b) match {
      case (NumberValue(_, ai), NumberValue(_, bi)) => BooleanValue(resultName, ai < bi)
      case (StringValue(_, as), StringValue(_, bs)) => BooleanValue(resultName, as < bs)
      case _ => super.eval(a, b)
    }
  }
}

object GtOp extends ValueOp(">") {

  override def eval(a: Value, b: Value): Value = {
    val resultName = rn(a, b)
    (a, b) match {
      case (NumberValue(_, ai), NumberValue(_, bi)) => BooleanValue(resultName, ai > bi)
      case (StringValue(_, as), StringValue(_, bs)) => BooleanValue(resultName, as > bs)
      case _ => super.eval(a, b)
    }
  }
}

object LeOp extends ValueOp("<=") {

  override def eval(a: Value, b: Value): Value = {
    val resultName = rn(a, b)
    (a, b) match {
      case (NumberValue(_, ai), NumberValue(_, bi)) => BooleanValue(resultName, ai <= bi)
      case (StringValue(_, as), StringValue(_, bs)) => BooleanValue(resultName, as <= bs)
      case _ => super.eval(a, b)
    }
  }
}

object GeOp extends ValueOp(">=") {

  override def eval(a: Value, b: Value): Value = {
    val resultName = rn(a, b)
    (a, b) match {
      case (NumberValue(_, ai), NumberValue(_, bi)) => BooleanValue(resultName, ai >= bi)
      case (StringValue(_, as), StringValue(_, bs)) => BooleanValue(resultName, as >= bs)
      case _ => super.eval(a, b)
    }
  }
}

object EquEquOp extends ValueOp("==") {

  override def eval(a: Value, b: Value): Value = {
    val resultName = rn(a, b)
    (a, b) match {
      case (NumberValue(_, ai), NumberValue(_, bi)) => BooleanValue(resultName, ai == bi)
      case (StringValue(_, as), StringValue(_, bs)) => BooleanValue(resultName, as == bs)
      case (NullValue(_), NullValue(_)) => BooleanValue(resultName, true)
      case _ => BooleanValue(resultName, false)
    }
  }
}

object BangEquOp extends ValueOp("!=") {

  override def eval(a: Value, b: Value): Value = {
    val resultName = rn(a, b)
    BooleanValue(resultName, !EquEquOp.eval(a, b).asInstanceOf[BooleanValue].bool)
  }
}
