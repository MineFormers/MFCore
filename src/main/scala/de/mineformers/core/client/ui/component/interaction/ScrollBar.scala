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

package de.mineformers.core.client.ui.component.interaction

import de.mineformers.core.client.ui.component.Component
import de.mineformers.core.client.shape2d.{Rectangle, Size, Point}
import de.mineformers.core.client.ui.util.{MouseEvent, MouseButton}
import de.mineformers.core.client.ui.skin.TextureManager
import de.mineformers.core.client.ui.proxy.Context
import de.mineformers.core.client.ui.component.interaction.ScrollBar.Orientation._
import de.mineformers.core.reaction.Publisher
import net.minecraft.client.gui.GuiScreen
import de.mineformers.core.client.ui.component.container.ScrollPanel

/**
 * ScrollBar
 *
 * @author PaleoCrafter
 */
class ScrollBar(length: Int, orientation: Int) extends Component {
  private val scrollerSize = Size(if (orientation == Horizontal) 15 else 12, if (orientation == Horizontal) 12 else 15)
  size = Size(if (orientation == Horizontal) length + 2 else scrollerSize.width + 2, if (orientation == Horizontal) scrollerSize.height + 2 else length + 2)

  override def init(channel: Publisher, context: Context): Unit = {
    this.context = context
    listenTo(context)
  }

  override def update(mousePos: Point): Unit = {

  }

  def scroll(dir: Int): Unit = {
    def clamp(): Unit = {
      if (orientation == Horizontal)
        scrollerPos = Point(scrollerPos.x.min(length - 14).max(1), 1)
      else
        scrollerPos = Point(1, scrollerPos.y.min(length - 14).max(1))
    }
    if (orientation == Horizontal) {
      scrollerPos -= Point(dir * stepSize, 0)
    } else
      scrollerPos -= Point(0, dir * stepSize)
    clamp()
  }

  def clampLocal(pos: Point): Point = {
    val l: Int = length - 14
    if (orientation == Horizontal) {
      Point((l min (pos.x - 16 / 2)) max 1, 1)
    } else {
      Point(1, (l min (pos.y - 16 / 2)) max 1)
    }
  }

  def clamp(p: Point): Point = clampLocal(local(p))

  reactions += {
    case e: MouseEvent.Drag if enabled =>
      if (e.lastButton == MouseButton.Left && clicked) {
        scrollerPos = clamp(e.pos)
      }
    case e: MouseEvent.Click if enabled => clicked = scrollerBounds.contains(local(e.pos))
      if (!clicked && hovered(e.pos))
        scrollerPos = clamp(e.pos)
    case e: MouseEvent.Scroll if enabled && !GuiScreen.isShiftKeyDown =>
      parent match {
        case s: ScrollPanel =>
          if (orientation == Vertical) {
            if (parent.hovered(e.pos) && (!s.scrollHorizontal || !s.scrollBarHorizontal.enabled)) {
              scroll(e.direction)
            } else if (parent.hovered(e.pos) && !GuiScreen.isCtrlKeyDown)
              scroll(e.direction)
            else if (hovered(e.pos))
              scroll(e.direction)
          } else if (orientation == Horizontal) {
            if (parent.hovered(e.pos) && (!s.scrollVertical || !s.scrollBarVertical.enabled))
              scroll(e.direction)
            else if (parent.hovered(e.pos) && GuiScreen.isCtrlKeyDown)
              scroll(e.direction)
            else if (hovered(e.pos))
              scroll(e.direction)
          }
        case _ =>
          if ((orientation == Vertical && parent.hovered(e.pos)) || (orientation == Horizontal && parent.hovered(e.pos) && GuiScreen.isCtrlKeyDown)) {
            scroll(e.direction)
          }
      }
  }

  override def hovered(mousePosition: Point): Boolean = screenBounds contains mousePosition

  def offset = if (orientation == Horizontal) scrollerPos.x - 1 else scrollerPos.y - 1

  def offset_=(offset: Int): Unit = {
    if (orientation == Horizontal) {
      scrollerPos = Point(clampLocal(Point(offset, 0)).x, 1)
    } else {
      scrollerPos = Point(1, clampLocal(Point(0, offset)).y)
    }
  }

  def scrollerBounds = Rectangle(scrollerPos, scrollerSize)

  var stepSize = 18
  private var clicked = false
  private var scrollerPos = Point(1, 1)
  override var skin: Skin = new ScrollBarSkin

  class ScrollBarSkin extends Skin {
    override protected def drawForeground(mousePos: Point): Unit = {
      val drawable = TextureManager("scroller:" + (if (orientation == Horizontal) "horizontal" else "vertical") + (if (!enabled) ":disabled" else "")).getOrElse(null)
      if (drawable != null) {
        drawable.size = scrollerSize
        drawable.draw(mousePos, screen + scrollerPos, zIndex)
      }
    }
  }

}

object ScrollBar {

  object Orientation {
    val Vertical = 0
    val Horizontal = 1
  }

}