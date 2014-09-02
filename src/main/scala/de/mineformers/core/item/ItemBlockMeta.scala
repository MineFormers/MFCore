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
package de.mineformers.core.item

import net.minecraft.item.{ItemStack, ItemBlock}
import net.minecraft.block.Block
import de.mineformers.core.block.MetaBlock

/**
 * ItemBlockMeta
 *
 * @param block the block associated with this ItemBlock
 * @author PaleoCrafter
 */
class ItemBlockMeta(block: Block) extends ItemBlock(block) {
  this.setHasSubtypes(true)

  /**
   * @param dmg the damage value of the stack representing the block
   * @return the given damage value
   */
  override def getMetadata(dmg: Int): Int = dmg

  /**
   * @param stack the stack representing a SubBlock
   * @return the unlocalized name for the given stack
   */
  override def getUnlocalizedName(stack: ItemStack): String = block.asInstanceOf[MetaBlock].getUnlocalizedName(stack)
}
