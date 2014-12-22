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
package de.mineformers.core.registry

import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.common.{Loader, LoaderState}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import de.mineformers.core.block.{MetaBlock, TileProvider}
import de.mineformers.core.client.util.BlockRendering
import de.mineformers.core.item.ItemBlockMeta
import de.mineformers.core.util.Log
import net.minecraft.block.Block
import net.minecraft.item.ItemBlock
import net.minecraft.tileentity.TileEntity

import scala.language.existentials

/**
 * BlockEntry representing a block in the registry's map
 * @param block the block to register
 * @param itemBlock the ItemBlock class for the supplied block
 * @param cstrArgs the arguments for the ItemBlock's constructor
 */
case class BlockEntry(block: Block, itemBlock: Class[_ <: ItemBlock] = classOf[ItemBlock], cstrArgs: Array[Object] = Array[Object]())

/**
 * SharedBlockRegistry
 * Register your blocks here :)
 *
 * @author PaleoCrafter
 */
object SharedBlockRegistry extends SharedRegistry[String, BlockEntry] {
  /**
   * Helper method
   * @param key the key to register the block with
   * @param block the block to register
   * @param itemBlock the ItemBlock class for this block
   * @param cstrArgs the constructor arguments for the ItemBlock
   */
  def add(key: String, block: Block, itemBlock: Class[_ <: ItemBlock] = classOf[ItemBlock], cstrArgs: Array[Object] = Array[Object]()): Unit = this.add(key, BlockEntry(block, itemBlock, cstrArgs))

  @SideOnly(Side.CLIENT)
  def registerRenderers(): Unit = {
    this foreach {
      e =>
        val block = e._2.block
        block match {
          case rendering: BlockRendering with TileProvider[_] => rendering.proxy.registerRenderers(rendering, rendering.tileClass.asInstanceOf[Class[_ <: TileEntity]])
          case rendering: BlockRendering => rendering.proxy.registerRenderers(rendering, null)
          case _ =>
        }
    }
  }

  /**
   * Registers the given entry to the GameRegistry
   * @param key the mapping's key
   * @param value the mapping's value
   */
  override protected def put(key: String, value: BlockEntry): Unit = {
    if (Loader.instance().isInState(LoaderState.PREINITIALIZATION)) {
      if (value.block.isInstanceOf[MetaBlock] && value.itemBlock == classOf[ItemBlock]) {
        GameRegistry.registerBlock(value.block, classOf[ItemBlockMeta], key)
        return
      }
      if (value.cstrArgs.length > 0)
        GameRegistry.registerBlock(value.block, value.itemBlock, key, value.cstrArgs: _*)
      else
        GameRegistry.registerBlock(value.block, value.itemBlock, key)
      value.block match {
        case tileProvider: TileProvider[_] =>
          val cls = tileProvider.tileClass
          GameRegistry.registerTileEntity(cls, Loader.instance().activeModContainer().getModId + ":" + cls.getSimpleName)
        case _ =>
      }
    } else {
      Log.error("A mod was trying to register a block outside the pre init phase")
    }
  }
}
