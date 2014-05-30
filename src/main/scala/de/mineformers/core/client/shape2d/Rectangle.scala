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

import scala.collection.mutable
import de.mineformers.core.client.shape2d.Rectangle.Bounds

/**
 * Rectangle
 *
 * @author PaleoCrafter
 */
object Rectangle {
  private val cache = new mutable.HashMap[Bounds, Rectangle]

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
}

class Rectangle(r: (Point, Point)) {

  def start: Point = r._1

  def end: Point = r._2

  def x: Int = start.x

  def y: Int = start.y

  def size: Size = Size(width, height)

  def width: Int = end.x - start.x

  def height: Int = end.y - start.y

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

  def &(r: Rectangle): Option[Rectangle] = {
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

  def contains(p: Point): Boolean = (p xyGreaterOrEqual start) && (p xyLessOrEqual end)

  def contains(r: Rectangle): Boolean = (r.start xyGreaterOrEqual start) && (r.end xyLessOrEqual end)

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case r: Rectangle => return r.start == start && r.end == end
      case t: Bounds => return t == r
    }
    false
  }

  override def toString = s"( start=$start, width=$width, height=$height )"

}
