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
package de.mineformers.core.client.ui.component.container

import de.mineformers.core.client.shape2d.{Point, Size}
import de.mineformers.core.client.ui.component.container.Frame.Anchor
import de.mineformers.core.client.ui.proxy.{Context, UIScreen}
import de.mineformers.core.reaction.Publisher
import net.minecraft.client.Minecraft

/**
 * Frame
 *
 * @author PaleoCrafter
 */
class Frame(size0: Size) extends Panel {
  size = size0

  override def init(channel: Publisher, context: Context): Unit = {
    this.screen = position + anchor.pos(this, proxy)
    super.init(channel, context)
  }

  def showProxy(): Unit = {
    this.proxy = newProxy
    Minecraft.getMinecraft.displayGuiScreen(proxy)
  }

  def newProxy = new UIScreen(this)

  override def update(mousePos: Point): Unit = {
    super.update(mousePos)
    this.screen = position + anchor.pos(this, proxy)
  }

  var proxy: UIScreen = _
  var anchor = Anchor.Center
}

object Frame {

  object Anchor extends Enumeration {
    val HorizontalCenter = Value("horizontalCenter", (frame: Frame, screen: UIScreen) => Point((screen.width - frame.width) / 2, 0))
    val VerticalCenter = Value("verticalCenter", (frame: Frame, screen: UIScreen) => Point(0, (screen.height - frame.height) / 2))
    val Center = Value("center", (frame: Frame, screen: UIScreen) => Point((screen.width - frame.width) / 2, (screen.height - frame.height) / 2))
    val TopLeft = Value("topLeft", (frame: Frame, screen: UIScreen) => Point(0, 0))
    val TopRight = Value("topRight", (frame: Frame, screen: UIScreen) => Point(screen.width - frame.width, 0))
    val BottomLeft = Value("bottomLeft", (frame: Frame, screen: UIScreen) => Point(0, screen.height - frame.height))
    val BottomRight = Value("bottomRight", (frame: Frame, screen: UIScreen) => Point(screen.width - frame.width, screen.height - frame.height))

    class AnchorVal(name: String, val pos: (Frame, UIScreen) => Point) extends Val(nextId, name)

    protected final def Value(name: String, pos: (Frame, UIScreen) => Point): AnchorVal = new AnchorVal(name, pos)
  }

}