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

import de.mineformers.core.util.math.shape2d.Point.Coordinates

import scala.collection.mutable

/**
 * Point
 *
 * @author PaleoCrafter
 */
object Point {
  private val cache = new mutable.WeakHashMap[Coordinates, Point]
  type Coordinates = (Int, Int)
  val Zero = Point(0, 0)
  val One = Point(1, 1)

  /**
   * Create a new [[Point]] based on the given coordinates
   * @param x the X of the new point
   * @param y the Y of the new point
   * @return a [[Point]] instance, either a new one or one from the cache
   */
  def apply(x: Int, y: Int): Point = apply((x, y))

  /**
   * Create a new [[Point]] based on the given coordinates
   * @param p a tuple of the coordinates in the form (x, y)
   * @return a [[Point]] instance, either a new one or one from the cache
   */
  def apply(p: Coordinates): Point = cache.getOrElseUpdate(p, new Point(p))

  def unapply(p: Point): Option[Coordinates] = Some((p.x, p.y))
}

class Point private(p: Coordinates) {
  val (x, y) = p
  private val _hashCode = 31 * x + y

  def +(p: Coordinates): Point = this + Point(p)

  def +(s: Size): Point = this + Point(s.width, s.height)

  def +(p: Point): Point = Point(x + p.x, y + p.y)

  def -(p: Coordinates): Point = this - Point(p)

  def -(s: Size): Point = this - Point(s.width, s.height)

  def -(p: Point): Point = this + -p

  def *(s: Int): Point = this * Point(s, s)

  def *(p: Coordinates): Point = this * Point(p)

  def *(p: Point): Point = Point(x * p.x, y * p.y)

  def /(s: Int): Point = this / Point(s, s)

  def /(p: Coordinates): Point = this / Point(p)

  def /(p: Point): Point = Point(x / p.x, y / p.y)

  def unary_+ = this

  def unary_- = Point(-x, -y)

  def invert = Point(y, x)

  /**
   * @return the magnitude (length) of this point
   */
  def mag = math.sqrt(magSq)

  /**
   * @return the squared magnitude (length) of this point
   */
  def magSq = x * x + y * y

  /**
   * The distance between this point and the given coordinates
   * @param x the X coordinate to calculate the distance to
   * @param y the Y coordinate to calculate the distance to
   * @return the distance between this point and the given coordinates
   *         (this - (x, y)).mag
   */
  def distance(x: Int, y: Int) = {
    (this -(x, y)).mag
  }

  /**
   * The distance between this point and the given coordinates
   * @param p the coordinates to calculate the distance to
   * @return the distance between this point and the given coordinates
   *         (this - coords).mag
   */
  def distance(p: Coordinates) = {
    (this - p).mag
  }

  /**
   * The distance between this point and the given coordinates
   * @param p the coordinates to calculate the distance to
   * @return the distance between this point and the given coordinates
   *         (this - p).mag
   */
  def distance(p: Point) = {
    (this - p).mag
  }

  /**
   * The squared distance between this point and the given coordinates
   * @param x the X coordinate to calculate the distance to
   * @param y the Y coordinate to calculate the distance to
   * @return the squared distance between this point and the given coordinates
   *         (this - (x, y)).magSq
   */
  def distanceSq(x: Int, y: Int) = {
    (this -(x, y)).magSq
  }

  /**
   * The squared distance between this point and the given coordinates
   * @param p the coordinates to calculate the distance to
   * @return the squared distance between this point and the given coordinates
   *         (this - p).magSq
   */
  def distanceSq(p: Coordinates) = {
    (this - p).magSq
  }

  /**
   * The squared distance between this point and the given coordinates
   * @param p the coordinates to calculate the distance to
   * @return the squared distance between this point and the given coordinates
   *         (this - p).magSq
   */
  def distanceSq(p: Point) = {
    (this - p).magSq
  }

  override def hashCode = _hashCode

  override def equals(obj: Any): Boolean = {
    obj match {
      case Point(pX, pY) => return pX == x && pY == y
      case (pX, pY) => return pX == x && pY == y
    }
    false
  }

  override def toString = "(" + x + ", " + y + ")"

  def >(o: Point): Boolean = xGreater(o)

  def xGreater(o: Point): Boolean = x > o.x

  def <(o: Point): Boolean = xLess(o)

  def xLess(o: Point): Boolean = x < o.x

  def |>(o: Point): Boolean = yGreater(o)

  def yGreater(o: Point): Boolean = y > o.y

  def |<(o: Point): Boolean = yLess(o)

  def yLess(o: Point): Boolean = y < o.y

  def xyGreater(o: Point): Boolean = x > o.x && y > o.y

  def xyLess(o: Point): Boolean = x < o.x && y < o.y

  def xGreaterOrEqual(o: Point): Boolean = x >= o.x

  def xLessOrEqual(o: Point): Boolean = x <= o.x

  def yGreaterOrEqual(o: Point): Boolean = y >= o.y

  def yLessOrEqual(o: Point): Boolean = y <= o.y

  def xyGreaterOrEqual(o: Point): Boolean = x >= o.x && y >= o.y

  def xyLessOrEqual(o: Point): Boolean = x <= o.x && y <= o.y
}