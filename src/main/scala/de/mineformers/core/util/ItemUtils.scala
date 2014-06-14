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

package de.mineformers.core.util

import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary

/**
 * ItemUtils
 *
 * @author PaleoCrafter
 */
object ItemUtils {
  def stacksEqual(compare: ItemStack, against: ItemStack, matchNBT: Boolean): Boolean = {
    (compare.getItem eq against.getItem) && damageEqual(compare, against) && (!matchNBT || nbtEqual(compare, against))
  }

  def damageEqual(compare: ItemStack, against: ItemStack): Boolean = {
    against.getItemDamage == OreDictionary.WILDCARD_VALUE || compare.getItemDamage == OreDictionary.WILDCARD_VALUE || against.getItemDamage == compare.getItemDamage
  }

  def nbtEqual(compare: ItemStack, against: ItemStack): Boolean = {
    (against.stackTagCompound == null && compare.stackTagCompound == null) || against.stackTagCompound.equals(compare.stackTagCompound)
  }

  final val MergeFullStack = -1

  class StackMerger(var merge: ItemStack, var into: ItemStack) {
    def performMerge(): Boolean = performMerge(MergeFullStack)

    def performMerge(amount: Int): Boolean = {
      var nbMerged = 0
      var am = amount
      if (!canMerge(merge, into)) return false
      if (merge == null) return false
      if (amount == MergeFullStack) am = merge.stackSize
      am = Math.min(am, merge.stackSize)
      if (into == null) {
        nbMerged = am
        into = merge.copy
        into.stackSize = am
        merge.stackSize -= am
        if (merge.stackSize <= 0) merge = null
        return true
      }
      nbMerged = into.getMaxStackSize - into.stackSize
      if (nbMerged == 0) return false
      nbMerged = Math.min(nbMerged, am)
      merge.stackSize -= nbMerged
      if (merge.stackSize == 0) merge = null
      into.stackSize += nbMerged
      true
    }
  }

  def canMerge(merge: ItemStack, into: ItemStack): Boolean = {
    if (merge == null || into == null) return true
    areItemStacksStackable(merge, into)
  }

  def areItemStacksStackable(stack1: ItemStack, stack2: ItemStack): Boolean = {
    if (stack1 == null || stack2 == null) return false
    if (!stack1.isStackable) return false
    (stack1.getItem eq stack2.getItem) && (!stack2.getHasSubtypes || stack2.getItemDamage == stack1.getItemDamage) && ItemStack.areItemStackTagsEqual(stack2, stack1)
  }
}
