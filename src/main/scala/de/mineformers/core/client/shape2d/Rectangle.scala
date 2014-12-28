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
package de.mineformers.core.client.shape2d

import com.google.common.base.Objects
import de.mineformers.core.client.shape2d.Rectangle.Bounds

import scala.collection.mutable

/**
 * Rectangle
 *
 * @author PaleoCrafter
 */
object Rectangle {
  private val cache = new mutable.WeakHashMap[Bounds, Rectangle]
  val Empty = Rectangle(0, 0, 0, 0)
  type Bounds = (Point, Point)

  /**
   * Create a new [[Rectangle]] based on the given coordinates
   * @param startX the X of the new rectangle's start point
   * @param startY the Y of the new rectangle's start point
   * @param width the width of the rectangle
   * @param height the height of the rectangle
   * @return a [[Rectangle]] instance, either a new one or one from the cache
   */
  def apply(startX: Int, startY: Int, width: Int, height: Int): Rectangle = apply(Point(startX, startY), Point(startX + width, startY + height))

  def apply(start: Point, size: Size): Rectangle = apply(start, start + size)

  def apply(start: Point, width: Int, height: Int): Rectangle = apply(start, start +(width, height))

  def apply(start: Point, end: Point): Rectangle = apply((start, end))

  /**
   * Create a new [[Rectangle]] based on the given points
   * @param r a tuple of the points in the form (start, end)
   * @return a [[Rectangle]] instance, either a new one or one from the cache
   */
  def apply(r: Bounds): Rectangle = {
    val (start, end) = r
    if (start < end && (end |> start)) cache.getOrElseUpdate(r, new Rectangle(r))
    else {
      var newR = (end, start)
      if (start > end && (end |> start))
        newR = (Point(end.x, start.y), Point(start.x, end.y))
      if ((start |> end) && start < end)
        newR = (Point(start.x, end.y), Point(end.x, start.y))
      cache.getOrElseUpdate(newR, new Rectangle(newR))
    }
  }

  def unapply(r: Rectangle): Option[Bounds] = Some((r.start, r.end))
}

class Rectangle private(r: Bounds) extends Shape[Rectangle] {
  val (start, end) = r
  val x: Int = start.x
  val y: Int = start.y
  val width: Int = end.x - start.x
  val height: Int = end.y - start.y
  val size: Size = Size(width, height)
  private val _hashCode = Objects.hashCode(start, end)

  def +(p: Point): Rectangle = Rectangle(start, end + p)

  def +(r: Rectangle): Rectangle = {
    val startX = if (start > r.start) r.start.x else start.x
    val startY = if (start |> r.start) r.start.y else start.y
    val endX = if (end < r.end) r.end.x else end.x
    val endY = if (end |< r.end) r.end.y else end.y
    Rectangle(Point(startX, startY), Point(endX, endY))
  }

  def *(scale: Float): Rectangle = Rectangle(start, (width * scale).toInt, (height * scale).toInt)

  def *(scaleX: Float, scaleY: Float) = Rectangle(start, (width * scaleX).toInt, (height * scaleY).toInt)

  def *(scale: Point): Rectangle = Rectangle(start, width * scale.x, height * scale.y)

  def intersect(r: Rectangle): Option[Rectangle] = {
    if (r.start xyGreater end)
      return None
    val startX = if (start < r.start) r.start.x else start.x
    val startY = if (start |< r.start) r.start.y else start.y
    val endX = if (end > r.end) r.end.x else end.x
    val endY = if (end |> r.end) r.end.y else end.y
    Some(Rectangle(Point(startX, startY), Point(endX, endY)))
  }

  def resize(size: Size): Rectangle = Rectangle(start, size)

  def translate(x: Int, y: Int): Rectangle = translate(Point(x, y))

  def translate(p: Point): Rectangle = Rectangle(start + p, end + p)

  def center(s: Size): Point = center(Rectangle(Point(0, 0), s))

  def center(r: Rectangle): Point = Point(x + ((width - r.width) / 2), y + ((height - r.height) / 2))

  def centerInSize(s: Size): Point = centerInSize(Rectangle(Point(0, 0), s))

  def centerInSize(r: Rectangle): Point = Point((width - r.width) / 2, (height - r.height) / 2)

  def contains(p: Point): Boolean = (p xyGreaterOrEqual start) && (p xyLessOrEqual end)

  def contains(r: Rectangle): Boolean = (r.start xyGreaterOrEqual start) && (r.end xyLessOrEqual end)

  def local(p: Point): Point = p - start

  override def bounds: Rectangle = this

  override def hashCode = _hashCode

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case r: Rectangle => return r.start == start && r.end == end
      case (rStart, rEnd) => return rStart == start && rEnd == end
    }
    false
  }

  override def toString = s"( start=$start, width=$width, height=$height )"
}
