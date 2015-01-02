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

import de.mineformers.core.util.math.shape2d.{Point, Size}
import de.mineformers.core.client.ui.component.View
import de.mineformers.core.client.ui.component.container.{DebugWindow, Frame}
import de.mineformers.core.client.ui.component.decoration.Tooltip
import de.mineformers.core.client.ui.util.{KeyEvent, MouseEvent}
import de.mineformers.core.util.renderer.GuiUtils
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Mouse

import scala.collection.mutable

/**
 * UIScreen
 *
 * @author PaleoCrafter
 */
class UIScreen(frames0: Seq[Frame]) extends GuiScreen with Context {
  override def initGui(): Unit = {
    frames foreach {
      f => f.proxy = this
        f.init(this, this)
    }
  }

  override def size: Size = Size(width, height)

  override def mouseClicked(x: Int, y: Int, button: Int): Unit = {
    val currentTime = System.currentTimeMillis()
    publish(MouseEvent.Click(Point(x, y), button))

    if (button == lastButton && currentTime - lastClickTime <= 300) {
      publish(MouseEvent.DoubleClick(Point(x, y), button))
    }

    lastButton = button
    lastClickTime = currentTime
  }

  override def mouseClickMove(x: Int, y: Int, lastButton: Int, timeSinceClick: Long): Unit = {
    publish(MouseEvent.Drag(Point(x, y), lastDragPosition, lastButton, timeSinceClick))
    lastDragPosition = Point(x, y)
  }

  override def mouseReleased(x: Int, y: Int, button: Int): Unit = {
    publish(MouseEvent.Release(Point(x, y), button))
  }

  override def keyTyped(char: Char, code: Int): Unit = {
    if (focused == null || !focused.focused)
      super.keyTyped(char, code)
    publish(KeyEvent.Type(char, code))
  }

  override def updateScreen(): Unit = {
    val scaledWidth = GuiUtils.scaledResolution.getScaledWidth
    val scaledHeight = GuiUtils.scaledResolution.getScaledHeight
    val x: Int = Mouse.getX * scaledWidth / this.mc.displayWidth
    val y: Int = scaledHeight - Mouse.getY * scaledHeight / this.mc.displayHeight - 1
    val pos = Point(x, y)
    if (pos != lastMousePosition) {
      publish(MouseEvent.Move(pos, lastMousePosition))
      lastMousePosition = pos
    }
    for(i <- 0 until Mouse.getButtonCount) {
      if(Mouse.isButtonDown(i)) {
        val current = continuousClick.getOrElseUpdate(i, (0L, System.currentTimeMillis()))
        val duration = System.currentTimeMillis() - current._2 + current._1
        publish(MouseEvent.ContinuousClick(pos, i, duration))
        continuousClick.put(i, (duration, System.currentTimeMillis()))
      } else {
        continuousClick.put(i, (0L, System.currentTimeMillis()))
      }
    }
    val dWheel = Mouse.getDWheel / 120
    if (dWheel != 0) {
      publish(MouseEvent.Scroll(pos, dWheel))
    }
    frames.foreach {
      frame =>
        frame.update(pos)
        frame.updateState(pos)
    }
    if(attached != null)
      attached.update(pos)
  }

  override def close(): Unit = {
    this.mc.displayGuiScreen(null)
    this.mc.setIngameFocus()
  }

  override def doesGuiPauseGame(): Boolean = false

  override def drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    this.drawWorldBackground(0)
    val p = Point(mouseX, mouseY)
    tooltip.text = ""
    frames.foreach {
      frame =>
        frame.skin.draw(p)
        tooltip.screen = p + Point(5, 5)
        val text = frame.deepTooltip(p)
        if (text != null) {
          tooltip.text = text
        }
    }
    if(attached != null)
      attached.skin.draw(p)
    if (tooltip.text != "")
      tooltip.skin.draw(p)
  }

  override def findComponent(mousePos: Point, predicate: View => Boolean): View = {
    for (frame <- frames.sortBy(_.zIndex).reverse) {
      val comp = frame.findComponent(mousePos, predicate)
      if (comp != null)
        return comp
    }
    null
  }

  private val debug = true
  val frames = frames0 ++ (if (debug) Seq(new DebugWindow) else Seq())
  private val tooltip = new Tooltip("")
  private val continuousClick = mutable.HashMap.empty[Int, (Long, Long)]
  private var lastButton = -1
  private var lastClickTime = 0L
  private var lastMousePosition = Point(0, 0)
  private var lastDragPosition = Point(0, 0)
}
