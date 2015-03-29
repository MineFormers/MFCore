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

import de.mineformers.core.client.ui.util.ViewEvent.{ButtonPressed, ValueChanged}
import de.mineformers.core.client.ui.util.{MouseButton, MouseEvent}
import de.mineformers.core.client.ui.view.container.Panel
import de.mineformers.core.client.ui.view.container.Panel.Padding
import de.mineformers.core.client.ui.view.interaction.NavigationButton.Orientation
import de.mineformers.core.util.math.shape2d.{Point, Size}

/**
 * NumberSpinner
 *
 * @author PaleoCrafter
 */
class NumberSpinner(textWidth: Int = 50, var min: Int = 1, var max: Int = -1, var start: Int = 1, var step: Int = 1) extends Panel {
  this.padding = Padding.None
  private var _value = start
  val text = new TextBox(start.toString, Size(textWidth, 12))
  text.formatter = TextBox.IntegerFormatter
  val btnUp = new NavigationButton(Orientation.Up)
  btnUp.style = "numberSpinner"
  btnUp.size = Size(9, 6)
  btnUp.position = Point(textWidth + 1, 0)
  val btnDown = new NavigationButton(Orientation.Down)
  btnDown.style = "numberSpinner"
  btnDown.position = Point(textWidth + 1, 6)
  btnDown.size = Size(9, 6)
  this.size = Size(textWidth + 10, 12)

  add(text)
  add(btnUp)
  add(btnDown)

  text.reactions += {
    case e: MouseEvent.Scroll =>
      value += e.direction
  }

  btnUp.reactions += {
    case e: MouseEvent.ContinuousClick if e.button == MouseButton.Left =>
      if(e.duration > 300)
        value += 1
  }

  btnDown.reactions += {
    case e: MouseEvent.ContinuousClick if e.button == MouseButton.Left =>
      if(e.duration > 300)
        value -= 1
  }

  listenTo(text, btnUp, btnDown)

  globalReactions += {
    case e: ValueChanged =>
      if (e.v eq text)
        if (text.text.nonEmpty) {
          value = text.text.toInt
        }
    case ButtonPressed(b) =>
      if (b eq btnUp)
        value += 1
      else if (b eq btnDown)
        value -= 1
  }

  def value: Int = _value

  def value_=(value: Int): Unit = {
    val old = this.value
    this._value = value max min
    if (max != -1)
      this._value = this.value min max
    if (old != this.value) {
      publish(ValueChanged(this, old, this.value))
      text.setText(this.value.toString, notify = false)
    }
  }
}
