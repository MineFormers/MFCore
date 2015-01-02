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
package de.mineformers.core.client.ui.component.container

import de.mineformers.core.util.math.shape2d.{Point, Rectangle, Size}
import de.mineformers.core.client.ui.component.View
import de.mineformers.core.client.ui.component.container.Panel.Padding
import de.mineformers.core.client.ui.layout.{Constraints, LayoutManager}
import de.mineformers.core.client.ui.proxy.Context
import de.mineformers.core.client.ui.skin.ScissorRegion
import de.mineformers.core.reaction.GlobalPublisher
import org.lwjgl.opengl.GL11

/**
 * Panel
 *
 * @author PaleoCrafter
 */
class Panel extends View {
  override def init(channel: GlobalPublisher, context: Context): Unit = {
    super.init(channel, context)
    content.foreach(c => {
      c.parent = this
      c.init(channel, context)
      if (layout != null)
        c.screen = screen + layout.positionFor(this, c) + Point(padding.left, padding.right)
      else
        c.screen = screen + c.position + Point(padding.left, padding.right)
    })
    if (size == Size(0, 0))
      size = contentSize()
  }

  override def updateState(mousePos: Point): Unit = {
    super.updateState(mousePos)
    content.foreach(c => {
      c.updateState(mousePos)
    })
  }

  override def dispose(): Unit = {
    super.dispose()
    content.foreach(_.dispose())
  }

  override def update(mousePos: Point): Unit = {
    content.foreach(c => {
      if (layout != null)
        c.screen = screen + layout.positionFor(this, c) + Point(padding.left, padding.top)
      else
        c.screen = screen + c.position + Point(padding.left, padding.top)
      if (!sizeUpdate)
        c.onParentResized(this.size, this.size, Size(0, 0))
      c.update(mousePos)
    })
    if (!sizeUpdate)
      sizeUpdate = true
  }

  /**
   * Installed reaction won't receive events from the given publisher anylonger.
   */
  override def deafTo(ps: GlobalPublisher*): Unit = {
    super.deafTo(ps: _*)
    content foreach {
      _.deafTo(ps: _*)
    }
  }

  def deafToNonChildren(ps: GlobalPublisher*): Unit = {
    super.deafTo(ps: _*)
  }

  def addViews(cs: View*): Unit = cs.foreach(add)

  def add(c: View): Unit = this.add(c, if (layout != null) layout.defaultConstraints else null)

  def add(c: View, constraints: Constraints): Unit = {
    content :+= c
    if (layout != null)
      layout.setConstraints(c, constraints)
  }

  def findComponent(mousePos: Point, predicate: View => Boolean): View = {
    var result: View = null
    if (predicate(this))
      result = this
    for (c <- content) {
      c match {
        case p: Panel => val pResult = p.findComponent(mousePos, predicate)
          if (pResult != null)
            result = pResult
        case comp =>
          if (predicate(comp))
            result = comp
      }
    }
    result
  }

  override def size_=(size: Size): Unit = {
    val oldSize = this.size
    super.size_=(size)
    if (this.size != oldSize)
      content.foreach(_.onParentResized(this.size, oldSize, this.size - oldSize))
  }

  def contentSize(withPadding: Boolean = true): Size = {
    if (layout != null) layout.size(this)
    else {
      var width = 0
      var height = 0
      for (component <- content) {
        if (component.x + component.width > width)
          width = component.x + component.width
        if (component.y + component.height > height)
          height = component.y + component.height
      }
      if (withPadding)
        Size(width + padding.left + padding.right, height + padding.top + padding.bottom)
      else
        Size(width, height)
    }
  }

  def screenPaddingBounds = Rectangle(screen + Point(padding.left, padding.top), width - padding.left - padding.right, height - padding.top - padding.bottom)

  def scissorRegion: ScissorRegion = {
    var rect = Rectangle(screenBounds.start + Point(padding.left, padding.top), screenBounds.end - Point(padding.right, padding.bottom))
    if (parent != null) {
      rect = (rect & parent.scissorRegion.bounds).getOrElse(Rectangle(0, 0, 0, 0))
    }
    new ScissorRegion(rect)
  }

  def deepTooltip(p: Point): String = {
    content foreach {
      c =>
        if (c.tooltip != null && (context.findHoveredComponent(p) eq c))
          return c.tooltip
        else
          c match {
            case panel: Panel =>
              val deep = panel.deepTooltip(p)
              if (deep != null)
                return deep
            case _ =>
          }
    }
    null
  }

  def content = _content

  def content_=(content: Seq[View]): Unit = _content = content

  override var skin: Skin = new PanelSkin
  var layout: LayoutManager[_ <: Constraints] = _
  var clip = true
  var padding: Padding = Padding(4)
  private var _content = Seq.empty[View]
  protected var sizeUpdate = false

  override def toString: String = s"Panel(layout=$layout, clip=$clip, padding=$padding, content=${content.mkString("[", ",", "]")})"

  class PanelSkin extends Skin {
    override def drawForeground(mousePos: Point): Unit = {
      val scissor = scissorRegion
      if (clip) {
        scissor.activate()
      }
      content.foreach { c =>
        GL11.glColor4f(1F, 1F, 1F, 1F)
        c.skin.draw(mousePos)
      }
      if (clip)
        scissor.deactivate()
    }
  }

}

object Panel {

  case class Padding(left: Int, top: Int, right: Int, bottom: Int)

  object Padding {
    final val None = Padding(0)

    def apply(horizontal: Int, vertical: Int): Padding = Padding(horizontal, vertical, horizontal, vertical)

    def apply(all: Int): Padding = Padding(all, all)
  }

}
