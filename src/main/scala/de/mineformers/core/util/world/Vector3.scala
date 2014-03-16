package de.mineformers.core.util.world

import scala.collection.mutable
import com.google.common.base.Objects

object Vector3 {
  private val cache = mutable.HashMap[(Double, Double, Double), Vector3]()

  val Zero = Vector3(0, 0, 0)
  val One = Vector3(1, 1, 1)
  val Center = Vector3(0.5D, 0.5D, 0.5D)

  def apply(x: Double, y: Double, z: Double): Vector3 = apply((x, y, z))

  def apply(coords: (Double, Double, Double)): Vector3 = cache.getOrElseUpdate(coords, new Vector3(coords))
}

class Vector3(coords: (Double, Double, Double)) extends Comparable[BlockPos] {

  var _hashCode = 0

  def x: Double = coords._1

  def y: Double = coords._2

  def z: Double = coords._3

  def +(x: Double, y: Double, z: Double) = Vector3(coords._1 + x, coords._2 + y, coords._3 + z)

  def +(coords: (Double, Double, Double)) = Vector3(coords._1 + x, coords._2 + y, coords._3 + z)

  def +(pos: Vector3) = Vector3(coords._1 + pos.x, coords._2 + pos.y, coords._3 + pos.z)

  def -(x: Double, y: Double, z: Double) = Vector3(coords._1 - x, coords._2 - y, coords._3 - z)

  def -(coords: (Double, Double, Double)) = Vector3(x - coords._1, y - coords._2, z - coords._3)

  def -(pos: Vector3) = Vector3(coords._1 - pos.x, coords._2 - pos.y, coords._3 - pos.z)

  def *(scalar: Double) = Vector3(x * scalar, y * scalar, z * scalar)

  def *(x: Double, y: Double, z: Double) = Vector3(coords._1 * x, coords._2 * y, coords._3 * z)

  def *(coords: (Double, Double, Double)) = Vector3(coords._1 * x, coords._2 * y, coords._3 * z)

  def *(pos: Vector3) = Vector3(coords._1 * pos.x, coords._2 * pos.y, coords._3 * pos.z)

  override def equals(that: Any): Boolean = {
    that match {
      case pos: BlockPos => return pos.x == x && pos.y == y && pos.z == z
      case coords: (Double, Double, Double) => return coords._1 == x && coords._2 == y && coords._3 == z
    }
    false
  }

  override def compareTo(o: BlockPos): Int = {
    if (x != o.x) return if (x < o.x) 1 else -1
    if (y != o.y) return if (y < o.y) 1 else -1
    if (z != o.z) return if (z < o.z) 1 else -1
    0
  }

  def mag = {
    math.sqrt(magSq)
  }

  def magSq = {
    x * x + y * y + z * z
  }

  def distance(x: Int, y: Int, z: Int) = {
    (this -(x, y, z)).mag
  }

  def distance(coords: (Double, Double, Double)) = {
    (this - coords).mag
  }

  def distance(pos: Vector3) = {
    (this - pos).mag
  }

  def distanceSq(x: Double, y: Double, z: Double) = {
    (this -(x, y, z)).magSq
  }

  def distanceSq(coords: (Double, Double, Double)) = {
    (this - coords).magSq
  }

  def distanceSq(pos: Vector3) = {
    (this - pos).magSq
  }

  def dotProduct(x: Double, y: Double, z: Double): Double = dotProduct(Vector3(x, y, z))

  def dotProduct(coords: (Double, Double, Double)): Double = dotProduct(Vector3(coords))

  def dotProduct(vec: Vector3): Double = {
    var d: Double = vec.x * x + vec.y * y + vec.z * z
    if (d > 1 && d < 1.00001) d = 1
    else if (d < -1 && d > -1.00001) d = -1
    d
  }

  def crossProduct(x: Double, y: Double, z: Double): Vector3 = crossProduct(Vector3(x, y, z))

  def crossProduct(coords: (Double, Double, Double)): Vector3 = crossProduct(Vector3(coords))

  def crossProduct(vec: Vector3): Vector3 = {
    val d: Double = y * vec.z - z * vec.y
    val d1: Double = z * vec.x - x * vec.z
    val d2: Double = x * vec.y - y * vec.x
    Vector3(d, d1, d2)
  }

  def normalize: Vector3 = {
    val d = mag
    if (d != 0)
      this * (1 / d)
    else
      this
  }

  def isZero: Boolean = {
    x == 0 && y == 0 && z == 0
  }

  def isAxial: Boolean = {
    if (x == 0) y == 0 || z == 0 else y == 0 && z == 0
  }

  override def hashCode() = {
    if (_hashCode == 0) _hashCode = Objects.hashCode(java.lang.Double.valueOf(x), java.lang.Double.valueOf(y), java.lang.Double.valueOf(z))
    _hashCode
  }

  override def toString = "( " + x + ", " + y + ", " + z + " )"

}

