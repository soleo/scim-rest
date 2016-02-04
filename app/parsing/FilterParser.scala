package parsing

import scala.util.parsing.combinator._

object FilterParser extends JavaTokenParsers with FilterSyntax {
    
// should put all allowed attributes in regular expression
  def attributeName:Parser[StringLiteral] = """(emails|title|username|usertype)""".r ^^ {
      case s => new StringLiteral(s) 
  }

  def attributeValue:Parser[StringLiteral] = stringLiteral ^^ {
     case str => new StringLiteral(str.substring(1, str.length - 1))
  }
  
  def parenthesizedExpression = "(" ~> expr <~ ")"
  
  def and:Parser[And] = operator ~ "and" ~ operator ^^ {
     case (x ~ "and" ~ y) => And(x, y)
  }

  def or:Parser[Or] = operator ~ "or" ~ operator ^^ {
     case (x ~ "or" ~ y) => Or(x, y)
  }
  
  def eq:Parser[Equal] = attributeName ~ "eq" ~ attributeValue ^^ {
    case (x ~ "eq" ~ y) => Equal(x, y)
  }
  
  def co:Parser[Contains] = attributeName ~ "co" ~ attributeValue ^^ {
    case (x ~ "co" ~ y) => Contains(x, y)
  }
  
  def sw:Parser[StartsWith] = attributeName ~ "sw" ~ attributeValue ^^ {
    case (x ~ "sw" ~ y) => StartsWith(x, y)
  }
  
  def gt:Parser[GreaterThan] = attributeName ~ "gt" ~ attributeValue ^^ {
    case (x ~ "gt" ~ y) => GreaterThan(x, y)
  }
  
  def ge:Parser[GreaterThanOrEqual] = attributeName ~ "ge" ~ attributeValue ^^ {
    case (x ~ "ge" ~ y) => GreaterThanOrEqual(x, y)
  }
  
  def lt:Parser[LessThan] = attributeName ~ "lt" ~ attributeValue ^^ {
    case (x ~ "lt" ~ y) => LessThan(x, y)
  }
  
  def le:Parser[LessThanOrEqual] = attributeName ~ "le" ~ attributeValue ^^ {
    case (x ~ "le" ~ y) => LessThanOrEqual(x, y)
  }

  def operator: Parser[Expression] = (eq | co | sw | gt | ge | le | lt | parenthesizedExpression) 
  
  def expr: Parser[Expression] = ( operator | and | or | parenthesizedExpression)

  def parse(text:String): FilterParser.Expression = {
       
    parseAll(expr, text) match {
          case Success(t, _) => t
          case Failure(msg, in) => throw new IllegalArgumentException("Failure\n"+in+"\n" + msg)
          case Error(msg, _) => throw new IllegalArgumentException("Error\n" + msg)
        }
  }
   
  def evaluate(text: String) : String = {
       // @TODO
       // map those operations to SQL 
       // startsWith should be X LIKE "%Y"
       // contains should be X LIKE "%Y%"
       // equal should be X = "Y"
       // and would be AND
       // or would be OR
       // etc.
       ""
  }
}


