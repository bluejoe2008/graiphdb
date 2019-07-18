package cn.graiph.engine;

import org.neo4j.cypher.internal.v3_5.expressions._
import org.neo4j.cypher.internal.v3_5.parser.{ExprExtensions, Expressions}
import org.neo4j.cypher.internal.v3_5.util.InputPosition
import org.neo4j.cypher.internal.v3_5.util.symbols._
import org.neo4j.cypher.internal.v3_5.{expressions => ast}
import org.parboiled.scala._

/**
  * Created by bluejoe on 2019/7/16.
  */
object GraiphDBInjection extends Expressions {
  def touch = {}

  private def AlgoNameWithThreshold: Rule1[AlgoNameWithThresholdExpr] = rule("an algorithm with threshold") {
    group(SymbolicNameString ~ optional(operator("/") ~ DoubleLiteral)) ~~>>
      ((a, b) => AlgoNameWithThresholdExpr(Some(a), b.map(_.value))) |
      group(DoubleLiteral ~ optional(operator("/") ~ SymbolicNameString)) ~~>>
        ((a, b) => AlgoNameWithThresholdExpr(b, Some(a.value)))
  }

  private def AlgoName: Rule1[AlgoNameWithThresholdExpr] = rule("an algorithm with threshold") {
    group(SymbolicNameString) ~~>>
      ((a) => AlgoNameWithThresholdExpr(Some(a), None))
  }

  ExprExtensions.addExpr3((Expression2: Rule1[org.neo4j.cypher.internal.v3_5.expressions.Expression]) => {
    group(operator("~:") ~ optional(AlgoNameWithThreshold) ~~ Expression2) ~~>>
      ((a: ast.Expression, b, c) =>
        SemanticLikeExpr(a, b, c)) |
      group(operator("!:") ~ optional(AlgoNameWithThreshold) ~~ Expression2) ~~>>
        ((a: ast.Expression, b, c) =>
          SemanticUnlikeExpr(a, b, c)) |
      group(operator(":::") ~ optional(AlgoName) ~~ Expression2) ~~>>
        ((a: ast.Expression, b, c) =>
          SemanticSetCompareExpr(a, b, c)) |
      group(operator(">>:") ~ optional(AlgoNameWithThreshold) ~~ Expression2) ~~>>
        ((a: ast.Expression, b, c) =>
          SemanticContainSetExpr(a, b, c)) |
      group(operator("<<:") ~ optional(AlgoNameWithThreshold) ~~ Expression2) ~~>>
        ((a: ast.Expression, b, c) =>
          SemanticSetInExpr(a, b, c)) |
      group(operator("::") ~ optional(AlgoName) ~~ Expression2) ~~>>
        ((a: ast.Expression, b, c) =>
          SemanticCompareExpr(a, b, c)) |
      group(operator(">:") ~ optional(AlgoNameWithThreshold) ~~ Expression2) ~~>>
        ((a: ast.Expression, b, c) =>
          SemanticContainExpr(a, b, c)) |
      group(operator("<:") ~ optional(AlgoNameWithThreshold) ~~ Expression2) ~~>>
        ((a: ast.Expression, b, c) =>
          SemanticInExpr(a, b, c))
  });
}

case class AlgoNameWithThresholdExpr(algorithm: Option[String], threshold: Option[Double])(val position: InputPosition)
  extends Expression {
}

case class CustomPropertyExpr(map: Expression, propertyKey: PropertyKeyName)(val position: InputPosition) extends LogicalProperty {
  override def asCanonicalStringVal = s"${map.asCanonicalStringVal}.${propertyKey.asCanonicalStringVal}"
}

case class SemanticLikeExpr(lhs: Expression, ant: Option[AlgoNameWithThresholdExpr], rhs: Expression)(val position: InputPosition)
  extends Expression with BinaryOperatorExpression {
  override val signatures = Vector(
    TypeSignature(argumentTypes = Vector(CTAny, CTAny), outputType = CTBoolean)
  )

  override def canonicalOperatorSymbol = this.getClass.getSimpleName
}

case class SemanticUnlikeExpr(lhs: Expression, ant: Option[AlgoNameWithThresholdExpr], rhs: Expression)(val position: InputPosition)
  extends Expression with BinaryOperatorExpression {
  override val signatures = Vector(
    TypeSignature(argumentTypes = Vector(CTAny, CTAny), outputType = CTBoolean)
  )

  override def canonicalOperatorSymbol = this.getClass.getSimpleName
}

case class SemanticCompareExpr(lhs: Expression, ant: Option[AlgoNameWithThresholdExpr], rhs: Expression)(val position: InputPosition)
  extends Expression with BinaryOperatorExpression {
  override val signatures = Vector(
    TypeSignature(argumentTypes = Vector(CTAny, CTAny), outputType = CTFloat)
  )

  override def canonicalOperatorSymbol = this.getClass.getSimpleName
}

case class SemanticSetCompareExpr(lhs: Expression, ant: Option[AlgoNameWithThresholdExpr], rhs: Expression)(val position: InputPosition)
  extends Expression with BinaryOperatorExpression {
  override val signatures = Vector(
    TypeSignature(argumentTypes = Vector(CTAny, CTAny), outputType = CTList(CTList(CTFloat)))
  )

  override def canonicalOperatorSymbol = this.getClass.getSimpleName
}

case class SemanticContainExpr(lhs: Expression, ant: Option[AlgoNameWithThresholdExpr], rhs: Expression)(val position: InputPosition)
  extends Expression with BinaryOperatorExpression {
  override val signatures = Vector(
    TypeSignature(argumentTypes = Vector(CTAny, CTAny), outputType = CTBoolean)
  )

  override def canonicalOperatorSymbol = this.getClass.getSimpleName
}

case class SemanticInExpr(lhs: Expression, ant: Option[AlgoNameWithThresholdExpr], rhs: Expression)(val position: InputPosition)
  extends Expression with BinaryOperatorExpression {
  override val signatures = Vector(
    TypeSignature(argumentTypes = Vector(CTAny, CTAny), outputType = CTBoolean)
  )

  override def canonicalOperatorSymbol = this.getClass.getSimpleName
}

case class SemanticContainSetExpr(lhs: Expression, ant: Option[AlgoNameWithThresholdExpr], rhs: Expression)(val position: InputPosition)
  extends Expression with BinaryOperatorExpression {
  override val signatures = Vector(
    TypeSignature(argumentTypes = Vector(CTAny, CTAny), outputType = CTBoolean)
  )

  override def canonicalOperatorSymbol = this.getClass.getSimpleName
}

case class SemanticSetInExpr(lhs: Expression, ant: Option[AlgoNameWithThresholdExpr], rhs: Expression)(val position: InputPosition)
  extends Expression with BinaryOperatorExpression {
  override val signatures = Vector(
    TypeSignature(argumentTypes = Vector(CTAny, CTAny), outputType = CTBoolean)
  )

  override def canonicalOperatorSymbol = this.getClass.getSimpleName
}
