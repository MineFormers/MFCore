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

package de.mineformers.core.structure

import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection

/**
 * Layer
 *
 * @author PaleoCrafter
 */
class Layer(val width: Int, val length: Int) {
  private[structure] val blocks = Array.ofDim[BlockInfo](width, length)

  def set(x: Int, z: Int, block: Block, metadata: Int) {
    set(x, z, block, metadata, null)
  }

  def set(x: Int, z: Int, block: Block, metadata: Int, tileEntity: NBTTagCompound) {
    val clampedX: Int = clampX(x)
    val clampedZ: Int = clampZ(z)
    set(new SimpleBlockInfo(block, clampedX, clampedZ, (metadata & 0xF).asInstanceOf[Byte], tileEntity))
  }

  def set(info: BlockInfo) {
    blocks(info.x)(info.z) = info
  }

  def remove(x: Int, z: Int) {
    blocks(clampX(x))(clampZ(z)) = null
  }

  def get(x: Int, z: Int): BlockInfo = {
    if (x >= width || z >= length || x < 0 || z < 0) return null
    var info: BlockInfo = blocks(x)(z)
    if (info == null) {
      info = new SimpleBlockInfo(Blocks.air, x, z, 0.asInstanceOf[Byte])
      set(info)
    }
    info
  }

  private def clampX(x: Int): Int = math.min(math.max(x, 0), width - 1)

  private def clampZ(z: Int): Int = math.min(math.max(z, 0), length - 1)

  def copy: Layer = {
    val layer: Layer = new Layer(width, length)
    for (x <- 0 until width; z <- 0 until length) {
      val info = get(x, z)
      if (info != null) {
        layer.set(info.copy)
      }
    }
    layer
  }

  override def toString: String = {
    var s: String = "Layer=["
    s += blocks.deep.mkString(",")
    s += "]"
    s
  }
}

abstract class BlockInfo(private var _x: Int, private var _z: Int) {
  def x = _x

  def z = _z

  private[structure] def x_=(x: Int): Unit = _x = x

  private[structure] def z_=(z: Int): Unit = _z = z

  def getEntry: BlockEntry

  def getBlock = getEntry.block

  def getMetadata = getEntry.metadata

  def getTileEntity = getEntry.tile

  def setBlock(block: Block): Unit = getEntry.block = block

  def setMetadata(metadata: Int): Unit = getEntry.metadata = (metadata & 0xF).toByte

  def setTile(tile: NBTTagCompound): Unit = getEntry.tile = tile

  def update(world: StructureWorld, y: Int): Unit = ()

  def getTranslatedTileEntity(x: Int, y: Int, z: Int): NBTTagCompound = {
    if (getEntry.tile != null) {
      val translated: NBTTagCompound = getEntry.tile.copy.asInstanceOf[NBTTagCompound]
      translated.setInteger("x", translated.getInteger("x") + x)
      translated.setInteger("y", translated.getInteger("y") + y)
      translated.setInteger("z", translated.getInteger("z") + z)
      translated
    } else
      null
  }

  def rotate(world: World, y: Int, axis: ForgeDirection): Unit

  def copy: BlockInfo

  override def toString: String = "BlockInfo=[pos=(" + x + "," + z + "), block=" + this.getEntry.block + ", metadata=" + this.getEntry.metadata + "]"
}

class SimpleBlockInfo(private var block: Block, _x: Int, _z: Int, metadata: Byte, tile: NBTTagCompound) extends BlockInfo(_x, _z) {
  if (tile != null) {
    tile.setInteger("x", x)
    tile.setInteger("z", z)
  }
  private var entry = BlockEntry(block, metadata, tile)

  def getEntry = entry

  def this(block: Block, x: Int, z: Int, metadata: Byte) = this(block, x, z, metadata, null)

  override def rotate(world: World, y: Int, axis: ForgeDirection): Unit = {
    block.rotateBlock(world, x, y, z, axis)
    entry = BlockEntry(block, metadata, tile)
  }

  def copy: BlockInfo = new SimpleBlockInfo(block, x, z, metadata, tile)
}

object BlockEntry {
  def apply(block: Block, metadata: Int): BlockEntry = apply(block, (metadata & 0xF).toByte, null)
}

case class BlockEntry(var block: Block, var metadata: Byte, var tile: NBTTagCompound) {
  def this(block: Block, metadata: Byte) = this(block, metadata, null)
}