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
import de.mineformers.core.client.ui.component.{View, Focus}
import de.mineformers.core.client.ui.util.Positioned
import de.mineformers.core.reaction.{GlobalPublisher, Event, Publisher}

/**
 * Context
 *
 * @author PaleoCrafter
 */
trait Context extends GlobalPublisher {
  var attached: View = _
  var focused: Focus = _

  def close(): Unit

  def size: Size

  def findAffectedComponent(mousePos: Point): View = findComponent(mousePos, c => c.hovered(mousePos) && c.visible && c.enabled)

  def findHoveredComponent(mousePos: Point): View = findComponent(mousePos, c => c.hovered(mousePos))

  def findComponent(mousePos: Point, predicate: View => Boolean): View

  override def publish(e: Event): Unit = {
    e match {
      case p: Positioned =>
        val reactor = findAffectedComponent(p.pos)
        if (reactor != null)
          reactor.reactions(p)
      case _ =>
    }
    super.publish(e)
  }
}