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

import scala.collection.mutable
import com.google.common.base.Objects
import java.lang.{Integer => JInt}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{AxisAlignedBB, Vec3}

/**
 * BlockPos
 * Manages the pool of block positions
 *
 * @author PaleoCrafter
 */
object BlockPos {
  private val cache = mutable.WeakHashMap[(Int, Int, Int), BlockPos]()

  val Zero = BlockPos(0, 0, 0)
  val One = BlockPos(1, 1, 1)

  def apply(tag: NBTTagCompound): BlockPos = apply(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"))

  /**
   * Create a new [[BlockPos]] based on the given coordinates
   * @param x the X of the new block position
   * @param y the Y of the new block position
   * @param z the Z of the new block position
   * @return a [[BlockPos]] instance, either a new one or one from the cache
   */
  def apply(x: Int, y: Int, z: Int): BlockPos = apply((x, y, z))

  /**
   * Create a new [[BlockPos]] based on the given coordinates
   * @param coords a tuple of the coordinates in the form (x, y, z)
   * @return a [[BlockPos]] instance, either a new one or one from the cache
   */
  def apply(coords: (Int, Int, Int)): BlockPos = cache.getOrElseUpdate(coords, new BlockPos(coords))

  def unapply(pos: BlockPos): Option[(Int, Int, Int)] = Some((pos.x, pos.y, pos.z))

  def fromArray(array: Array[Int]): BlockPos = {
    require(array.size >= 3, "BlockPos consists of 3 components")
    BlockPos(array(0), array(1), array(2))
  }
}

/**
 * Don't use this constructor, use BlockPos(x, y, z) for caching!
 *
 * @param coords a tuple representing the position's coordinates
 */
class BlockPos private(coords: (Int, Int, Int)) extends Ordered[BlockPos] {
  val (x, y, z) = coords
  private val _hashCode = Objects.hashCode(JInt.valueOf(x), JInt.valueOf(y), JInt.valueOf(z))

  /**
   * Add the given coordinates to this block pos
   * @param x the X component to add
   * @param y the Y component to add
   * @param z the Z component to add
   * @return a new (cached) BlockPos with the sum of the coordinates
   */
  def +(x: Int, y: Int, z: Int): BlockPos = this + BlockPos(x, y, z)

  /**
   * Add the given coordinates to this block pos
   * @param coords a tuple representing the coordinates to add
   * @return a new (cached) BlockPos with the sum of the coordinates
   */
  def +(coords: (Int, Int, Int)): BlockPos = this + BlockPos(coords)

  /**
   * Add the given coordinates to this block pos
   * @param pos another BlockPos to add
   * @return a new (cached) BlockPos with the sum of the coordinates
   */
  def +(pos: BlockPos): BlockPos = BlockPos(this.x + pos.x, this.y + pos.y, this.z + pos.z)

  /**
   * Subtract the given coordinates from this block pos
   * @param x the X component to subtract
   * @param y the Y component to subtract
   * @param z the Z component to subtract
   * @return a new (cached) BlockPos with the difference of the coordinates
   */
  def -(x: Int, y: Int, z: Int): BlockPos = this + BlockPos(x, y, z)

  /**
   * Subtract the given coordinates from this block pos
   * @param coords a tuple representing the coordinates to subtract
   * @return a new (cached) BlockPos with the difference of the coordinates
   */
  def -(coords: (Int, Int, Int)): BlockPos = this + BlockPos(coords)

  /**
   * Subtract the given coordinates from this block pos
   * @param pos another BlockPos to subtract
   * @return a new (cached) BlockPos with the difference of the coordinates
   */
  def -(pos: BlockPos): BlockPos = this + -pos

  /**
   * Multiply the coordinates of this block pos
   * @param scalar a plain value every component of the BlockPos will be multiplied with
   * @return a new (cached) BlockPos with the product of this position with the scalar
   */
  def *(scalar: Int): BlockPos = BlockPos(x * scalar, y * scalar, z * scalar)

  /**
   * Multiply the given coordinates with this block pos
   * @param x the X coordinate to multiply with
   * @param y the Y coordinate to multiply with
   * @param z the Z coordinate to multiply with
   * @return a new (cached) BlockPos with the product of the form
   *         (this.x * x, this.y * y, this.z * z)
   */
  def *(x: Int, y: Int, z: Int): BlockPos = this * BlockPos(x, y, z)

  /**
   * Multiply the given coordinates with this block pos
   * @param coords a tuple representing the coordinates to multiply with
   * @return a new (cached) BlockPos with the product of the form
   *         (this.x * coords.x, this.y * coords.y, this.z * coords.z)
   */
  def *(coords: (Int, Int, Int)): BlockPos = this * BlockPos(coords)

  /**
   * Multiply the given coordinates with this block pos
   * @param pos a tuple representing the coordinates to multiply with
   * @return a new (cached) BlockPos with the product of the form
   *         (this.x * pos.x, this.y * pos.y, this.z * pos.z)
   */
  def *(pos: BlockPos): BlockPos = BlockPos(this.x * pos.x, this.y * pos.y, this.z * pos.z)

  def unary_+ = this

  def unary_- = BlockPos(-x, -y, -z)

  /**
   * @return the magnitude (length) of this block pos
   */
  def mag = {
    math.sqrt(magSq)
  }

  /**
   * @return the squared magnitude (length) of this block pos
   */
  def magSq = {
    x * x + y * y + z * z
  }

  /**
   * The distance between this block pos and the given coordinates
   * @param x the X coordinate to calculate the distance to
   * @param y the Y coordinate to calculate the distance to
   * @param z the Z coordinate to calculate the distance to
   * @return the distance between this position and the given coordinates
   *         (this - (x, y, z)).mag
   */
  def distance(x: Int, y: Int, z: Int) = {
    (this -(x, y, z)).mag
  }

  /**
   * The distance between this block pos and the given coordinates
   * @param coords the coordinates to calculate the distance to
   * @return the distance between this position and the given coordinates
   *         (this - coords).mag
   */
  def distance(coords: (Int, Int, Int)) = {
    (this - coords).mag
  }

  /**
   * The distance between this block pos and the given coordinates
   * @param pos the coordinates to calculate the distance to
   * @return the distance between this position and the given coordinates
   *         (this - pos).mag
   */
  def distance(pos: BlockPos) = {
    (this - pos).mag
  }

  /**
   * The squared distance between this block pos and the given coordinates
   * @param x the X coordinate to calculate the distance to
   * @param y the Y coordinate to calculate the distance to
   * @param z the Z coordinate to calculate the distance to
   * @return the squared distance between this position and the given coordinates
   *         (this - (x, y, z)).magSq
   */
  def distanceSq(x: Int, y: Int, z: Int) = {
    (this -(x, y, z)).magSq
  }

  /**
   * The squared distance between this block pos and the given coordinates
   * @param coords the coordinates to calculate the distance to
   * @return the squared distance between this position and the given coordinates
   *         (this - coords).magSq
   */
  def distanceSq(coords: (Int, Int, Int)) = {
    (this - coords).magSq
  }

  /**
   * The squared distance between this block pos and the given coordinates
   * @param pos the coordinates to calculate the distance to
   * @return the squared distance between this position and the given coordinates
   *         (this - pos).magSq
   */
  def distanceSq(pos: BlockPos) = {
    (this - pos).magSq
  }

  /**
   * @return true if all components are 0
   */
  def isZero: Boolean = {
    x == 0 && y == 0 && z == 0
  }

  /**
   * @return true if this block position is aligned axial
   */
  def isAxial: Boolean = {
    if (x == 0) y == 0 || z == 0 else y == 0 && z == 0
  }

  def toVec3 = Vec3.createVectorHelper(x, y, z)

  def toVector = Vector3(x, y, z)

  def toArray = Array(x, y, z)

  def containedBy(bounds: AxisAlignedBB): Boolean = bounds.minX <= x && bounds.minY <= y && bounds.minZ <= z && bounds.maxX > x && bounds.maxY > y && bounds.maxZ > z

  def sharesChunk(bounds: AxisAlignedBB): Boolean = {
    def toChunkD(d: Double): Int = (d.toInt / 16) * 16
    def toChunkI(i: Int): Int = (i % 16) * 16
    val chunkX = toChunkI(x)
    val chunkZ = toChunkI(z)
    toChunkD(bounds.minX) <= chunkX && toChunkD(bounds.minZ) <= chunkZ && toChunkD(bounds.maxX) >= chunkX && toChunkD(bounds.maxZ) >= chunkZ
  }

  override def hashCode = _hashCode

  override def equals(that: Any): Boolean = {
    that match {
      case BlockPos(thatX, thatY, thatZ) => return this.x == thatX && this.y == thatY && this.z == thatZ
      case (thatX, thatY, thatZ) => return this.x == thatX && this.y == thatY && this.z == thatZ
    }
    false
  }

  override def compare(o: BlockPos): Int = {
    if (x != o.x) return if (x < o.x) 1 else -1
    if (y != o.y) return if (y < o.y) 1 else -1
    if (z != o.z) return if (z < o.z) 1 else -1
    0
  }

  override def toString = "( " + x + ", " + y + ", " + z + " )"
}
