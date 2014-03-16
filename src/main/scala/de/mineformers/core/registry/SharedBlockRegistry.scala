/*******************************************************************************
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
 ******************************************************************************/
package de.mineformers.core.registry

import cpw.mods.fml.common.{LoaderState, Loader}
import net.minecraft.item.ItemBlock
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.block.Block
import de.mineformers.core.block.MetaBlock
import de.mineformers.core.item.ItemBlockMeta
import scala.language.existentials

/**
 * BlockEntry representing a block in the registry's map
 * @param block the block to register
 * @param itemBlock the ItemBlock class for the supplied block
 * @param modId the mod id to register the block with
 * @param cstrArgs the arguments for the ItemBlock's constructor
 */
case class BlockEntry(
                       block: Block,
                       itemBlock: Class[_ <: ItemBlock] = classOf[ItemBlock],
                       modId: String = null, cstrArgs: Array[Object] = Array[Object]())

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
   * @param modId the mod id to register the block with
   * @param cstrArgs the constructor arguments for the ItemBlock
   */
  def add(
           key: String,
           block: Block,
           itemBlock: Class[_ <: ItemBlock] = classOf[ItemBlock],
           modId: String = null, cstrArgs: Array[Object] = Array[Object]()): Unit = this.add(key, BlockEntry(block, itemBlock, modId, cstrArgs))

  /**
   * Registers the given entry to the GameRegistry
   * @param key the mapping's key
   * @param value the mapping's value
   */
  override protected def put(key: String, value: BlockEntry): Unit = {
    if (Loader.instance().isInState(LoaderState.PREINITIALIZATION)) {
      if (value.block.isInstanceOf[MetaBlock] && value.itemBlock == classOf[ItemBlock]) {
        GameRegistry.registerBlock(value.block, classOf[ItemBlockMeta], key, value.modId)
        return
      }
      if (value.cstrArgs.length > 0)
        GameRegistry.registerBlock(value.block, value.itemBlock, key, value.modId, value.cstrArgs: _*)
      else
        GameRegistry.registerBlock(value.block, value.itemBlock, key, value.modId)
    } else {
      System.err.println("A mod was trying to register a block outside the pre init phase")
    }
  }
}
