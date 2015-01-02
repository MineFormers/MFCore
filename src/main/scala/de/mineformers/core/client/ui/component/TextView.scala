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

import de.mineformers.core.util.math.shape2d.Point
import de.mineformers.core.client.ui.state.{ComponentState, Property}
import de.mineformers.core.client.ui.util.font.Font
import org.lwjgl.opengl.GL11

/**
 * TextComponent
 *
 * @author PaleoCrafter
 */
trait TextView extends View {
  def font: Font

  def font_=(font: Font): Unit

  def text: String

  def text_=(text: String): Unit

  def textOff: Point = Point(0, 0)

  override var skin: Skin = new TextSkin

  abstract override def defaultState(state: ComponentState): Unit = super.defaultState(state.set(Property.Text, ""))

  class TextSkin extends Skin {
    def drawForeground(mousePos: Point): Unit = {
      GL11.glColor4f(1, 1, 1, 1)
      font.draw(text, component.screen.x + textOff.x, component.screen.y + textOff.y, component.zIndex, font.color)
      GL11.glColor4f(1, 1, 1, 1)
    }
  }

}