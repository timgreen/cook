package cook.config.runner.value

import scala.collection.mutable.HashMap

import cook.config.parser.unit._
import cook.config.runner.EvalException
import cook.util._

abstract class Value(var name: String, val typeName: String) {

  protected def attrName(id: String) = name + "." + id
  protected def attrOrMethod(
      methodBuilders: HashMap[String, ValueMethodBuilder], id: String): Value = {
    if (methodBuilders != null) {
      methodBuilders.get(id) match {
        case Some(builder) => builder(this)
        case None => NullValue(attrName(id))
      }
    } else {
      NullValue(attrName(id))
    }
  }
  def attr(id: String): Value
  def unaryOp(op: String): Value = {
    throw new EvalException("Unsupportted UnaryOperation \"%s\" on <%s>: %s", op, typeName, name)
  }

  def isTrue = true
  def isVoid = false
  def isNull = false
  def get(): Any

  // Type convert
  def toChar: Char = toChar("<%s>:%s should be CharValue", typeName, name)
  def toChar(errorMessage: String, args: Any*) = this match {
    case CharValue(_, c) => c
    case _ => throw new EvalException(errorMessage, args: _*)
  }

  def toStr: String = toStr("<%s>:%s should be StringValue", typeName, name)
  def toStr(errorMessage: String, args: Any*) = this match {
    case StringValue(_, str) => str
    case _ => throw new EvalException(errorMessage, args: _*)
  }

  def toInt: Int = toInt("<%s>:%s should be NumberValue", typeName, name)
  def toInt(errorMessage: String, args: Any*) = this match {
    case NumberValue(_, int) => int
    case _ => throw new EvalException(errorMessage, args: _*)
  }

  def toBool: Boolean = toBool("<%s>:%s should be BooleanValue", typeName, name)
  def toBool(errorMessage: String, args: Any*) = this match {
    case BooleanValue(_, bool) => bool
    case _ => throw new EvalException(errorMessage, args: _*)
  }

  def toTargetLabel: TargetLabel = toTargetLabel("<%s>:%s should be TargetLabel", typeName, name)
  def toTargetLabel(errorMessage: String, args: Any*) = this match {
    case TargetLabelValue(_, targetLabel) => targetLabel
    case _ => throw new EvalException(errorMessage, args: _*)
  }

  def toFileLabel: FileLabel = toFileLabel("<%s>:%s should be FileLabel", typeName, name)
  def toFileLabel(errorMessage: String, args: Any*) = this match {
    case FileLabelValue(_, fileLabel) => fileLabel
    case _ => throw new EvalException(errorMessage, args: _*)
  }

  def toListValue(errorMessage: String, args: Any*): Seq[Value] = this match {
    case ListValue(_, list) => list
    case _ => throw new EvalException(errorMessage, args: _*)
  }

  def toListStr: Seq[String] = toListStr("<%s>:%s should be List StringValue", typeName, name)
  def toListStr(errorMessage: String, args: Any*) = {
    this.toListValue(errorMessage, args: _*).map { _.toStr(errorMessage, args: _*) }
  }

  def toListChar: Seq[Char] = toListChar("<%s>:%s should be List CharValue", typeName, name)
  def toListChar(errorMessage: String, args: Any*) = {
    this.toListValue(errorMessage, args: _*).map { _.toChar(errorMessage, args: _*) }
  }

  def toListTargetLabel: Seq[TargetLabel] =
      toListTargetLabel("<%s>:%s should be List TargetLabel", typeName, name)
  def toListTargetLabel(errorMessage: String, args: Any*) = {
    this.toListValue(errorMessage, args: _*).map { _.toTargetLabel(errorMessage, args: _*) }
  }

  def toListFileLabel: Seq[FileLabel] =
      toListFileLabel("<%s>:%s should be List FileLabel", typeName, name)
  def toListFileLabel(errorMessage: String, args: Any*) = {
    this.toListValue(errorMessage, args: _*).map { _.toFileLabel(errorMessage, args: _*) }
  }
}

case class VoidValue(n: String) extends Value(n, "Void") {

  override def isTrue: Boolean = {
    throw new UnsupportedOperationException("<VoidValue>:%s can not be cast to bool".format(name))
  }
  override def isVoid = true
  override def get(): Any = {
    throw new UnsupportedOperationException("VoidValue doesn't have wrapped data")
  }
  override def attr(id: String): Value = id match {
    case "isVoid" => BooleanValue(attrName(id), true)
    case _ => throw new EvalException("Error on access attr \"%s\" on VoidValue %s", id, name)

  }
}

case class NullValue(n: String) extends Value(n, "Null") {

  override def isTrue: Boolean = false
  override def isNull = true
  override def get(): Any = null
  override def attr(id: String): Value = id match {
    case "isNull" => BooleanValue(attrName(id), true)
    case _ => throw new EvalException("Error on access attr \"%s\" on NullValue %s", id, name)
  }
}

case class BooleanValue(n: String, bool: Boolean) extends Value(n, "Bool") {

  override def isTrue: Boolean = bool
  override def unaryOp(op: String): Value = op match {
    case "!" => BooleanValue("!" + name, !bool)
    case _ => super.unaryOp(op)
  }
  override def attr(id: String): Value = id match {
    case "isBool" => BooleanValue(attrName(id), true)
    case _ => super.attrOrMethod(null, id)
  }

  override def get(): Any = bool
}

case class NumberValue(n: String, int: Int) extends Value(n, "Number") {

  override def isTrue: Boolean = (int != 0)
  override def get(): Any = int
  override def attr(id: String): Value = id match {
    case "isInt" => BooleanValue(attrName(id), true)
    case _ => super.attrOrMethod(null, id)
  }
}

case class StringValue(n: String, str: String) extends Value(n, "String") {

  override def isTrue: Boolean = str.nonEmpty
  override def attr(id: String): Value = id match {
    case "isStr" => BooleanValue(attrName(id), true)
    case "size" => NumberValue(attrName(id), str.size)
    case "length" => NumberValue(attrName(id), str.length)
    case "isEmpty" => BooleanValue(attrName(id), str.isEmpty)
    case "nonEmpty" => BooleanValue(attrName(id), str.nonEmpty)
    case _ => super.attrOrMethod(ValueMethod.stringMethodBuilders, id)
  }

  override def get(): Any = str
}

case class CharValue(n: String, c: Char) extends Value(n, "Char") {

  override def get(): Any = c
  override def attr(id: String): Value = id match {
    case "isChar" => BooleanValue(attrName(id), true)
    case _ => super.attrOrMethod(null, id)
  }
}

case class ListValue(n: String, list: Seq[Value]) extends Value(n, "List") {

  override def attr(id: String): Value = id match {
    case "isList" => BooleanValue(attrName(id), true)
    case "size" => NumberValue(attrName(id), list.size)
    case "length" => NumberValue(attrName(id), list.length)
    case "isEmpty" => BooleanValue(attrName(id), list.isEmpty)
    case "nonEmpty" => BooleanValue(attrName(id), list.nonEmpty)
    case _ => super.attrOrMethod(ValueMethod.listMethodBuilders, id)
  }

  override def get(): Any = list
}

object ListValue {

  def apply(n: String): ListValue = ListValue(n, Seq[Value]())
}

case class MapValue(n: String, map: HashMap[String, Value]) extends Value(n, "Map") {
  override def get(): Any = map
  override def attr(id: String): Value = {
    if (map.contains(id)) {
      val v = map(id)
      v.name = attrName(id)
      v
    } else {
      id match {
        case "isMap" => BooleanValue(attrName(id), true)
        case "size" => NumberValue(attrName(id), map.size)
        case _ => super.attrOrMethod(null, id)
      }
    }
  }
}

abstract class LabelValue(n: String, typeName: String) extends Value(n, typeName)
object LabelValue {

  def apply(n: String, label: Label): LabelValue = label match {
    case fileLabel: FileLabel => FileLabelValue(n, fileLabel)
    case targetLabel: TargetLabel => TargetLabelValue(n, targetLabel)
  }
}

case class FileLabelValue(n: String, fileLabel: FileLabel) extends LabelValue(n, "FileLabel") {

  override def get(): Any = fileLabel
  override def attr(id: String): Value = id match {
    case "isLabel" => BooleanValue(attrName(id), true)
    case "isFileLabel" => BooleanValue(attrName(id), true)
    case "file" => StringValue(attrName(id), fileLabel.file.getAbsolutePath)
    case _ => super.attrOrMethod(null, id)
  }
}

object FileLabelValue {

  def apply(n: String, absPath: String): FileLabelValue = {
    FileLabelValue(n, new FileLabel(null, absPath))
  }
}

case class TargetLabelValue(n: String, targetLabel: TargetLabel)
    extends LabelValue(n, "TargetLabel") {

  override def get(): Any = targetLabel
  override def attr(id: String): Value = id match {
    case "isLabel" => BooleanValue(attrName(id), true)
    case "isTargetLabel" => BooleanValue(attrName(id), true)
    case "outputDir" => StringValue(attrName(id), targetLabel.outputDir.getAbsolutePath)
    case _ => super.attrOrMethod(null, id)
  }
}

class ArgsDef(val names: Seq[String], val defaultValues: HashMap[String, Value])
class FunctionValue(n: String,
                    val path: String,
                    val scope: Scope,
                    val argsDef: ArgsDef,
                    val statements: Seq[Statement]) extends Value(n, "Function") {

  override def get(): Any = {
    throw new UnsupportedOperationException("FunctionValue doesn't have wrapped data")
  }
  override def attr(id: String): Value = id match {
    case "isFunction" => BooleanValue(attrName(id), true)
    case _ => super.attrOrMethod(null, id)
  }
}
abstract class BuildinFunction(n: String, argsDef: ArgsDef)
    extends FunctionValue(n, null, null, argsDef, null) {

  def eval(path: String, argsValue: Scope): Value
}
