package de.mineformers.core.client.ui.util.font

import de.mineformers.core.util.math.shape2d.Size

/**
 * Font
 *
 * @author PaleoCrafter
 */
trait Font {
  def withColor(color: Int): Font

  def color: Int

  def draw(text: String, x: Int, y: Int): Unit = draw(text, x, y, 0, color)

  def draw(text: String, x: Int, y: Int, z: Int): Unit = draw(text, x, y, z, color)

  def draw(text: String, x: Int, y: Int, z: Int, color: Int): Unit

  def height: Int

  def width(text: String): Int = width(text.split("\\n").mkString("\\\\n").split("\\\\n"): _*)

  def width(lines: String*): Int

  def height(text: String): Int = height(text.split("\\n").mkString("\\\\n").split("\\\\n"): _*)

  def height(lines: String*): Int = (height + 1) * lines.length - 1

  def charWidth(char: Char): Int

  def fit(text: String, width: Int): String = fit(text, width, reverse = false)

  def fit(text: String, width: Int, reverse: Boolean): String

  def size(text: String): Size = Size(width(text), height(text))
}
