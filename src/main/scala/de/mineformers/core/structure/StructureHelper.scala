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
import net.minecraft.entity.{EntityList, Entity}
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagDouble
import net.minecraft.nbt.NBTTagList
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants
import net.minecraft.util.AxisAlignedBB
import de.mineformers.core.util.Implicits._
import net.minecraft.entity.player.EntityPlayer

/**
 * StructureHelper
 *
 * @author PaleoCrafter
 */
object StructureHelper {
  def pasteStructure(world: World, x: Int, y: Int, z: Int, structure: Structure, pasteAir: Boolean, spawnEntities: Boolean) {
    for (y1 <- 0 until structure.getHeight) {
      val layer: Layer = structure.getLayer(y1)
      for (x1 <- 0 until structure.getWidth; z1 <- 0 until structure.getLength) {
        val info: BlockInfo = layer.get(x1, z1)
        if (info != null && (pasteAir || (info.getBlock ne Blocks.air))) {
          world.setBlock(x + x1, y + y1, z + z1, info.getBlock, info.getMetadata, 2)
          if (info.getTileEntity != null && world.getTileEntity(x + x1, y + y1, z + z1) != null) {
            world.getTileEntity(x + x1, y + y1, z + z1).readFromNBT(info.getTranslatedTileEntity(x, y, z))
            world.markBlockForUpdate(x + x1, y + y1, z + z1)
          }
        }
      }
    }
    if (spawnEntities) {
      val posAdd = Array(x, y, z)
      for (nbt <- structure.getEntities) {
        val newTag: NBTTagCompound = nbt.copy.asInstanceOf[NBTTagCompound]
        val pos: NBTTagList = newTag.getTagList("Pos", Constants.NBT.TAG_DOUBLE)
        val newPos: NBTTagList = new NBTTagList
        for (i <- 0 until pos.tagCount) {
          newPos.appendTag(new NBTTagDouble(pos.func_150309_d(i) + posAdd(i)))
        }
        newTag.setTag("Pos", newPos)
        val e: Entity = EntityList.createEntityFromNBT(newTag, world)
        world.spawnEntityInWorld(e)
      }
    }
  }

  def createFromRegion(world: World, bounds: AxisAlignedBB, includeEntities: Boolean): Structure = {
    val structure: Structure = new Structure
    val minX: Int = bounds.minX.toInt
    val minY: Int = bounds.minY.toInt
    val minZ: Int = bounds.minZ.toInt
    val maxX: Int = bounds.maxX.toInt
    val maxY: Int = bounds.maxY.toInt
    val maxZ: Int = bounds.maxZ.toInt
    for (y <- minY to maxY) {
      val layer: Layer = new Layer((maxX - minX) + 1, (maxZ - minZ) + 1)
      for (x <- minX to maxX; z <- minZ to maxZ) {
        if (world.getBlock(x, y, z) != null) {
          val block: Block = world.getBlock(x, y, z)
          val meta: Int = world.getBlockMetadata(x, y, z)
          val tile: TileEntity = world.getTileEntity(x, y, z)
          val tag: NBTTagCompound = if (tile != null) new NBTTagCompound else null
          if (tag != null) {
            tile.writeToNBT(tag)
            updateTileCoordinates(tag, x - minX, y - minY, z - minZ)
          }
          layer.set(x - minX, z - minZ, block, meta, tag)
        }
        else layer.set(x - minX, y - minY, Blocks.air, 0)
      }
      structure.addLayer(layer)
    }

    if (includeEntities) {
      val mins = Array(minX, minY, minZ)
      val entities = world.selectEntities(bounds, classOf[Entity])({
        case e: EntityPlayer => false
        case _ => true
      })
      for (e <- entities) {
        val tag: NBTTagCompound = new NBTTagCompound
        var valid: Boolean = e.writeToNBTOptional(tag)
        if (!valid) valid = e.writeMountToNBT(tag)
        if (valid) {
          val pos: NBTTagList = tag.getTagList("Pos", Constants.NBT.TAG_DOUBLE)
          val newPos: NBTTagList = new NBTTagList
          for (i <- 0 until pos.tagCount())
            newPos.appendTag(new NBTTagDouble(pos.func_150309_d(i) - mins(i)))
          tag.setTag("Pos", newPos)
        }
        structure.addEntity(tag)
      }
    }
    structure
  }

  def updateTileCoordinates(tag: NBTTagCompound, x: Int, y: Int, z: Int) {
    tag.setInteger("x", x)
    tag.setInteger("y", y)
    tag.setInteger("z", z)
  }
}