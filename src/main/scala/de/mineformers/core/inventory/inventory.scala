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

package de.mineformers.core.inventory

import cpw.mods.fml.relauncher.ReflectionHelper
import de.mineformers.core.reaction.{Event, Publisher}
import de.mineformers.core.util.Log
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.{IInventory, ISidedInventory}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraftforge.common.util.{Constants, ForgeDirection}

/**
 * Inventory
 *
 * @author PaleoCrafter
 */
object Inventory {
  final val InventoryKey = "_mf$inventory"
  val field = ReflectionHelper.findField(classOf[NBTTagList], "tagList", "field_74747_a")

  def createSimple(size: Int) = new SimpleInventory(size)

  def writeStackList(stacks: Traversable[ItemStack]): NBTTagList = {
    val indexed = stacks.toIndexedSeq
    val nbt = new NBTTagList()
    val len = stacks.size
    for (i <- 0 until len) {
      val item = indexed(i)
      if (item != null) {
        val itemCompound = item.writeToNBT(new NBTTagCompound())
        itemCompound.setShort("slot", i.toShort)
        nbt.appendTag(itemCompound)
      }
    }
    nbt
  }

  def readStackList(stacks: Array[ItemStack], list: NBTTagList): Unit = {
    field.setAccessible(true)
    val tags = field.get(list).asInstanceOf[java.util.List[NBTTagCompound]]
    import scala.collection.JavaConversions._
    for (nbt <- tags) {
      val item = ItemStack.loadItemStackFromNBT(nbt)
      val idx = nbt.getShort("slot").toInt
      if (idx < stacks.length) {
        stacks(idx) = item
      } else {
        Log.fatal("Inventory slot %d is out of bounds (length %d) while reading inventory".format(idx, stacks.length))
      }
    }
  }
}

trait Inventory extends IInventory with Publisher with Traversable[ItemStack] {
  val content = Array.ofDim[ItemStack](getSizeInventory)

  def apply(slot: Int) = Option(getStackInSlot(slot))

  override def getStackInSlot(slot: Int): ItemStack =
    if (slot < 0 || slot >= content.length)
      null
    else
      content(slot)

  override def setInventorySlotContents(slot: Int, stack: ItemStack): Unit =
    if (slot >= 0 && slot < getSizeInventory) {
      content(slot) = stack
      markDirty()
    }

  override def decrStackSize(slot: Int, count: Int): ItemStack =
    this(slot) match {
      case Some(stack) =>
        if (stack.stackSize <= count) {
          val returnStack = stack
          setInventorySlotContents(slot, null)
          returnStack
        } else {
          val returnStack = stack.splitStack(count)

          if (stack.stackSize == 0) {
            setInventorySlotContents(slot, null)
          } else {
            markDirty()
          }
          returnStack
        }
      case _ => null
    }

  override def getStackInSlotOnClosing(slot: Int): ItemStack = {
    val stack = getStackInSlot(slot)
    setInventorySlotContents(slot, null)
    stack
  }

  override def markDirty(): Unit = publish(InventoryChanged(this))

  override def getInventoryStackLimit: Int = 64

  override def isItemValidForSlot(slot: Int, stack: ItemStack): Boolean = true

  override def isUseableByPlayer(player: EntityPlayer): Boolean = true

  override def openInventory(): Unit = ()

  override def closeInventory(): Unit = ()

  override def hasCustomInventoryName: Boolean = false

  override def getInventoryName: String = ""

  def saveToNBT(tag: NBTTagCompound): Unit = {
    tag.setTag(Inventory.InventoryKey, Inventory.writeStackList(content))
  }

  def loadFromNBT(tag: NBTTagCompound): Unit = {
    Inventory.readStackList(content, tag.getTagList(Inventory.InventoryKey, Constants.NBT.TAG_COMPOUND))
  }

  override def foreach[U](f: (ItemStack) => U): Unit = content foreach f
}

trait InventoryHolder extends Inventory {
  lazy val inventory = createInventory

  def createInventory: IInventory

  override def getSizeInventory: Int = inventory.getSizeInventory
}

object InventoryHolder {

  trait Sided extends InventoryHolder with ISidedInventory {
    val accessibleSlots: Map[ForgeDirection, Array[Int]]

    override def getAccessibleSlotsFromSide(side: Int): Array[Int] = accessibleSlots.getOrElse(ForgeDirection.getOrientation(side), Array.empty[Int])

    override def canInsertItem(slot: Int, stack: ItemStack, side: Int): Boolean = getAccessibleSlotsFromSide(side).contains(slot) && isItemValidForSlot(slot, stack)

    override def canExtractItem(slot: Int, stack: ItemStack, side: Int): Boolean = getAccessibleSlotsFromSide(side).contains(slot) && isItemValidForSlot(slot, stack)
  }

}

class SimpleInventory private[inventory](size: Int) extends Inventory {
  override def getSizeInventory: Int = size
}

case class InventoryChanged(inv: IInventory) extends Event
