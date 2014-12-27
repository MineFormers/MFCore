package de.mineformers.core.client.ui.state

import java.lang.{Integer => JInt}

/**
 * IntProperty
 *
 * @author PaleoCrafter
 */
class IntProperty(val name: String, val defaultValue: Int = 0, range: Range = null) extends Property[Int] {
  val allowedValues = if (range != null) range.toSeq else null

  override def nameFrom[A >: Int](value: A): String = value.toString

  override def parse(input: String): Int = JInt.valueOf(input).intValue()
}