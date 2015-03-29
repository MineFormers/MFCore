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
package de.mineformers.core.client.ui.view.inventory

import de.mineformers.core.util.math.shape2d.{Point, Size}
import de.mineformers.core.client.ui.view.View
import de.mineformers.core.client.ui.view.container.Panel
import de.mineformers.core.client.ui.view.container.Panel.Padding
import de.mineformers.core.client.ui.util.{MouseButton, MouseEvent}
import de.mineformers.core.util.renderer.GuiUtils
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumChatFormatting
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.GLU

/**
 * ItemStack
 *
 * @author PaleoCrafter
 */
class ItemSlot(private var _stack: ItemStack, drawSlot: Boolean = false, val attached: Boolean = false) extends View {
  if (attached)
    zIndex = 500

  import scala.collection.JavaConversions._

  if (stack != null) {
    val original = stack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips)
    tooltip = stack.getRarity.rarityColor + original.head.toString + "\n" + original.tail.map(EnumChatFormatting.GRAY + _.toString).mkString("\n")
  }

  reactions += {
    case e: MouseEvent.Click =>
      if (e.button == MouseButton.Left) {
        if (stack != null && (context.attached == null || !context.attached.isInstanceOf[ItemSlot])) {
          context.attached = new ItemSlot(stack, attached = true)
          context.attached.init(context, context)
          this.stack = null
        }
      }
  }

  globalReactions += {
    case e: MouseEvent.Click if attached =>
      val clicked = context.findAffectedView(e.pos)
      if (e.button == MouseButton.Left) {
        clicked match {
          case i: ItemSlot if i.stack == null => i.stack = stack
            context.attached = null
          case null => context.attached = null
          case _ =>
        }
      }
    case e: MouseEvent.Release if attached =>
      val clicked = context.findAffectedView(e.pos)
      if (e.button == MouseButton.Left) {
        clicked match {
          case i: ItemSlot if i.stack == null => i.stack = stack
            context.attached = null
          case null => context.attached = null
          case _ =>
        }
      }
  }

  this.size = if (drawSlot) Size(18, 18) else Size(16, 16)
  if (drawSlot)
    background = "scrollBar"

  override def update(mousePos: Point): Unit = {
    if (attached) {
      screen = mousePos - Point(8, 8)
    }
  }

  def stack = _stack

  def stack_=(stack: ItemStack): Unit = {
    this._stack = stack
    if (stack != null) {
      val original = stack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips)
      tooltip = stack.getRarity.rarityColor + original.head.toString + "\n" + original.tail.map(EnumChatFormatting.GRAY + _.toString).mkString("\n")
    }
  }

  override var skin: Skin = new ItemStackSkin

  override def toString: String = s"LabelItemStack(stack=$stack, drawSlot=$drawSlot)"

  class ItemStackSkin extends Skin {
    override protected def drawForeground(mousePos: Point): Unit = {
      if (stack != null)
        if (drawSlot)
          utils.drawItemStack(stack, screen.x + 1, screen.y + 1, zIndex - 1, null)
        else
          utils.drawItemStack(stack, screen.x, screen.y, zIndex, null)
    }
  }

}

object ItemSlot {
  def createContainer(stack: ItemStack) = new ItemSlotContainer(stack)

  class ItemSlotContainer(stack: ItemStack) extends Panel {
    size = Size(18, 18)
    padding = Padding(1)
    clip = false
    skin = new SlotSkin

    this add new ItemSlot(stack)

    class SlotSkin extends PanelSkin {
      override def drawForeground(mousePos: Point): Unit = {
        super.drawForeground(mousePos)
        if (hovered(mousePos))
          GuiUtils.drawRectangle(0xFFFFFF, 0.5F, screen.x + 1, screen.y + 1, 200, 16, 16)
      }
    }

  }

}