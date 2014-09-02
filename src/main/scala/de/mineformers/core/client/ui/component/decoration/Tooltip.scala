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

package de.mineformers.core.client.ui.component.decoration

import de.mineformers.core.client.ui.component.TextComponent
import de.mineformers.core.client.ui.util.Font
import de.mineformers.core.client.shape2d.{Size, Point}

/**
 * Tooltip
 *
 * @author PaleoCrafter
 */
class Tooltip(private var _text: String, var font: Font = Font.Default) extends TextComponent {
  zIndex = 100
  override def text: String = _text

  override def text_=(text: String): Unit = {
    _text = text
    this.size = Size(font.width(text) + 8, font.height(text) + 8)
  }

  override def textOff: Point = Point(4, 4)

  override def update(mousePos: Point): Unit = ()
}
