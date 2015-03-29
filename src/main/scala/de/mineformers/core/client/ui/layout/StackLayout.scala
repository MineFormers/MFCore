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

import de.mineformers.core.client.ui.view.View
import de.mineformers.core.client.ui.view.container.Panel
import de.mineformers.core.util.math.shape2d.{Point, Size}

/**
 * StackLayout
 *
 * @author PaleoCrafter
 */
class StackLayout(var gap: Int = 2, var horizontal: Boolean = false, var cached: Boolean = true) extends LayoutManager[StackConstraints] {
  override def defaultConstraints: StackConstraints = null

  override def positionFor(panel: Panel, view: View): Point = {
    if (this(view) != null && cached)
      this(view).pos
    else {
      var pos = Point(if (!horizontal) view.x else 0, if (!horizontal) 0 else view.y)
      val iterator = panel.content.iterator.buffered
      import scala.util.control.Breaks._
      breakable(while (iterator.hasNext) {
        val current = iterator.next()
        if (current eq view)
          break()
        if (iterator.head eq view) {
          val p = if (cached) this(current).pos else positionFor(panel, current)
          pos = p + Point(if (horizontal) current.width + gap else view.x, if (horizontal) view.y - current.y else current.height + gap)
        }
      })
      if (cached)
        setConstraints(view, StackConstraints(pos))
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

  override def usableSize(panel: Panel, view: View): Size = {
    val fillHorizontal = panel.content.count(_.maxSize.width == Integer.MAX_VALUE)
    val fillVertical = panel.content.count(_.maxSize.height == Integer.MAX_VALUE)
    val viewHorizontal = panel.content.view.filterNot(_.maxSize.width == Integer.MAX_VALUE)
    val viewVertical = panel.content.view.filterNot(_.maxSize.height == Integer.MAX_VALUE)
    val notFilledHorizontal = viewHorizontal.size
    val notFilledVertical = viewVertical.size
    val usedWidth = viewHorizontal.foldLeft(0)(_ + _.width)
    val usedHeight = viewVertical.foldLeft(0)(_ + _.height)
    val usableWidth = if(!horizontal || fillHorizontal == 0) panel.screenPaddingBounds.width else (panel.screenPaddingBounds.width - usedWidth - notFilledHorizontal * gap) / fillHorizontal - gap
    val usableHeight = if(horizontal || fillVertical == 0) panel.screenPaddingBounds.height else (panel.screenPaddingBounds.height - usedHeight - notFilledVertical * gap) / fillVertical - gap

    Size(usableWidth, usableHeight)
  }
}

case class StackConstraints(pos: Point) extends Constraints
