package de.mineformers.core.client.ui.state

import java.util.Objects

/**
 * Property
 *
 * @author PaleoCrafter
 */
trait Property[+T] {
  def name: String

  def defaultValue: T

  def allowedValues: Seq[T]

  def nameFrom[A >: T](value: A): String

  def parse(input: String): T

  override def hashCode(): Int = Objects.hash(name, allowedValues)

  override def equals(obj: scala.Any): Boolean = obj match {
    case p: Property[_] => p.name == name && p.allowedValues == allowedValues
    case _ => false
  }
}

object Property {
  final val Hovered = new BooleanProperty("hovered")
  final val Focused = new BooleanProperty("focused")
  final val Enabled = new BooleanProperty("enabled")
  final val Text = new StringProperty("text")
}