package de.mineformers.core.client.ui.skin

import scala.util.parsing.combinator.JavaTokenParsers

/**
 * SkinQueryParser
 *
 * @author PaleoCrafter
 */
class SkinQueryParser extends JavaTokenParsers {
  def parseQuery(input: String) = parseAll(query, input)

  def query: Parser[Either[String, (String, Map[String, String])]] = (state | ident) ^^ {
    case id: String => Left(id)
    case state: (String, Map[String, String]) => Right(state)
  }

  def state: Parser[(String, Map[String, String])] = ident ~ "[" ~ rep1sep(property, ",") ~ "]" ^^ { case name ~ "[" ~ props ~ "]" => (name, props.toMap)}

  def property: Parser[(String, String)] = ((ident ~ "=" ~ ident) | ("!" ~ ident) | "disabled" | ident) ^^ {
    case "disabled" => ("enabled", "false")
    case s: String => (s, "true")
    case "!" ~ s => (s.toString, "false")
    case key ~ "=" ~ value => (key.toString, value.toString)
  }
}
