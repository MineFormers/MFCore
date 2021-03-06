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
package de.mineformers.core.client.ui.view.container

import de.mineformers.core.client.ui.proxy.Context
import de.mineformers.core.client.ui.util.MouseEvent
import de.mineformers.core.client.ui.view.View
import de.mineformers.core.client.ui.view.container.Panel.Padding
import de.mineformers.core.client.ui.view.interaction.ScrollBar
import de.mineformers.core.client.ui.view.interaction.ScrollBar.Orientation
import de.mineformers.core.reaction.GlobalPublisher
import de.mineformers.core.util.math.shape2d.{Point, Size}
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.opengl.GL11

/**
 * ScrollPanel
 *
 * @author PaleoCrafter
 */
class ScrollPanel(_size: Size, private var _scrollHorizontal: Boolean = true, private var _scrollVertical: Boolean = true) extends Panel {
  size = _size
  padding = Padding(0, 0, if (scrollVertical) 14 else 0, if (scrollHorizontal) 14 else 0)
  skin = new ScrollPanelSkin
  clip = true

  globalReactions += {
    case e: MouseEvent.Scroll =>
      if(context.findAffectedView(e.p) == this || (hovered(e.p) && enabled && !context.findAffectedView(e.p).isInstanceOf[ScrollPanel])) {
        if (scrollBarHorizontal.enabled && scrollHorizontal && (GuiScreen.isCtrlKeyDown || !scrollVertical))
          scrollBarHorizontal.scroll(e.direction)
        if (scrollBarVertical.enabled && scrollVertical && (!GuiScreen.isCtrlKeyDown || !scrollHorizontal))
          scrollBarVertical.scroll(e.direction)
      }
  }

  override def init(channel: GlobalPublisher, context: Context): Unit = {
    this.listenTo(channel)
    super.init(channel, context)
    this.channel = channel
    scrollBarHorizontal.parent = this
    scrollBarVertical.parent = this
    if (scrollHorizontal)
      scrollBarHorizontal.init(channel, context)
    if (scrollVertical)
      scrollBarVertical.init(channel, context)

    scrollBarHorizontal.position = Point(0, height - 14)
    scrollBarVertical.position = Point(width - 14, 0)
    scrollBarHorizontal.screen = screen + scrollBarHorizontal.position
    scrollBarVertical.screen = screen + scrollBarVertical.position
  }

  override def findView(mousePos: Point, predicate: View => Boolean): View = {
    var result = super.findView(mousePos, predicate)
    if (result eq this) {
      if (predicate(scrollBarHorizontal))
        result = scrollBarHorizontal
      if (predicate(scrollBarVertical))
        result = scrollBarVertical
    }
    result
  }

  override def onParentResized(newSize: Size, oldSize: Size, delta: Size): Unit = {
    super.onParentResized(newSize, oldSize, delta)
    resetScrollBars()
  }

  override def contains(view: View): Boolean = super.contains(view) || (view eq scrollBarHorizontal) || (view eq scrollBarVertical)

  override def update(mousePos: Point): Unit = {
    scrollBarHorizontal.screen = screen + scrollBarHorizontal.position
    scrollBarVertical.screen = screen + scrollBarVertical.position
    val contentSize = this.contentSize(withPadding = false)
    scrollBarHorizontal.enabled = contentSize.width > width - padding.right - padding.left
    scrollBarVertical.enabled = contentSize.height > height - padding.bottom - padding.top
    var x = -((scrollBarHorizontal.offset / (width - padding.left - padding.right - scrollBarHorizontal.scrollerBounds.width - 2).toFloat) * (contentSize.width - width + padding.left + padding.right)).toInt
    var y = -((scrollBarVertical.offset / (height - padding.top - padding.bottom - scrollBarVertical.scrollerBounds.height - 2).toFloat) * (contentSize.height - height + padding.top + padding.bottom)).toInt

    if (contentSize.width < width - padding.right)
      x = 0
    if (contentSize.height < height - padding.bottom)
      y = 0
    val off = screen + Point(x, y)
    content.foreach(c => {
      if (layout != null)
        c.screen = off + layout.positionFor(this, c) + Point(padding.left, padding.top)
      else
        c.screen = off + c.position + Point(padding.left, padding.top)
      c.update(mousePos)
    })
  }

  def scrollHorizontal = _scrollHorizontal

  def scrollHorizontal_=(scroll: Boolean) = {
    _scrollHorizontal = scroll
    resetScrollBars()
  }

  def scrollVertical = _scrollVertical

  def scrollVertical_=(scroll: Boolean) = {
    _scrollVertical = scroll
    resetScrollBars()
  }

  def resetScrollBars(): Unit = {
    val hOff = scrollBarHorizontal.offset
    val vOff = scrollBarVertical.offset
    scrollBarHorizontal = new ScrollBar(size.width - (if (scrollVertical) 16 else 2), Orientation.Horizontal)
    scrollBarHorizontal.offset = hOff
    scrollBarVertical = new ScrollBar(size.height - (if (scrollHorizontal) 16 else 2), Orientation.Vertical)
    scrollBarVertical.offset = vOff
    scrollBarHorizontal.position = Point(0, height - 14)
    scrollBarVertical.position = Point(width - 14, 0)
    scrollBarHorizontal.parent = this
    scrollBarVertical.parent = this
    scrollBarHorizontal.init(channel, context)
    scrollBarVertical.init(channel, context)
  }

  private[view] var scrollBarHorizontal = new ScrollBar(size.width - (if (scrollVertical) 16 else 2), Orientation.Horizontal)
  private[view] var scrollBarVertical = new ScrollBar(size.height - (if (scrollHorizontal) 16 else 2), Orientation.Vertical)
  private var channel: GlobalPublisher = _

  class ScrollPanelSkin extends PanelSkin {
    override def drawForeground(mousePos: Point): Unit = {
      super.drawForeground(mousePos)
      GL11.glColor4f(1F, 1F, 1F, 1F)
      if (scrollHorizontal)
        scrollBarHorizontal.skin.draw(mousePos)
      if (scrollVertical)
        scrollBarVertical.skin.draw(mousePos)
    }
  }

}
