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

import de.mineformers.core.client.ui.component.Component
import de.mineformers.core.client.shape2d.{Size, Point}
import de.mineformers.core.client.ui.reaction.Publisher
import de.mineformers.core.client.ui.proxy.Context
import de.mineformers.core.client.ui.skin.Skin
import de.mineformers.core.client.ui.skin.container.PanelSkin
import de.mineformers.core.client.ui.layout.{Constraints, LayoutManager}

/**
 * Panel
 *
 * @author PaleoCrafter
 */
class Panel extends Component[Panel] {
  override def init(channel: Publisher, context: Context): Unit = {
    super.init(channel, context)
    content.foreach(_.init(channel, context))
  }

  override def update(mousePos: Point): Unit = {
    content.foreach(c => {
      if (layout != null)
        c.screen = screen + layout.positionFor(this, c)
      else
        c.screen = screen + c.position
      c.update(mousePos)
    })
  }

  def add(c: Component[_]): Unit = this.add(c, if (layout != null) layout.defaultConstraints else null)

  def add(c: Component[_], constraints: Constraints): Unit = {
    content :+ c
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

  override def defaultSkin: Skin[Panel] = new PanelSkin

  var content = Seq[Component[Any]]()

  var layout: LayoutManager[_ <: Constraints] = _
}
