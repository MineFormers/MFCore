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
package de.mineformers.core.client.ui.view.interaction

import de.mineformers.core.client.ui.state.{StringProperty, ViewState}
import de.mineformers.core.client.ui.view.interaction.NavigationButton.Orientation.Orientation
import de.mineformers.core.util.math.shape2d.Size

/**
 * NavigationButton
 *
 * @author PaleoCrafter
 */
class NavigationButton(orientation: Orientation) extends Button("") {
  size = if (orientation.vertical) Size(15, 10) else Size(10, 15)

  override def defaultState(state: ViewState): Unit = super.defaultState(state.set(NavigationButton.OrientationProperty, orientation.name))
}

object NavigationButton {
  final val OrientationProperty = new StringProperty("orientation", "left", Orientation.values.map(_.toString).toSeq)

  object Orientation extends Enumeration {
    type Orientation = OrientationVal
    final val Left = Value("left", vertical = false)
    final val Right = Value("right", vertical = false)
    final val Up = Value("up", vertical = true)
    final val Down = Value("down", vertical = true)

    class OrientationVal(val name: String, val vertical: Boolean) extends Val(nextId, name) {
      override def toString(): String = name
    }

    protected final def Value(name: String, vertical: Boolean) = new OrientationVal(name, vertical)
  }

}