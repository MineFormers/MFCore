package de.mineformers.core.client.ui.skin

import de.mineformers.core.client.ui.component.Component

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

  def matches(component: Component): Boolean = {
    if (component.identifier == id) {
      if (rawProperties == null)
        true
      else {
        if (properties == null) {
          properties = Map.empty[String, Any]
          val props = component.state.properties.keySet
          props foreach { p =>
            if (rawProperties.contains(p.name)) {
              val value = p.parse(rawProperties(p.name))
              if (value != null && (p.allowedValues == null || p.allowedValues.contains(value)))
                properties = properties.updated(p.name, value)
            }
          }
          println(component.state)
        }
        properties.forall(p => component.state.byName(p._1).orNull == p._2)
      }
    } else
      false
  }

  def matches(properties: Map[String, String]) = this.rawProperties.toList forall (properties.toList contains)

  def matches(query: String) = query == this.query
}
