// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

abstract sealed trait TokenType

object TokenType {
  case object Eof extends TokenType
  case object OpenParen extends TokenType
  case object CloseParen extends TokenType
  case object OpenBracket extends TokenType
  case object CloseBracket extends TokenType
  case object OpenBrace extends TokenType
  case object CloseBrace extends TokenType
  // formerly CONSTANT
  case object Literal extends TokenType
  // encapsulates what was formerly IDENT or VARIABLE
  case object Ident extends TokenType
  case object Command extends TokenType
  case object Reporter extends TokenType
  case object Keyword extends TokenType
  case object Comma extends TokenType
  case object Comment extends TokenType
  case object Bad extends TokenType        // characters the tokenizer couldn't digest
  // Formerly LITERAL
  case object Extension extends TokenType  // extension literals (for export-world of extension types)
}
