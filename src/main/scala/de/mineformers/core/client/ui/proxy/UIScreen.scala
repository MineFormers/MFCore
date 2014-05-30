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

package de.mineformers.core.client.ui.proxy

import net.minecraft.client.gui.GuiScreen
import de.mineformers.core.client.ui.reaction.{MouseEvent, KeyEvent}
import de.mineformers.core.client.ui.component.container.Panel
import de.mineformers.core.client.shape2d.Point
import org.lwjgl.input.Mouse
import de.mineformers.core.util.renderer.GuiUtils

/**
 * UIScreen
 *
 * @author PaleoCrafter
 */
class UIScreen(container: Panel) extends GuiScreen with Context {
  override def initGui(): Unit = container.init(this, this)

  override def mouseClicked(x: Int, y: Int, button: Int): Unit = {
    publish(MouseEvent.Click(Point(x, y), button))
  }

  override def mouseClickMove(x: Int, y: Int, lastButton: Int, timeSinceClick: Long): Unit = {
    publish(MouseEvent.Drag(Point(x, y), lastDragPosition, lastButton, timeSinceClick))
    lastDragPosition = Point(x, y)
  }

  override def keyTyped(char: Char, code: Int): Unit = {
    super.keyTyped(char, code)
    publish(KeyEvent.Type(char, code))
  }


  override def updateScreen(): Unit = {
    val scaledWidth = GuiUtils.scaledResolution.getScaledWidth
    val scaledHeight = GuiUtils.scaledResolution.getScaledHeight
    val x: Int = Mouse.getX * scaledWidth / this.mc.displayWidth
    val y: Int = scaledHeight - Mouse.getY * scaledHeight / this.mc.displayHeight - 1
    val pos = Point(x, y)
    if(pos != lastMousePosition) {
      publish(MouseEvent.Move(pos, lastMousePosition))
      lastMousePosition = pos
    }
    container.update(pos)
  }

  override def drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    container.skin.draw(Point(mouseX, mouseY), container)
  }

  private var lastMousePosition = Point(0, 0)
  private var lastDragPosition = Point(0, 0)
}
