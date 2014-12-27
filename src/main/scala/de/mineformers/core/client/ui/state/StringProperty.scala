package de.mineformers.core.client.ui.state

/**
 * StringProperty
 *
 * @author PaleoCrafter
 */
class StringProperty(val name: String, val defaultValue: String = "", val allowedValues: Seq[String] = null) extends Property[String] {
  override def nameFrom[A >: String](value: A): String = value.toString

  override def parse(input: String): String = input
}