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
  def black(alpha: Float) = this(0, 0, 0, alpha)

  def fromHSV(hue: Float, saturation: Float, value: Float): Color = fromHSV(hue, saturation, value, 1)

  def fromHSV(hue: Float, saturation: Float, value: Float, alpha: Float): Color = apply(java.awt.Color.HSBtoRGB(hue, saturation, value), alpha)

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