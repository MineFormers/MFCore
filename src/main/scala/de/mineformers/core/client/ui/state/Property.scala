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

  def priority: Int

  override def hashCode(): Int = Objects.hash(name, allowedValues)

  override def equals(obj: scala.Any): Boolean = obj match {
    case p: Property[_] => p.name == name && p.allowedValues == allowedValues
    case _ => false
  }
}

object Property {
  final val Name = new StringProperty("name")
  final val Hovered = new BooleanProperty("hovered", priority = 1)
  final val Focused = new BooleanProperty("focused")
  final val Enabled = new BooleanProperty("enabled", priority = 2)
  final val Text = new StringProperty("text")
  final val Style = new StringProperty("style")
}