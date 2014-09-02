/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 MineFormers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.mineformers.core.client.util

/**
 * Color
 *
 * @author PaleoCrafter
 */
object Color {
  def fromHSV(hue: Float, saturation: Float, value: Float): Color = fromHSV(hue, saturation, value, 1)

  def fromHSV(hue: Float, saturation: Float, value: Float, alpha: Float): Color = {
    if (saturation == 0)
      return Color(value, value, value, alpha)

    val h = (hue - math.floor(hue).toFloat) * 6.0f
    val f = h - math.floor(hue).toFloat
    val p = value * (1 - saturation)
    val q = value * (1 - f * saturation)
    val t = value * (1 - (1 - f) * saturation)
    h match {
      case 0 => Color(value, t, p, alpha)
      case 1 => Color(q, value, p, alpha)
      case 2 => Color(p, value, t, alpha)
      case 3 => Color(p, q, value, alpha)
      case 4 => Color(t, p, value, alpha)
      case 5 => Color(value, p, q, alpha)
    }
  }

  def apply(hex: Int): Color = {
    val r = ((hex >> 16) & 0xFF) / 255F
    val g = ((hex >> 8) & 0xFF) / 255F
    val b = (hex & 0xFF) / 255F
    val a = ((hex >> 24) & 0xFF) / 255F
    this(r, g, b, if (a > 0) a else 1)
  }

  def apply(hex: Int, alpha: Float): Color = {
    val r = ((hex >> 16) & 0xFF) / 255F
    val g = ((hex >> 8) & 0xFF) / 255F
    val b = (hex & 0xFF) / 255F
    this(r, g, b, alpha)
  }

  def apply(r: Float, g: Float, b: Float): Color = this(r, g, b, 1)
}

case class Color(r: Float, g: Float, b: Float, a: Float) {
  def integer = {
    var i = ((a * 255).toInt & 0xFF) << 24
    i |= ((r * 255).toInt & 0xFF) << 16
    i |= ((g * 255).toInt & 0xFF) << 8
    i |= (b * 255).toInt & 0xFF
    i
  }
}