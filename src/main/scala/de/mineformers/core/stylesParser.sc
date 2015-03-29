import de.mineformers.core.client.ui.view.container.Panel.Padding
import de.mineformers.core.client.util.Color
import de.mineformers.core.util.ResourceUtils.Resource

import scala.collection.mutable
import scala.util.parsing.combinator.JavaTokenParsers
val input =
  """button[hovered]#test {
    |   background: resource("mfcore:button_hovered") dynamic;
    |   background-corners: 0 0 0 0;
    |   padding: 5;
    |   color: white;
    |}
    |
    |button[disabled] {
    |   background: resource("mfcore:button_disabled") static;
    |   color: #000;
    |}
  """.stripMargin
case class RuleSet(selectors: List[Selector], body: List[Declaration])
case class Selector(component: String, state: Map[String, String])
case class Declaration(property: String, values: List[Any])
case class Texture(res: Resource, tpe: String, params: mutable.Map[String, List[Any]] = mutable.Map.empty[String, List[Any]])
class MFStylesParser extends JavaTokenParsers {
  private val predefinedColors = Map(
    "white" -> Color(0xFFFFFF),
    "red" -> Color(0xFF0000),
    "green" -> Color(0x00FF00),
    "blue" -> Color(0x0000FF),
    "black" -> Color(0x000000))
  private val specialProperties: Map[String, List[Any] => List[Any]] = Map(
    "color" -> {
      case (c: Color) :: Nil => List(c)
      case (s: String) :: Nil => List(predefinedColors(s))
    },
    "background" -> {
      case (r: Resource) :: (tpe: String) :: Nil => List(Texture(r, tpe))
    },
    "padding" -> {
      case (all: Int) :: Nil => List(Padding(all))
      case (vertical: Int) :: (horizontal: Int) :: Nil => List(Padding(vertical, horizontal))
      case (top: Int) :: (horizontal: Int) :: (bottom: Int) :: Nil => List(Padding(horizontal, top, horizontal, bottom))
      case (top: Int) :: (right: Int) :: (bottom: Int) :: (left: Int) :: Nil => List(Padding(left, top, right, bottom))
    }
  )
  private val rulePostProcessors: Seq[List[Declaration] => List[Declaration]] = List(
  { l =>
    l.find(_.property == "background") match {
      case Some(background) =>
        val texture = background.values.head.asInstanceOf[Texture]
        val params = l.filter(d => d.property.startsWith("background") && d.property != "background")
          .map(d => (d.property.stripPrefix("background-"), d.values))
        texture.params ++= params
        l.filterNot(d => d.property.startsWith("background") && d.property != "background")
      case None => l
    }
  }
  )

  def state: Parser[Map[String, String]] = "[" ~ rep1sep(stateProperty, ",") ~ "]" ^^ { case "[" ~ props ~ "]" => props.toMap}

  def stateProperty: Parser[(String, String)] = ((ident ~ "=" ~ ident) | ("!" ~ ident) | "disabled" | ident) ^^ {
    case "disabled" => ("enabled", "false")
    case s: String => (s, "true")
    case "!" ~ s => (s.toString, "false")
    case key ~ "=" ~ value => (key.toString, value.toString)
  }

  def element_name = ident

  def class_ = "." ~ ident ^^ { case "." ~ name => name}

  def HASH = "#" ~ ident ^^ { case "#" ~ name => name}

  def simple_selector = element_name ~ opt(state) ~ opt(class_) ~ opt(HASH) ^^ {
    case component ~ state ~ clazz ~ id =>
      var finalState = state.getOrElse(Map())
      if (clazz.isDefined)
        finalState += "style" -> clazz.get
      if (id.isDefined)
        finalState += "name" -> id.get
      Selector(component, finalState)
  }

  def selector = simple_selector ~ opt(",") ^^ {
    case sel ~ _ => sel
  }

  def hexcolor = "#(?:[0-9A-Fa-f]{3}){1,2}".r ^^ {
    case s =>
      val c = s.substring(1)
      c.length match {
        case 3 =>
          Color(Integer.parseInt(c.charAt(0).toString * 2 + c.charAt(1).toString * 2 + c.charAt(2).toString * 2, 16))
        case _ =>
          Color(Integer.parseInt(c, 16))
      }
  }

  def function = "[a-zA-Z:._0-9-]+\\(".r ~ funcexpr ~ ")"

  def funcexpr = rep1sep(opt(ident ~ "=") ~ term, ",")

  def property = "[a-zA-Z._0-9-]+".r

  def NUMBER = "([1-9]?\\d+)".r ^^ Integer.parseInt

  def unary_operator = "-" | "+"

  def resource = "resource(" ~ ((
    (stringLiteral ~ "," ~ stringLiteral) ^^ { case domain ~ "," ~ resource => Resource(domain.stripPrefix("\"").stripSuffix("\""), resource.stripPrefix("\"").stripSuffix("\""))}) |
    (stringLiteral ^^ (s => Resource(s.stripPrefix("\"").stripSuffix("\"")))) |
    "[^)]+".r) ~ ")" ^^ {
    case _ ~ res ~ _ =>
      res
  }

  def term: Parser[List[Any]] = rep1(unary_operator | (hexcolor | stringLiteral | NUMBER | resource | function | ident))

  def declaration = property ~ ":" ~ term ^^ {
    case prop ~ _ ~ expr =>
      if (specialProperties.contains(prop))
        Declaration(prop, specialProperties(prop)(expr))
      else
        Declaration(prop, expr)
  }

  def declaration_body = "{" ~ rep((declaration ~ rep(";")) ^^ { case dec ~ semicolons => dec}) ~ "}" ^^ {
    case "{" ~ declarations ~ "}" => rulePostProcessors.foldLeft(declarations)((decs, postProcessor) => postProcessor(decs))
  }

  def ruleset = rep1(selector) ~ declaration_body ^^ {
    case query ~ body => RuleSet(query, body)
  }
  def stylesheet = rep(ruleset)
}

val parser = new MFStylesParser
println(parser.parseAll(parser.stylesheet, input).get.mkString("\n"))