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

/**
 * VectorLike
 *
 * @author PaleoCrafter
 */
trait VectorLike[T <: VectorLike[T]] extends Ordered[T] {
  this: T =>
  /**
   * Add the given coordinates to this vector
   * @param vec another vector to add
   * @return a new (cached) vector with the sum of the coordinates
   */
  def +(vec: T): T

  /**
   * Subtract the given coordinates from this vector
   * @param vec another vector to subtract
   * @return a new (cached) vector with the difference of the coordinates
   */
  def -(vec: T): T = this + -vec

  /**
   * Multiply the coordinates of this vector
   * @param scalar a plain value every component of the vector will be multiplied with
   * @return a new (cached) vector with the product of this vector with the scalar
   */
  def *(scalar: Double): T

  /**
   * Multiply the given coordinates with this vector
   * @param vec a tuple representing the coordinates to multiply with
   * @return a new (cached) vector with the product of the form
   *         (this.c0 * vec.c0, this.c1 * vec.c1, .. this.cN * vec.cN)
   */
  def *(vec: T): T

  def unary_+ = this

  def unary_- : T

  /**
   * @return the magnitude (length) of this vector
   */
  def mag = math.sqrt(magSq)

  /**
   * @return the squared magnitude (length) of this vector
   */
  def magSq: Double

  /**
   * The distance between this vector and the given coordinates
   * @param vec the coordinates to calculate the distance to
   * @return the distance between this vector and the given coordinates
   *         (this - vec).mag
   */
  def distance(vec: T) = (this - vec).mag

  /**
   * The squared distance between this vector and the given coordinates
   * @param vec the coordinates to calculate the distance to
   * @return the squared distance between this vector and the given coordinates
   *         (this - vec).magSq
   */
  def distanceSq(vec: T) = (this - vec).magSq

  /**
   * Calculate the dot product of this vector with another
   * @param vec the other vector
   * @return the dot product of the two vectors
   */
  def dotProduct(vec: T): Double

  /**
   * Normalize this vector
   * @return a new (cached) vector, the same as this one, just normalized
   */
  def normalize: T = {
    val d = mag
    if (d != 0)
      this * (1 / d)
    else
      this
  }

  /**
   * @return true if all components are 0
   */
  def isZero: Boolean

  /**
   * @return true if this vector is aligned axial
   */
  def isAxial: Boolean
}
