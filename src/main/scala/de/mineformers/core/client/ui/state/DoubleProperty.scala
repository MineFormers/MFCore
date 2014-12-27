package de.mineformers.core.client.ui.state

import java.lang.{Double => JDouble}

/**
 * DoubleProperty
 *
 * @author PaleoCrafter
 */
class DoubleProperty(val name: String, val defaultValue: Double = 0D, range: Range = null) extends Property[Double] {
  val allowedValues = if (range != null) range.toSeq.map(_.toDouble) else null

  override def nameFrom[A >: Double](value: A): String = value.toString

  override def parse(input: String): Double = JDouble.valueOf(input).doubleValue()
}