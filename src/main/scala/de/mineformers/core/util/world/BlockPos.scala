package de.mineformers.core.util.world

import scala.collection.mutable

object BlockPos {
  private val cache = mutable.HashMap[(Int, Int, Int), BlockPos]()

  val Zero = BlockPos(0, 0, 0)
  val One = BlockPos(1, 1, 1)

  def apply(x: Int, y: Int, z: Int): BlockPos = apply((x, y, z))

  def apply(coords: (Int, Int, Int)): BlockPos = cache.getOrElseUpdate(coords, new BlockPos(coords))
}

class BlockPos(coords: (Int, Int, Int)) extends Comparable[BlockPos] {

  private var _hashCode = 0

  def x: Int = coords._1

  def y: Int = coords._2

  def z: Int = coords._3

  def +(x: Int, y: Int, z: Int) = BlockPos(coords._1 + x, coords._2 + y, coords._3 + z)

  def +(coords: (Int, Int, Int)) = BlockPos(coords._1 + x, coords._2 + y, coords._3 + z)

  def +(pos: BlockPos) = BlockPos(coords._1 + pos.x, coords._2 + pos.y, coords._3 + pos.z)

  def -(x: Int, y: Int, z: Int) = BlockPos(coords._1 - x, coords._2 - y, coords._3 - z)

  def -(coords: (Int, Int, Int)) = BlockPos(x - coords._1, y - coords._2, z - coords._3)

  def -(pos: BlockPos) = BlockPos(coords._1 - pos.x, coords._2 - pos.y, coords._3 - pos.z)

  def *(scalar: Int) = BlockPos(x * scalar, y * scalar, z * scalar)

  def *(x: Int, y: Int, z: Int) = BlockPos(coords._1 * x, coords._2 * y, coords._3 * z)

  def *(coords: (Int, Int, Int)) = BlockPos(coords._1 * x, coords._2 * y, coords._3 * z)

  def *(pos: BlockPos) = BlockPos(coords._1 * pos.x, coords._2 * pos.y, coords._3 * pos.z)

  override def hashCode() = {
    if (_hashCode == 0) _hashCode = (x ^ z) * 31 + y
    _hashCode
  }

  override def equals(that: Any): Boolean = {
    that match {
      case pos: BlockPos => return pos.x == x && pos.y == y && pos.z == z
      case coords: (Int, Int, Int) => return coords._1 == x && coords._2 == y && coords._3 == z
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

  def distance(coords: (Int, Int, Int)) = {
    (this - coords).mag
  }

  def distance(pos: BlockPos) = {
    (this - pos).mag
  }

  def distanceSq(x: Int, y: Int, z: Int) = {
    (this -(x, y, z)).magSq
  }

  def distanceSq(coords: (Int, Int, Int)) = {
    (this - coords).magSq
  }

  def distanceSq(pos: BlockPos) = {
    (this - pos).magSq
  }

  def isZero: Boolean = {
    x == 0 && y == 0 && z == 0
  }

  def isAxial: Boolean = {
    if (x == 0) y == 0 || z == 0 else y == 0 && z == 0
  }

  override def toString = "( " + x + ", " + y + ", " + z + " )"

}
