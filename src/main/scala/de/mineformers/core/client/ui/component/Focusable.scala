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
package de.mineformers.core.client.ui.component

import de.mineformers.core.client.shape2d.Point
import de.mineformers.core.client.ui.state.{Property, ComponentState}
import de.mineformers.core.client.ui.util.MouseButton
import de.mineformers.core.client.ui.util.MouseButton.MouseButton

/**
 * Focusable
 *
 * @author PaleoCrafter
 */
trait Focusable extends Component {
  abstract override def defaultState(state: ComponentState) = super.defaultState(state.set(Property.Focused, false))

  def onClick(pos: Point, button: MouseButton): Unit = {
    if (button == MouseButton.Left) {
      if (!hovered(pos) && focused && canLoseFocus) {
        noFocus()
      } else if (hovered(pos) && !focused) {
        focus()
      }
    }
  }

  def focused = _focused

  private[this] def focused_=(state: Boolean): Unit = _focused = state

  def focus(): Unit = {
    focused = true
    state.set(Property.Focused, focused)
    context.focused = this
    gainFocus()
  }

  def noFocus(): Unit = {
    focused = false
    state.set(Property.Focused, focused)
    context.focused = null
    loseFocus()
  }

  def gainFocus(): Unit

  def loseFocus(): Unit

  def canLoseFocus: Boolean

  private var _focused = !canLoseFocus
}
