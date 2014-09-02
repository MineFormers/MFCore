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

package de.mineformers.core.util.world

import com.google.common.base.Objects

import scala.collection.mutable

import java.lang.{Double => JDouble}

/**
 * Vector2
 * Manages every vector and caches them
 *
 * @author PaleoCrafter
 */
object Vector2 {
  private val cache = mutable.WeakHashMap[(Double, Double), Vector2]()

  val Zero = Vector2(0, 0)
  val One = Vector2(1, 1)
  val Center = Vector2(0.5D, 0.5D)

  /**
   * Create a new [[Vector2]] based on the given coordinates
   * @param x the X of the new vector
   * @param y the Y of the new vector
   * @return a [[Vector2]] instance, either a new one or one from the cache
   */
  def apply(x: Double, y: Double): Vector2 = apply((x, y))

  /**
   * Create a new [[Vector2]] based on the given coordinates
   * @param coords a tuple of the coordinates in the form (x, y)
   * @return a [[Vector2]] instance, either a new one or one from the cache
   */
  def apply(coords: (Double, Double)): Vector2 = cache.getOrElseUpdate(coords, new Vector2(coords))

  def unapply(vec: Vector2): Option[(Double, Double)] = Some((vec.x, vec.y))
}

/**
 * Don't use! Use Vector2(x, y, z) for caching!
 * @param coords a tuple representing the coordinates of this vector
 */
class Vector2 private(coords: (Double, Double)) extends VectorLike[Vector2] {
  val (x, y) = coords
  private val _hashCode = Objects.hashCode(JDouble.valueOf(x), JDouble.valueOf(y))

  /**
   * Add the given coordinates to this vector
   * @param x the X component to add
   * @param y the Y component to add
   * @return a new (cached) Vector2 with the sum of the coordinates
   */
  def +(x: Double, y: Double): Vector2 = this + Vector2(x, y)

  /**
   * Add the given coordinates to this vector
   * @param coords a tuple representing the coordinates to add
   * @return a new (cached) Vector2 with the sum of the coordinates
   */
  def +(coords: (Double, Double)): Vector2 = this + Vector2(coords)

  /**
   * Add the given coordinates to this vector
   * @param vec another Vector2 to add
   * @return a new (cached) Vector2 with the sum of the coordinates
   */
  def +(vec: Vector2): Vector2 = Vector2(x + vec.x, y + vec.y)

  /**
   * Subtract the given coordinates from this vector
   * @param x the X component to subtract
   * @param y the Y component to subtract
   * @return a new (cached) Vector2 with the difference of the coordinates
   */
  def -(x: Double, y: Double): Vector2 = this - Vector2(x, y)

  /**
   * Subtract the given coordinates from this vector
   * @param coords a tuple representing the coordinates to subtract
   * @return a new (cached) Vector2 with the difference of the coordinates
   */
  def -(coords: (Double, Double)): Vector2 = this - Vector2(coords)

  /**
   * Multiply the coordinates of this vector
   * @param scalar a plain value every component of the Vector2 will be multiplied with
   * @return a new (cached) Vector2 with the product of this vector with the scalar
   */
  def *(scalar: Double): Vector2 = Vector2(x * scalar, y * scalar)

  /**
   * Multiply the given coordinates with this vector
   * @param x the X coordinate to multiply with
   * @param y the Y coordinate to multiply with
   * @return a new (cached) Vector2 with the product of the form
   *         (this.x * x, this.y * y)
   */
  def *(x: Double, y: Double): Vector2 = this * Vector2(x, y)

  /**
   * Multiply the given coordinates with this vector
   * @param coords a tuple representing the coordinates to multiply with
   * @return a new (cached) Vector2 with the product of the form
   *         (this.x * coords.x, this.y * coords.y)
   */
  def *(coords: (Double, Double)): Vector2 = this * Vector2(coords)

  /**
   * Multiply the given coordinates with this vector
   * @param vec a tuple representing the coordinates to multiply with
   * @return a new (cached) Vector2 with the product of the form
   *         (this.x * vec.x, this.y * vec.y)
   */
  def *(vec: Vector2): Vector2 = Vector2(x * vec.x, y * vec.y)

  def unary_- = Vector2(-x, -y)

  /**
   * @return the squared magnitude (length) of this vector
   */
  def magSq = {
    x * x + y * y
  }

  /**
   * The distance between this vector and the given coordinates
   * @param x the X coordinate to calculate the distance to
   * @param y the Y coordinate to calculate the distance to
   * @return the distance between this vector and the given coordinates
   *         (this - (x, y)).mag
   */
  def distance(x: Int, y: Int) = {
    (this -(x, y)).mag
  }

  /**
   * The distance between this vector and the given coordinates
   * @param coords the coordinates to calculate the distance to
   * @return the distance between this vector and the given coordinates
   *         (this - coords).mag
   */
  def distance(coords: (Double, Double)) = {
    (this - coords).mag
  }

  /**
   * The squared distance between this vector and the given coordinates
   * @param x the X coordinate to calculate the distance to
   * @param y the Y coordinate to calculate the distance to
   * @return the squared distance between this vector and the given coordinates
   *         (this - (x, y)).magSq
   */
  def distanceSq(x: Double, y: Double) = {
    (this -(x, y)).magSq
  }

  /**
   * The squared distance between this vector and the given coordinates
   * @param coords the coordinates to calculate the distance to
   * @return the squared distance between this vector and the given coordinates
   *         (this - coords).magSq
   */
  def distanceSq(coords: (Double, Double)) = {
    (this - coords).magSq
  }

  /**
   * Calculate the dot product of this vector with another
   * @param x the X coordinate of the other vector
   * @param y the Y coordinate of the other vector
   * @return the dot product of the two vectors
   */
  def dotProduct(x: Double, y: Double): Double = dotProduct(Vector2(x, y))

  /**
   * Calculate the dot product of this vector with another
   * @param coords a tuple representing the other vector
   * @return the dot product of the two vectors
   */
  def dotProduct(coords: (Double, Double)): Double = dotProduct(Vector2(coords))

  /**
   * Calculate the dot product of this vector with another
   * @param vec the other vector
   * @return the dot product of the two vectors
   */
  def dotProduct(vec: Vector2): Double = {
    var d: Double = vec.x * x + vec.y * y
    if (d > 1 && d < 1.00001) d = 1
    else if (d < -1 && d > -1.00001) d = -1
    d
  }

  /**
   * @return true if all components are 0
   */
  def isZero: Boolean = {
    x == 0 && y == 0
  }

  /**
   * @return true if this vector is aligned axial
   */
  def isAxial: Boolean = {
    if (x == 0) y == 0 else y == 0
  }

  override def equals(that: Any): Boolean = {
    that match {
      case Vector2(thatX, thatY) => return thatX == x && thatY == y
      case (thatX, thatY) => return thatX == x && thatY == y
    }
    false
  }

  override def compare(o: Vector2): Int = {
    if (x != o.x) return if (x < o.x) 1 else -1
    if (y != o.y) return if (y < o.y) 1 else -1
    0
  }

  override def hashCode = _hashCode

  override def toString = "( " + x + ", " + y + " )"
}

