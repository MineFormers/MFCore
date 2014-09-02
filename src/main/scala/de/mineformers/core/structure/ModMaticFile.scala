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

import de.mineformers.core.util.Log
import net.minecraft.util.ResourceLocation
import net.minecraft.nbt.{NBTTagString, NBTTagList, NBTTagCompound, CompressedStreamTools}
import de.mineformers.core.util.ResourceUtils._
import java.io.IOException
import net.minecraftforge.common.util.Constants.NBT._
import net.minecraft.block.Block
import de.mineformers.core.util.world.BlockPos
import java.nio.{ByteOrder, ByteBuffer, ShortBuffer}
import scala.collection.immutable.HashMap
import net.minecraft.init.Blocks
import scala.collection.mutable

/**
 * ModMaticFile
 *
 * @author PaleoCrafter
 */
object ModMaticFile {
  def load(resource: ResourceLocation): ModMaticFile = {
    try
      new ModMaticFile(CompressedStreamTools.readCompressed(resource.toInputStream))
    catch {
      case e: IOException =>
        Log.error("Error while loading modmatic file", e)
        null
    }
  }

  def read(tag: NBTTagCompound): Structure = {
    val structure: Structure = new Structure
    if (tag.hasKey("Blocks", TAG_LIST) && tag.hasKey("Data", TAG_BYTE_ARRAY)) {
      var types = HashMap.empty[Short, Block]
      val typeStrings = tag.getTagList("Blocks", TAG_STRING)
      for (i <- 0 until typeStrings.tagCount()) {
        val block: Block = Block.getBlockFromName(typeStrings.getStringTagAt(i))
        if (block != null) types += i.toShort -> block
      }
      var tiles = HashMap.empty[BlockPos, NBTTagCompound]
      if (tag.hasKey("TileEntities", TAG_LIST)) {
        val tileList = tag.getTagList("TileEntities", TAG_COMPOUND)
        for (i <- 0 until tileList.tagCount()) {
          val tile = tileList.getCompoundTagAt(i)
          tiles += BlockPos(tile) -> tile
        }
      }
      val width: Int = tag.getInteger("Width")
      val length: Int = tag.getInteger("Length")
      val height: Int = tag.getInteger("Height")
      val data: Array[Byte] = tag.getByteArray("Data")
      val buf: ShortBuffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN).asShortBuffer
      for (y <- 0 until height) {
        val layer: Layer = new Layer(width, length)
        for (x <- 0 until width; z <- 0 until length) {
          val dat: Short = buf.get
          val id: Short = (dat >> 4).asInstanceOf[Short]
          val meta: Byte = (dat & 0xF).asInstanceOf[Byte]
          val block: Block = types.getOrElse(id, null)
          layer.set(x, z, block, meta, tiles.getOrElse(BlockPos(x, y, z), null))
        }
        structure.addLayer(layer)
      }
      if (tag.hasKey("Entities", TAG_LIST)) {
        val entities: NBTTagList = tag.getTagList("Entities", TAG_COMPOUND)
        for (i <- 0 until entities.tagCount()) {
          val entity: NBTTagCompound = entities.getCompoundTagAt(i)
          structure.addEntity(entity)
        }
      }
    }
    structure
  }

  def write(structure: Structure, tag: NBTTagCompound) {
    val tiles = new NBTTagList
    val typeStrings = new NBTTagList
    val types = mutable.HashMap.empty[Block, Short]
    var lastType: Short = 0
    for (layer <- structure.getLayers; x <- 0 until layer.width; z <- 0 until layer.length) {
      val info = layer.get(x, z)
      val block = if (info != null) info.getBlock else Blocks.air
      if (!types.contains(block)) {
        types.put(block, lastType)
        typeStrings.appendTag(new NBTTagString(Block.blockRegistry.getNameForObject(block)))
        lastType = (lastType + 1).toShort
      }
    }

    val width = structure.getWidth
    val length = structure.getLength
    val height = structure.getHeight
    tag.setInteger("Width", width)
    tag.setInteger("Length", length)
    tag.setInteger("Height", height)
    val bytes = ByteBuffer.wrap(new Array[Byte](2 * width * length * height)).order(ByteOrder.BIG_ENDIAN)
    val buf = bytes.asShortBuffer
    var y: Int = 0
    for (layer <- structure.getLayers) {
      for (x <- 0 until layer.width) {
        for (z <- 0 until layer.length)
          writeBlock(y, tiles, typeStrings, buf, types, layer.get(x, z))
        for (z <- 0 until length - layer.length)
          writeBlock(y, tiles, typeStrings, buf, types, null)
      }
      for(x <- 0 until width - layer.width; z <- 0 until length)
        writeBlock(y, tiles, typeStrings, buf, types, null)
      y += 1
    }
    tag.setByteArray("Data", bytes.array)
    tag.setTag("Blocks", typeStrings)
    tag.setTag("TileEntities", tiles)
    val entities: NBTTagList = new NBTTagList

    for (entity <- structure.getEntities) {
      entities.appendTag(entity)
    }
    tag.setTag("Entities", entities)
  }

  private def writeBlock(y: Int, tiles: NBTTagList, typeStrings: NBTTagList, buf: ShortBuffer, types: mutable.Map[Block, Short], info: BlockInfo) {
    if (info != null) {
      val s: Short = ((types.getOrElse(info.getBlock, 0.toShort) << 4) | (info.getMetadata & 0xF)).toShort
      buf.put(s)
      val tile: NBTTagCompound = info.getTileEntity
      if (tile != null) {
        tile.setInteger("x", info.x)
        tile.setInteger("y", y)
        tile.setInteger("z", info.z)
        tiles.appendTag(tile)
      }
    }
    else {
      if (!types.contains(Blocks.air)) {
        typeStrings.appendTag(new NBTTagString(Block.blockRegistry.getNameForObject(Blocks.air)))
        types += Blocks.air -> types.size.asInstanceOf[Short]
      }
      writeBlock(0, tiles, typeStrings, buf, types, new SimpleBlockInfo(Blocks.air, 0, 0, 0))
    }
  }
}

class ModMaticFile(tag: NBTTagCompound) {
  val structure = read()

  def read() = ModMaticFile.read(tag)
}
