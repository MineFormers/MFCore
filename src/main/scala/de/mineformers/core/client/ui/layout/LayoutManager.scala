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

import de.mineformers.core.client.ui.component.Component
import scala.collection.mutable
import de.mineformers.core.client.shape2d.{Size, Point}
import de.mineformers.core.client.ui.component.container.Panel

/**
 * LayoutManager
 *
 * @author PaleoCrafter
 */
trait LayoutManager[C <: Constraints] {
  def apply(c: Component[_]): Constraints = {
    constraints.getOrElseUpdate(c, defaultConstraints)
  }

  def setConstraints(c: Component[_], constraints: Constraints): Unit = {
    if (constraints != null)
      this.constraints.put(c, constraints.asInstanceOf[C])
    else
      this.constraints.put(c, defaultConstraints)
  }

  def defaultConstraints: C

  def positionFor(panel: Panel, component: Component[_]): Point

  def size(panel: Panel): Size

  val constraints = new mutable.LinkedHashMap[Component[_], C]()
}

trait Constraints