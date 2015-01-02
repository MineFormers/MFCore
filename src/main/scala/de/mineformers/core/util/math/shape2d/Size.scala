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
package de.mineformers.core.util.math.shape2d

import de.mineformers.core.util.math.shape2d.Size.Dimensions

import scala.collection.mutable

/**
 * Size
 *
 * @author PaleoCrafter
 */
object Size {
  private val cache = new mutable.WeakHashMap[Dimensions, Size]
  type Dimensions = (Int, Int)

  /**
   * Create a new [[Size]] based on the given dimensions
   * @param width the height of the new size representation
   * @param height the width of the new size representation
   * @return a [[Size]] instance, either a new one or one from the cache
   */
  def apply(width: Int, height: Int): Size = apply((width, height))

  /**
   * Create a new [[Size]] based on the given dimensions
   * @param dimensions a tuple of the dimensions in the form (width, height)
   * @return a [[Size]] instance, either a new one or one from the cache
   */
  def apply(dimensions: Dimensions): Size = cache.getOrElseUpdate(dimensions, new Size(dimensions))

  def unapply(s: Size): Option[Dimensions] = Some((s.width, s.height))
}

class Size private(dimensions: Dimensions) {
  val (width, height) = dimensions
  private val _hashCode = 31 * width + height

  def +(s: Size): Size = Size(width + s.width, height + s.height)

  def -(s: Size): Size = this + -s

  def unary_+ = Size(width.abs, height.abs)

  def unary_- = Size(-width, -height)

  def invert = Size(height, width)

  override def hashCode = _hashCode

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case Size(sWidth, sHeight) => return sWidth == width && sHeight == height
      case (sWidth, sHeight) => return sWidth == width && sHeight == height
    }
    false
  }

  override def toString: String = s"( $width, $height )"
}
