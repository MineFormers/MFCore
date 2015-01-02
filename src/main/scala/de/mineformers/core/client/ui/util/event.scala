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
package de.mineformers.core.client.ui.util

import de.mineformers.core.util.math.shape2d.Point
import de.mineformers.core.client.ui.component.View
import de.mineformers.core.client.ui.component.interaction.Button
import de.mineformers.core.client.ui.util.MouseButton.MouseButton
import de.mineformers.core.reaction.Event

trait Positioned extends Event {
  private[util] val p: Point

  def pos: Point = p
}

object ComponentEvent {

  case class ValueChanged(c: View, oldVal: Any, newVal: Any) extends ComponentEvent(c)

  case class ComponentClicked(c: View, mousePos: Point, button: MouseButton) extends ComponentEvent(c)

  case class ButtonPressed(b: Button) extends ComponentEvent(b)

}

class ComponentEvent(c: View) extends Event

object MouseEvent {

  case class Click(p: Point, buttonCode: Int) extends MouseEvent(p) {
    def button = MouseButton.get(buttonCode)
  }

  case class ContinuousClick(p: Point, buttonCode: Int, duration: Long) extends MouseEvent(p) {
    def button = MouseButton.get(buttonCode)
  }

  case class DoubleClick(p: Point, buttonCode: Int) extends MouseEvent(p) {
    def button = MouseButton.get(buttonCode)
  }

  case class Move(p: Point, lastPos: Point) extends MouseEvent(p)

  case class Drag(p: Point, lastPos: Point, lastButtonCode: Int, timeSinceClick: Long) extends MouseEvent(p) {
    def lastButton = MouseButton.get(lastButtonCode)
  }

  case class Release(p: Point, buttonCode: Int) extends MouseEvent(p) {
    def button = MouseButton.get(buttonCode)
  }

  case class Scroll(p: Point, direction: Int) extends MouseEvent(p)

}

abstract class MouseEvent(p: Point) extends Event with Positioned

object KeyEvent {

  case class Type(private val char: Char, private val code: Int) extends KeyEvent(char, code)

}

abstract class KeyEvent(char: Char, code: Int) extends Event