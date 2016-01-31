package parsing

trait FilterSyntax {

  sealed abstract class Expression
  case class StringLiteral(s: String) extends Expression
  
  case class Equal(e1: Expression, e2: Expression) extends Expression
  case class Contains(e1: Expression, e2: Expression) extends Expression
  case class StartsWith(e1: Expression, e2: Expression) extends Expression
  case class Present(e1: Expression, e2: Expression) extends Expression
  case class GreaterThan(e1: Expression, e2: Expression) extends Expression
  case class GreaterThanOrEqual(e1: Expression, e2: Expression) extends Expression
  case class LessThan(e1: Expression, e2: Expression) extends Expression
  case class LessThanOrEqual(e1: Expression, e2: Expression) extends Expression
  
  case class And(e1: Expression, e2: Expression) extends Expression
  case class Or(e1: Expression, e2: Expression) extends Expression
}