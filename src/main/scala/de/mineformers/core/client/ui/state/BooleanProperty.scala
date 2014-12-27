package de.mineformers.core.client.ui.state

import java.lang.{Boolean => JBoolean}

/**
 * BooleanProperty
 *
 * @author PaleoCrafter
 */
class BooleanProperty(val name: String, val defaultValue: Boolean = false) extends Property[Boolean] {
  val allowedValues = Seq(true, false)

  override def nameFrom[A >: Boolean](value: A): String = value.toString

  override def parse(input: String): Boolean = JBoolean.valueOf(input).booleanValue()
}