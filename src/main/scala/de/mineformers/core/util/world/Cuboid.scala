package de.mineformers.core.util.world

import de.mineformers.core.util.math.Vector3
import net.minecraft.util.AxisAlignedBB

/**
 * Cuboid
 *
 * @author PaleoCrafter
 */
sealed trait Cuboid[N, C <: Coord3[N]] extends Traversable[C] {
  def coordFactory: Coord3Factory[N, C]

  def point1: C

  def point2: C

  def lowerCorner: C = coordFactory(point1.numeric.min(point1.x, point2.x), point1.numeric.min(point1.y, point2.y), point1.numeric.min(point1.z, point2.z))

  def higherCorner: C = coordFactory(point1.numeric.max(point1.x, point2.x), point1.numeric.max(point1.y, point2.y), point1.numeric.max(point1.z, point2.z))

  def width: N = {
    val numeric = point1.numeric
    val lowerCorner = this.lowerCorner
    val higherCorner = this.higherCorner
    numeric.plus(numeric.fromInt(1), numeric.minus(higherCorner.x, lowerCorner.x))
  }

  def height: N = {
    val numeric = point1.numeric
    val lowerCorner = this.lowerCorner
    val higherCorner = this.higherCorner
    numeric.plus(numeric.fromInt(1), numeric.minus(higherCorner.y, lowerCorner.y))
  }

  def length: N = {
    val numeric = point1.numeric
    val lowerCorner = this.lowerCorner
    val higherCorner = this.higherCorner
    numeric.plus(numeric.fromInt(1), numeric.minus(higherCorner.z, lowerCorner.z))
  }

  def volume: N = point1.numeric.times(width, point1.numeric.times(height, length))

  def contains(coord: C) = {
    val numeric = point1.numeric
    val lowerCorner = this.lowerCorner
    val higherCorner = this.higherCorner
    numeric.gteq(coord.x, lowerCorner.x) && numeric.gteq(coord.y, lowerCorner.y) && numeric.gteq(coord.z, lowerCorner.z) &&
      numeric.lteq(coord.x, higherCorner.x) && numeric.lteq(coord.y, higherCorner.y) && numeric.lteq(coord.z, higherCorner.z)
  }

  def local(coord: C) = {
    val numeric = point1.numeric
    val lowerCorner = this.lowerCorner
    coordFactory(numeric.minus(coord.x, lowerCorner.x), numeric.minus(coord.y, lowerCorner.y), numeric.minus(coord.z, lowerCorner.z))
  }

  override def foreach[U](op: C => U) = {
    val lowerCorner = this.lowerCorner
    val higherCorner = this.higherCorner
    val numeric = lowerCorner.numeric
    for (x <- numeric.toInt(lowerCorner.x) to numeric.toInt(higherCorner.x);
         y <- numeric.toInt(lowerCorner.y) to numeric.toInt(higherCorner.y);
         z <- numeric.toInt(lowerCorner.z) to numeric.toInt(higherCorner.z))
      op(coordFactory(numeric.fromInt(x), numeric.fromInt(y), numeric.fromInt(z)))
  }
}

object Cuboid {
  def from(aabb: AxisAlignedBB) = AxisAlignedCuboid(Vector3(aabb.minX, aabb.minY, aabb.minZ), Vector3(aabb.maxX, aabb.maxY, aabb.maxZ))
}

case class BlockCuboid(point1: BlockPos, point2: BlockPos) extends Cuboid[Int, BlockPos] {
  override def coordFactory: Coord3Factory[Int, BlockPos] = BlockPos
}

case class AxisAlignedCuboid(point1: Vector3, point2: Vector3) extends Cuboid[Double, Vector3] {
  override def coordFactory: Coord3Factory[Double, Vector3] = Vector3
}