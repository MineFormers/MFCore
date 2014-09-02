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

package de.mineformers.core.client.ui.component.decoration

import de.mineformers.core.client.ui.component.Component
import de.mineformers.core.client.shape2d.{Size, Point}
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.{GL12, GL11}
import net.minecraft.client.renderer.{OpenGlHelper, RenderHelper}

/**
 * ItemStack
 *
 * @author PaleoCrafter
 */
class LabelItemStack(val stacks: Array[ItemStack], drawSlot: Boolean = true, val addAmount: Int = -1) extends Component {
  def this(stack: ItemStack) = this(Array(stack))

  var i = 0
  this.tooltip = stacks(i).getDisplayName
  this.size = if (drawSlot) Size(18, 18) else Size(16, 16)
  if (drawSlot)
    background = "scrollbar"

  override def update(mousePos: Point): Unit = {
    val time = System.currentTimeMillis()
    if (time - last >= 2000) {
      if (i == stacks.length - 1)
        i = 0
      else
        i = (i + 1) max 0 min (stacks.length - 1)
      tooltip = current.getDisplayName
      last = time
    }
  }

  def current = stacks(i)

  override var skin: Skin = new ItemStackSkin
  private var last = System.currentTimeMillis()

  override def toString: String = s"LabelItemStack(stacks=${stacks.mkString("[", ",", "]")}}, drawSlot=$drawSlot, addAmount=$addAmount)"

  class ItemStackSkin extends Skin {
    override protected def drawForeground(mousePos: Point): Unit = {
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F)
      RenderHelper.enableGUIStandardItemLighting()
      GL11.glEnable(GL12.GL_RESCALE_NORMAL)
      if (drawSlot)
        utils.drawItemStack(current, screen.x + 1, screen.y + 1, if (addAmount > 0) (current.stackSize * addAmount).toString else if (addAmount == 0) "" else null)
      else
        utils.drawItemStack(current, screen.x, screen.y, if (addAmount > 0) (current.stackSize * addAmount).toString else if (addAmount == 0) "" else null)
      RenderHelper.disableStandardItemLighting()
      GL11.glDisable(GL11.GL_LIGHTING)
    }
  }

}
