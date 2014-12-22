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
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection

import scala.collection.mutable
import scala.util.control.Breaks

/**
 * configurable
 *
 * @author PaleoCrafter
 */
class StructureBuilder {
  private val layers = mutable.ListBuffer.empty[Layer]
  private var lastLayer: LayerBuilder = _

  def build(): Structure = {
    if (lastLayer != null)
      lastLayer.buildLayer()
    val structure = new Structure(layers.clone())
    layers.clear()
    structure
  }

  def buildConfig(): StructureConfiguration = {
    if (lastLayer != null)
      lastLayer.buildLayer()
    val config = new StructureConfiguration((layers map {
      case c: ConfigurableLayer => c
      case l: Layer => new ConfigurableLayer(l)
    }).toSeq)
    layers.clear()
    config
  }

  def layer(pattern: String*): LayerBuilder = {
    lastLayer = new LayerBuilder(pattern)
    lastLayer
  }

  def layer(required: Boolean, min: Int, max: Int, pattern: String*): LayerBuilder = {
    lastLayer = new LayerBuilder(pattern, config = true, required, min, max)
    lastLayer
  }

  class LayerBuilder(pattern: Seq[String], config: Boolean = false, required: Boolean = false, min: Int = 0, max: Int = -1) {
    var values = mutable.Map.empty[Char, BlockInfo]

    def build(): Structure = StructureBuilder.this.build()

    def buildConfig(): StructureConfiguration = StructureBuilder.this.buildConfig()

    def where(c: Char): InfoBuilder = new InfoBuilder(c)

    def layer(pattern: String*): LayerBuilder = {
      buildLayer()
      StructureBuilder.this.layer(pattern: _*)
    }

    private[StructureBuilder] def buildLayer(): Unit = {
      val width = pattern.foldLeft("")((a: String, b: String) => if (b.length > a.length) b else a).length
      val length = pattern.length
      val layer = if (!config) new Layer(width, length) else new ConfigurableLayer(width, length, required, min, max)
      var z = 0
      for (s <- this.pattern) {
        var x = 0
        for (c <- s) {
          if (c != ' ' && values.contains(c)) {
            val info = values(c).copy
            info.x = x
            info.z = z
            layer.set(info)
          }
          x += 1
        }
        z += 1
      }

      layers append layer
    }

    def layer(required: Boolean, min: Int, max: Int, pattern: String*): LayerBuilder = {
      buildLayer()
      StructureBuilder.this.layer(required, min, max, pattern: _*)
    }

    class InfoBuilder(c: Char) {
      def is(block: Block, meta: Int, tile: NBTTagCompound = null): LayerBuilder = is(new SimpleBlockInfo(block, 0, 0, (meta & 0xF).toByte, tile))

      def is(blocks: (Block, Int, NBTTagCompound)*): LayerBuilder = is(new MultiBlockInfo(0, 0, blocks map (e => BlockEntry(e._1, (e._2 & 0xF).toByte, e._3))))

      def is(info: BlockInfo): LayerBuilder = {
        values += c -> info
        LayerBuilder.this
      }
    }

  }

}

object StructureBuilder {
  def start(): StructureBuilder = new StructureBuilder
}

class StructureConfiguration(possibleLayers: Seq[ConfigurableLayer]) {
  val amount = mutable.Map.empty[Int, Int]

  def build(): Structure = {
    val structure = new Structure
    import scala.util.control.Breaks._
    for (y <- 0 until possibleLayers.length) {
      breakable {
        val confLayer = possibleLayers(y)
        val amount = this.amount.getOrElse(y, 0)
        if (confLayer.required && amount == 0)
          throw new IllegalArgumentException("Required layer wasn't respected")
        if (amount < confLayer.min)
          break()
        if (amount > confLayer.max && confLayer.max != -1)
          break()
        for (i <- 0 until amount)
          structure.addLayer(confLayer.copy)
      }
    }
    structure
  }
}

class ConfigurableLayer(width: Int, length: Int, val required: Boolean = false, val min: Int = 0, val max: Int = -1) extends Layer(width, length) {
  def this(l: Layer) = {
    this(l.width, l.length)
    for (x <- l.blocks; b <- x)
      set(b)
  }
}

class MultiBlockInfo(_x: Int, _z: Int, var blocks: Seq[BlockEntry]) extends BlockInfo(_x, _z) {
  var delay = 200
  private var i = 0
  private var last: Long = 0

  override def update(world: StructureWorld, y: Int): Unit = {
    val time = System.currentTimeMillis()
    if (time - last >= 1000) {
      if (i == blocks.length - 1)
        i = 0
      else
        i = (i + 1) max 0 min (blocks.length - 1)
      last = time
      world.markBlockForUpdate(x, y, z)
    }
  }

  override def rotate(world: World, y: Int, axis: ForgeDirection): Unit = {
    getEntry.block.rotateBlock(world, x, y, z, axis)
  }

  override def getEntry: BlockEntry = blocks(i)

  override def copy: BlockInfo = {
    new MultiBlockInfo(x, z, Seq(blocks: _*))
  }
}
