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

import net.minecraft.entity.Entity

import scala.collection.mutable
import com.google.common.base.Objects
import java.lang.{Double => JDouble}

/**
 * Vector3
 * Manages every vector and caches them
 *
 * @author PaleoCrafter
 */
object Vector3 {
  private val cache = mutable.WeakHashMap[(Double, Double, Double), Vector3]()

  val Zero = Vector3(0, 0, 0)
  val One = Vector3(1, 1, 1)
  val Center = Vector3(0.5D, 0.5D, 0.5D)

  /**
   * Create a new [[Vector3]] based on the given coordinates
   * @param x the X of the new vector
   * @param y the Y of the new vector
   * @param z the Z of the new vector
   * @return a [[Vector3]] instance, either a new one or one from the cache
   */
  def apply(x: Double, y: Double, z: Double): Vector3 = apply((x, y, z))

  /**
   * Create a new [[Vector3]] based on the given coordinates
   * @param coords a tuple of the coordinates in the form (x, y, z)
   * @return a [[Vector3]] instance, either a new one or one from the cache
   */
  def apply(coords: (Double, Double, Double)): Vector3 = cache.getOrElseUpdate(coords, new Vector3(coords))

  def unapply(vec: Vector3): Option[(Double, Double, Double)] = Some((vec.x, vec.y, vec.z))

  def fromEntityCenter(e: Entity) = Vector3(e.posX, e.posY - e.yOffset + e.height / 2.0F, e.posZ)
}

/**
 * Don't use! Use Vector3(x, y, z) for caching!
 * @param coords a tuple representing the coordinates of this vector
 */
class Vector3 private(coords: (Double, Double, Double)) extends VectorLike[Vector3] {
  val (x, y, z) = coords
  private val _hashCode = Objects.hashCode(JDouble.valueOf(x), JDouble.valueOf(y), JDouble.valueOf(z))

  /**
   * Add the given coordinates to this vector
   * @param x the X component to add
   * @param y the Y component to add
   * @param z the Z component to add
   * @return a new (cached) Vector3 with the sum of the coordinates
   */
  def +(x: Double, y: Double, z: Double): Vector3 = this + Vector3(x, y, z)

  /**
   * Add the given coordinates to this vector
   * @param coords a tuple representing the coordinates to add
   * @return a new (cached) Vector3 with the sum of the coordinates
   */
  def +(coords: (Double, Double, Double)): Vector3 = this + Vector3(coords)

  /**
   * Add the given coordinates to this vector
   * @param vec another Vector3 to add
   * @return a new (cached) Vector3 with the sum of the coordinates
   */
  def +(vec: Vector3): Vector3 = Vector3(x + vec.x, y + vec.y, z + vec.z)

  /**
   * Subtract the given coordinates from this vector
   * @param x the X component to subtract
   * @param y the Y component to subtract
   * @param z the Z component to subtract
   * @return a new (cached) Vector3 with the difference of the coordinates
   */
  def -(x: Double, y: Double, z: Double): Vector3 = this - Vector3(x, y, z)

  /**
   * Subtract the given coordinates from this vector
   * @param coords a tuple representing the coordinates to subtract
   * @return a new (cached) Vector3 with the difference of the coordinates
   */
  def -(coords: (Double, Double, Double)): Vector3 = this - Vector3(coords)

  /**
   * Multiply the coordinates of this vector
   * @param scalar a plain value every component of the Vector3 will be multiplied with
   * @return a new (cached) Vector3 with the product of this vector with the scalar
   */
  def *(scalar: Double): Vector3 = Vector3(x * scalar, y * scalar, z * scalar)

  /**
   * Multiply the given coordinates with this vector
   * @param x the X coordinate to multiply with
   * @param y the Y coordinate to multiply with
   * @param z the Z coordinate to multiply with
   * @return a new (cached) Vector3 with the product of the form
   *         (this.x * x, this.y * y, this.z * z)
   */
  def *(x: Double, y: Double, z: Double): Vector3 = this * Vector3(x, y, z)

  /**
   * Multiply the given coordinates with this vector
   * @param coords a tuple representing the coordinates to multiply with
   * @return a new (cached) Vector3 with the product of the form
   *         (this.x * coords.x, this.y * coords.y, this.z * coords.z)
   */
  def *(coords: (Double, Double, Double)): Vector3 = this * Vector3(coords)

  /**
   * Multiply the given coordinates with this vector
   * @param vec a tuple representing the coordinates to multiply with
   * @return a new (cached) Vector3 with the product of the form
   *         (this.x * vec.x, this.y * vec.y, this.z * vec.z)
   */
  def *(vec: Vector3): Vector3 = Vector3(x * vec.x, y * vec.y, y * vec.z)

  def unary_- = Vector3(-x, -y, -z)

  /**
   * @return the squared magnitude (length) of this vector
   */
  def magSq = {
    x * x + y * y + z * z
  }

  /**
   * The distance between this vector and the given coordinates
   * @param x the X coordinate to calculate the distance to
   * @param y the Y coordinate to calculate the distance to
   * @param z the Z coordinate to calculate the distance to
   * @return the distance between this vector and the given coordinates
   *         (this - (x, y, z)).mag
   */
  def distance(x: Int, y: Int, z: Int) = {
    (this -(x, y, z)).mag
  }

  /**
   * The distance between this vector and the given coordinates
   * @param coords the coordinates to calculate the distance to
   * @return the distance between this vector and the given coordinates
   *         (this - coords).mag
   */
  def distance(coords: (Double, Double, Double)) = {
    (this - coords).mag
  }

  /**
   * The squared distance between this vector and the given coordinates
   * @param x the X coordinate to calculate the distance to
   * @param y the Y coordinate to calculate the distance to
   * @param z the Z coordinate to calculate the distance to
   * @return the squared distance between this vector and the given coordinates
   *         (this - (x, y, z)).magSq
   */
  def distanceSq(x: Double, y: Double, z: Double) = {
    (this -(x, y, z)).magSq
  }

  /**
   * The squared distance between this vector and the given coordinates
   * @param coords the coordinates to calculate the distance to
   * @return the squared distance between this vector and the given coordinates
   *         (this - coords).magSq
   */
  def distanceSq(coords: (Double, Double, Double)) = {
    (this - coords).magSq
  }

  /**
   * Calculate the dot product of this vector with another
   * @param x the X coordinate of the other vector
   * @param y the Y coordinate of the other vector
   * @param z the Z coordinate of the other vector
   * @return the dot product of the two vectors
   */
  def dotProduct(x: Double, y: Double, z: Double): Double = dotProduct(Vector3(x, y, z))

  /**
   * Calculate the dot product of this vector with another
   * @param coords a tuple representing the other vector
   * @return the dot product of the two vectors
   */
  def dotProduct(coords: (Double, Double, Double)): Double = dotProduct(Vector3(coords))

  /**
   * Calculate the dot product of this vector with another
   * @param vec the other vector
   * @return the dot product of the two vectors
   */
  def dotProduct(vec: Vector3): Double = {
    var d: Double = vec.x * x + vec.y * y + vec.z * z
    if (d > 1 && d < 1.00001) d = 1
    else if (d < -1 && d > -1.00001) d = -1
    d
  }

  /**
   * Calculate the cross product of this vector with another
   * @param x the X coordinate of the other vector
   * @param y the Y coordinate of the other vector
   * @param z the Z coordinate of the other vector
   * @return the cross product of the two vectors
   */
  def crossProduct(x: Double, y: Double, z: Double): Vector3 = crossProduct(Vector3(x, y, z))

  /**
   * Calculate the cross product of this vector with another
   * @param coords a tuple representing the other vector
   * @return the cross product of the two vectors
   */
  def crossProduct(coords: (Double, Double, Double)): Vector3 = crossProduct(Vector3(coords))

  /**
   * Calculate the cross product of this vector with another
   * @param vec the other vector
   * @return the cross product of the two vectors
   */
  def crossProduct(vec: Vector3): Vector3 = {
    val d: Double = y * vec.z - z * vec.y
    val d1: Double = z * vec.x - x * vec.z
    val d2: Double = x * vec.y - y * vec.x
    Vector3(d, d1, d2)
  }

  def rotationMatrix(angle: Float) = {
    val matrix = Array.ofDim[Double](16)
    val axis = this.normalize
    val x = axis.x
    val y = axis.y
    val z = axis.z
    val a = angle * 0.0174532925D
    val cos = math.cos(a)
    val ocos = 1.0F - cos
    val sin = math.sin(a)
    matrix(0) = x * x * ocos + cos
    matrix(1) = y * x * ocos + z * sin
    matrix(2) = x * z * ocos - y * sin
    matrix(4) = x * y * ocos - z * sin
    matrix(5) = y * y * ocos + cos
    matrix(6) = y * z * ocos + x * sin
    matrix(8) = x * z * ocos + y * sin
    matrix(9) = y * z * ocos - x * sin
    matrix(10) = z * z * ocos + cos
    matrix(15) = 1.0F
    matrix
  }

  def rotate(yaw: Double, pitch: Double, roll: Double): Vector3 = {
    val yawRadians = math.toRadians(yaw)
    val pitchRadians = math.toRadians(pitch)
    val rollRadians = math.toRadians(roll)
    val newX = x * Math.cos(yawRadians) * Math.cos(pitchRadians) + z * (Math.cos(yawRadians) * Math.sin(pitchRadians) * Math.sin(rollRadians) - Math.sin(yawRadians) * Math.cos(rollRadians)) + y * (Math.cos(yawRadians) * Math.sin(pitchRadians) * Math.cos(rollRadians) + Math.sin(yawRadians) * Math.sin(rollRadians))
    val newY = -x * Math.sin(pitchRadians) + z * Math.cos(pitchRadians) * Math.sin(rollRadians) + y * Math.cos(pitchRadians) * Math.cos(rollRadians)
    val newZ = x * Math.sin(yawRadians) * Math.cos(pitchRadians) + z * (Math.sin(yawRadians) * Math.sin(pitchRadians) * Math.sin(rollRadians) + Math.cos(yawRadians) * Math.cos(rollRadians)) + y * (Math.sin(yawRadians) * Math.sin(pitchRadians) * Math.cos(rollRadians) - Math.cos(yawRadians) * Math.sin(rollRadians))
    Vector3(newX, newY, newZ)
  }

  def rotate(angle: Float, axis: Vector3) = translateByMatrix(axis.rotationMatrix(angle))

  def translateByMatrix(matrix: Array[Double]) = {
    val x = this.x * matrix(0) + this.y * matrix(1) + this.z * matrix(2) + matrix(3)
    val y = this.x * matrix(4) + this.y * matrix(5) + this.z * matrix(6) + matrix(7)
    val z = this.x * matrix(8) + this.y * matrix(9) + this.z * matrix(10) + matrix(11)
    Vector3(x, y, z)
  }

  /**
   * @return true if all components are 0
   */
  def isZero: Boolean = {
    x == 0 && y == 0 && z == 0
  }

  /**
   * @return true if this vector is aligned axial
   */
  def isAxial: Boolean = {
    if (x == 0) y == 0 || z == 0 else y == 0 && z == 0
  }

  override def equals(that: Any): Boolean = {
    that match {
      case Vector3(thatX, thatY, thatZ) => return thatX == x && thatY == y && thatZ == z
      case (thatX, thatY, thatZ) => return thatX == x && thatY == y && thatZ == z
    }
    false
  }

  override def compare(o: Vector3): Int = {
    if (x != o.x) return if (x < o.x) 1 else -1
    if (y != o.y) return if (y < o.y) 1 else -1
    if (z != o.z) return if (z < o.z) 1 else -1
    0
  }

  override def hashCode = _hashCode

  override def toString = "( " + x + ", " + y + ", " + z + " )"
}

