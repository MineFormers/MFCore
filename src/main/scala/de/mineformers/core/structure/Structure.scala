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

import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.block.Block
import scala.collection.mutable

/**
 * Structure
 *
 * @author PaleoCrafter
 */
class Structure(private val layers: mutable.Buffer[Layer] = mutable.Buffer.empty[Layer]) {
  private val entities = mutable.ListBuffer.empty[NBTTagCompound]

  def addLayer(layer: Layer): Unit = {
    if (layer == null) throw new IllegalArgumentException("Layer may not be null")
    layers append layer
  }

  def replaceLayer(y: Int, layer: Layer): Unit = {
    if (getLayer(y) == null) throw new IllegalArgumentException("Layer out of height")
    layers(y) = layer
  }

  def setLayer(y: Int, layer: Layer): Unit = {
    for (y1 <- getHeight until (y + 1))
      addLayer(new Layer(getWidth, getLength))
    replaceLayer(y, layer)
  }

  def getBlock(x: Int, y: Int, z: Int): BlockInfo = {
    val layer: Layer = getLayer(y)
    if (layer == null) return null
    layer.get(x, z)
  }

  def setBlock(x: Int, y: Int, z: Int, block: Block, metadata: Int, tile: NBTTagCompound): Unit = {
    getLayer(y).set(x, z, block, metadata, tile)
  }

  def getLayer(y: Int): Layer = {
    if (y >= getHeight || y < 0) return null
    layers(y)
  }

  def addEntity(tag: NBTTagCompound): Unit = {
    entities append tag
  }

  def clear(): Unit = {
    layers.clear()
  }

  def getWidth: Int = {
    var widest: Int = 0
    for (layer <- layers) if (widest < layer.width) widest = layer.width
    widest
  }

  def getLength: Int = {
    var longest: Int = 0
    for (layer <- layers) if (longest < layer.length) longest = layer.length
    longest
  }

  def getHeight: Int = layers.size

  def update(world: StructureWorld): Unit = {
    for (y <- 0 until getHeight; x <- 0 until getWidth; z <- 0 until getLength) {
      val info = getBlock(x, y, z)
      if (info != null)
        info.update(world, y)
    }
  }

  def blockTypes: Seq[BlockEntry] = for {
    layer <- layers
    row <- layer.blocks
    block <- row
    entries = block match {
      case b: MultiBlockInfo => b.blocks
      case _ => Seq(block.getEntry)
    }
    entry <- entries
    if entry.block != null && entry.block != Blocks.air
  } yield entry

  final def getLayers: List[Layer] = layers.toList

  final def getEntities: List[NBTTagCompound] = entities.toList

  def copy: Structure = {
    val struct: Structure = new Structure
    for (layer <- layers) struct.addLayer(layer.copy)
    struct
  }

  override def toString: String = "Structure=" + layers.toString
}