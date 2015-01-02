package de.mineformers.core.client.ui.skin

import de.mineformers.core.client.ui.component.View

/**
 * SkinSelector
 *
 * @author PaleoCrafter
 */
case class SkinSelector(query: String) {
  private[this] val parsed = new SkinQueryParser().parseQuery(query).getOrElse(null)
  if (parsed == null) {
    throw new Exception("Failed to parse skin selector \"" + query + "\"")
  }

  val id = parsed match {
    case Left(s) => s
    case Right((s, _)) => s
  }

  private val rawProperties = parsed match {
    case Right((_, m)) => m
    case _ => null
  }

  val propertyCount = if (rawProperties == null) 0 else rawProperties.size

  private var properties: Map[String, Any] = null

  def priority(component: View) = {
    if(properties != null) {
      properties.foldLeft(0)((acc, s) => acc + component.state.propertyPriority(s._1))
    } else 0
  }

  def initProperties(component: View): Unit = {
    if (properties == null && rawProperties != null) {
      properties = Map.empty[String, Any]
      val props = component.state.properties.keySet
      props foreach { p =>
        if (rawProperties.contains(p.name)) {
          val value = p.parse(rawProperties(p.name))
          if (value != null && (p.allowedValues == null || p.allowedValues.contains(value)))
            properties = properties.updated(p.name, value)
        }
      }
    }
  }

  def matches(component: View): Boolean = {
    if (rawProperties == null)
      true
    else {
      properties.forall(p => component.state.byName(p._1).orNull == p._2)
    }
  }

  def matches(properties: Map[String, String]) = this.rawProperties.toList forall (properties.toList contains)

  def matches(query: String) = query == this.query
}
