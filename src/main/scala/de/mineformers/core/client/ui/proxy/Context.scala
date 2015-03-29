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

import de.mineformers.core.client.ui.util.Positioned
import de.mineformers.core.client.ui.view.container.{DebugWindow, Frame}
import de.mineformers.core.client.ui.view.{Focus, View}
import de.mineformers.core.reaction.{Event, GlobalPublisher}
import de.mineformers.core.util.math.shape2d.{Point, Size}

/**
 * Context
 *
 * @author PaleoCrafter
 */
trait Context extends GlobalPublisher {
  final val debug = true
  protected var frames: Seq[Frame] = if (debug) Seq(new DebugWindow) else Seq()
  var attached: View = _
  var focused: Focus = _

  def close(): Unit

  def size: Size

  def findAffectedView(mousePos: Point): View = findView(mousePos, c => c.hovered(mousePos) && c.visible && c.enabled)

  def findHoveredView(mousePos: Point): View = findView(mousePos, c => c.hovered(mousePos))

  def findView(mousePos: Point, predicate: View => Boolean): View

  def canReceiveEvent(view: View, event: Event): Boolean

  def addFrames(frames: Frame*): Unit = {
    if(debug)
      this.frames = this.frames.dropRight(1) ++ frames ++ Seq(this.frames.last)
    else
      this.frames ++= frames
  }

  override def publish(e: Event): Unit = {
    e match {
      case p: Positioned =>
        val reactor = findAffectedView(p.pos)
        if (reactor != null && canReceiveEvent(reactor, e))
          reactor.reactions(p)
      case _ =>
    }
    super.publish(e)
  }
}