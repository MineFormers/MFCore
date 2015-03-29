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

import de.mineformers.core.client.ui.proxy.Context
import de.mineformers.core.client.ui.util.MouseButton
import de.mineformers.core.client.ui.util.ViewEvent.{ButtonPressed, ViewClicked}
import de.mineformers.core.client.ui.util.font.{Font, MCFont}
import de.mineformers.core.client.ui.view.{TextView, View}
import de.mineformers.core.reaction.GlobalPublisher
import de.mineformers.core.util.math.shape2d.{Point, Size}
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.util.ResourceLocation

/**
 * Button
 *
 * @author PaleoCrafter
 */
class Button(var text: String, initSize: Size = Size(0, 0)) extends View with TextView {
  var font: Font = MCFont.DefaultShadow
  this.size = initSize
  if (initSize == Size(0, 0))
    size = font.size(text) + Size(10, 10)

  globalReactions += {
    case ViewClicked(c, pos, button) =>
      if (button == MouseButton.Left && (c eq this)) {
        mc.getSoundHandler.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F))
        publish(ButtonPressed(this))
      }
  }

  override def init(channel: GlobalPublisher, context: Context): Unit = {
    super.init(channel, context)
    if (size == Size(0, 0))
      size = font.size(text) + Size(10, 10)
  }

  override def update(mousePos: Point): Unit = {
    if (context.findAffectedView(mousePos) eq this)
      font = font.withColor(0xFFFFA0)
    else
      font = MCFont.DefaultShadow
  }

  override def textOff: Point = bounds.centerInSize(font.size(text))

  override def toString: String = s"Button(text=$text, $propertyString)"
}
