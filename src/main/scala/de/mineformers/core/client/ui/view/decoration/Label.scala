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
package de.mineformers.core.client.ui.view.decoration

import de.mineformers.core.client.ui.proxy.Context
import de.mineformers.core.client.ui.util.Shadow
import de.mineformers.core.client.ui.util.font.{Font, MCFont}
import de.mineformers.core.client.ui.view.container.Panel.Padding
import de.mineformers.core.client.ui.view.{TextView, View}
import de.mineformers.core.client.util.Color
import de.mineformers.core.reaction.GlobalPublisher
import de.mineformers.core.util.math.shape2d.{Point, Size}

/**
 * Label
 *
 * @author PaleoCrafter
 */
class Label(private var _text: String) extends View with TextView {
  private var _font: Font = MCFont.DefaultDark
  size = font.size(_text)
  skin = new LabelSkin

  var shadow: Shadow = null
  var padding = Padding.None

  override def init(channel: GlobalPublisher, context: Context): Unit = {
    super.init(channel, context)
    this.size = font.size(text) + Size(padding.left + padding.right, padding.top + padding.bottom)
  }

  override def textOff: Point = Point(padding.left, padding.top)

  override def font_=(font: Font): Unit = {
    this._font = font
    size = font.size(text) + Size(padding.left + padding.right, padding.top + padding.bottom)
  }

  override def font = _font

  override def text_=(text: String): Unit = {
    this._text = text
    size = font.size(text) + Size(padding.left + padding.right, padding.top + padding.bottom)
  }

  override def text = _text

  override def update(mousePos: Point): Unit = {
  }

  class LabelSkin extends TextSkin {
    override def drawForeground(mousePos: Point): Unit = {
      if (shadow != null) {
        shadow.draw(text, view.screen.x + textOff.x, view.screen.y + textOff.y, view.zIndex, font, Color(font.color))
      }
      super.drawForeground(mousePos)
    }
  }

}
