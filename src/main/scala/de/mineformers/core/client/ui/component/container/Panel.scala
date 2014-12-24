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

import de.mineformers.core.client.shape2d.{Point, Rectangle, Size}
import de.mineformers.core.client.ui.component.Component
import de.mineformers.core.client.ui.component.container.Panel.Padding
import de.mineformers.core.client.ui.layout.{Constraints, LayoutManager}
import de.mineformers.core.client.ui.proxy.Context
import de.mineformers.core.client.ui.skin.ScissorRegion
import de.mineformers.core.reaction.Publisher
import org.lwjgl.opengl.GL11

/**
 * Panel
 *
 * @author PaleoCrafter
 */
class Panel extends Component {
  override def init(channel: Publisher, context: Context): Unit = {
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
      size = contentSize
  }

  override def update(mousePos: Point): Unit = {
    content.foreach(c => {
      if (layout != null)
        c.screen = screen + layout.positionFor(this, c) + Point(padding.left, padding.right)
      else
        c.screen = screen + c.position + Point(padding.left, padding.right)
      c.update(mousePos)
    })
  }

  /**
   * Installed reaction won't receive events from the given publisher anylonger.
   */
  override def deafTo(ps: Publisher*): Unit = {
    super.deafTo(ps: _*)
    content foreach {
      _.deafTo(ps: _*)
    }
  }

  def deafToNonChildren(ps: Publisher*): Unit = {
    super.deafTo(ps: _*)
  }

  def add(c: Component): Unit = this.add(c, if (layout != null) layout.defaultConstraints else null)

  def add(c: Component, constraints: Constraints): Unit = {
    content :+= c
    if (layout != null)
      layout.setConstraints(c, constraints)
  }

  def contentSize: Size = {
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
      Size(width, height)
    }
  }

  def scissorRegion: ScissorRegion = {
    var rect = Rectangle(screenBounds.start + Point(padding.left, padding.top), screenBounds.end - Point(padding.right, padding.bottom))
    if (parent != null) {
      rect = (rect & parent.scissorRegion.bounds).orNull
    }
    new ScissorRegion(rect)
  }

  def deepTooltip(p: Point): String = {
    content foreach {
      c =>
        if (c.tooltip != null && c.hovered(p))
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

  def content_=(content: Seq[Component]): Unit = _content = content

  override var skin: Skin = new PanelSkin
  var layout: LayoutManager[_ <: Constraints] = _
  var clip = true
  var padding: Padding = Padding(4)
  private var _content = Seq.empty[Component]

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
