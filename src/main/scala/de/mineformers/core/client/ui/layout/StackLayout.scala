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
package de.mineformers.core.client.ui.layout

import de.mineformers.core.client.shape2d.{Point, Size}
import de.mineformers.core.client.ui.component.Component
import de.mineformers.core.client.ui.component.container.Panel

import scala.util.control.Breaks

/**
 * StackLayout
 *
 * @author PaleoCrafter
 */
class StackLayout(var gap: Int = 2, var horizontal: Boolean = false, var cached: Boolean = true) extends LayoutManager[StackConstraints] {
  override def defaultConstraints: StackConstraints = null

  override def positionFor(panel: Panel, component: Component): Point = {
    if (this(component) != null && cached)
      this(component).pos
    else {
      var pos = Point(if (!horizontal) component.x else 0, if (!horizontal) 0 else component.y)
      val iterator = panel.content.iterator.buffered
      import scala.util.control.Breaks._
      breakable(while (iterator.hasNext) {
        val current = iterator.next()
        if (current eq component)
          break()
        if (iterator.head eq component) {
          val p = if (cached) this(current).pos else positionFor(panel, current)
          pos = p + Point(if (horizontal) current.width + gap else component.x, if (horizontal) component.y - current.y else current.height + gap)
        }
      })
      if (cached)
        setConstraints(component, StackConstraints(pos))
      pos
    }
  }

  override def size(panel: Panel): Size = {
    var width = 0
    var height = 0
    for (c <- panel.content) {
      val pos = positionFor(panel, c)
      if (pos.x + c.width > width)
        width = pos.x + c.width
      if (pos.y + c.height > height)
        height = pos.y + c.height
    }
    Size(width, height)
  }
}

case class StackConstraints(pos: Point) extends Constraints
