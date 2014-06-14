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

package de.mineformers.core.client.ui.component.interaction

import de.mineformers.core.client.ui.component.TextComponent
import de.mineformers.core.client.shape2d.{Size, Point}
import de.mineformers.core.client.ui.util.{MouseButton, Font}
import de.mineformers.core.client.ui.reaction.ComponentEvent.{ButtonPressed, ComponentClicked}

/**
 * Button
 *
 * @author PaleoCrafter
 */
class Button(var text: String, initSize: Size = Size(50, 20)) extends TextComponent {
  this.size = initSize

  reactions += {
    case ComponentClicked(c, button) =>
      if ((button == MouseButton.Left) && (c eq this))
        publish(ButtonPressed(this))
  }

  override def update(mousePos: Point): Unit = {
    if (hovered(mousePos) && !identifier.endsWith("hovered"))
      identifier = identifier + ":hovered"
    else if (!hovered(mousePos) && identifier.endsWith("hovered"))
      identifier = identifier.replace(":hovered", "")
  }

  override def textOff: Point = bounds.centerInSize(font.size(text))

  var font: Font = Font.DefaultShadow
}
