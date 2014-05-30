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

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon

/**
 * DamageItem
 *
 * @param baseName the base unlocalized name for this item
 * @param tab the creative tab for this item
 * @param subItems an array of [[SubItem]]s
 * @author PaleoCrafter
 */
class DamageItem(baseName: String, tab: CreativeTabs, subItems: Array[SubItem]) extends BaseItem(baseName, tab) {
  this.setHasSubtypes(true)
  this.setMaxDamage(0)

  override def requiresMultipleRenderPasses(): Boolean = true

  /**
   * @param stack the stack representing a [[SubItem]]
   * @param pass the pass to render in
   * @return the icon used for this pass and [[SubItem]]
   */
  override def getIcon(stack: ItemStack, pass: Int): IIcon = {
    val dmg = stack.getItemDamage.max(0).min(subItems.length - 1)
    subItems(dmg).getIcon(stack, pass)
  }

  /**
   * @param metadata the damage value of the stack
   * @return the render passes needed
   */
  override def getRenderPasses(metadata: Int): Int = 1

  /**
   * @param stack the stack representing the [[SubItem]]
   * @return the unlocalized name for the given stack
   */
  override def getUnlocalizedName(stack: ItemStack): String = {
    val dmg = stack.getItemDamage.max(0).min(subItems.length - 1)
    "item." + baseName + "." + subItems(dmg).getName(stack)
  }
}
