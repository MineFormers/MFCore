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

import de.mineformers.core.client.shape2d.{Rectangle, Point, Size}
import de.mineformers.core.client.ui.component.Drag
import de.mineformers.core.client.ui.component.container.Frame.Anchor
import de.mineformers.core.client.ui.component.container.Panel.Padding
import de.mineformers.core.client.ui.component.interaction.FrameControl
import de.mineformers.core.client.ui.proxy.{Context, UIScreen}
import de.mineformers.core.client.ui.state.{BooleanProperty, ComponentState}
import de.mineformers.core.client.ui.util.{Font, MouseButton, MouseEvent}
import de.mineformers.core.reaction.Publisher
import net.minecraft.client.Minecraft

import scala.collection.mutable.ListBuffer

/**
 * Frame
 *
 * @author PaleoCrafter
 */
class Frame(size0: Size) extends Panel with Drag {
  size = size0

  override def init(channel: Publisher, context: Context): Unit = {
    this.screen = position + anchor.pos(this, proxy)
    if (!fixed)
      padding = Padding(4, 8, 4, 4)
    super.init(channel, context)
    state.set(Frame.FixedProperty, fixed)
    for (i <- 0 until controls.size) {
      controls(i).screen = Point(screen.x + width - 11 - 10 * i, screen.y + 2)
    }

    controls.foreach(_.init(channel, context))
  }

  def showProxy(): Unit = {
    this.proxy = newProxy
    Minecraft.getMinecraft.displayGuiScreen(proxy)
  }

  def newProxy = new UIScreen(this)

  override def defaultState(state: ComponentState): Unit = state.set(Frame.FixedProperty, fixed)

  override def updateState(mousePos: Point): Unit = {
    super.updateState(mousePos)
    controls.foreach(_.updateState(mousePos))
  }

  override def update(mousePos: Point): Unit = {
    super.update(mousePos)
    if (fixed)
      this.screen = position + anchor.pos(this, proxy)
    for (i <- 0 until controls.size) {
      controls(i).screen = Point(screen.x + width - 11 - 10 * i, screen.y + 2)
    }
    controls.foreach(_.update(mousePos))
  }

  def addControl(control: FrameControl): Unit = {
    controls += control
  }

  def addDefaultControls(): Unit = {
    val close = new FrameControl("close", () => context.close())
    close.tooltip = "Close"
    addControl(close)
  }

  override def canDrag: Boolean = !fixed

  override def controlRegion: Rectangle = Rectangle(0, 0, width, 11)

  skin = new FrameSkin

  var proxy: UIScreen = _
  var anchor = Anchor.Center
  var fixed = false
  private val controls = ListBuffer.empty[FrameControl]

  class FrameSkin extends PanelSkin {
    override def drawForeground(mousePos: Point): Unit = {
      super.drawForeground(mousePos)
      Font.Default.draw("Test", screen.x + 2, screen.y + 3, zIndex, 0x6a6a6a)
      Font.DefaultLight.draw("Test", screen.x + 2, screen.y + 2)
      controls.foreach(_.skin.draw(mousePos))
    }
  }

}

object Frame {

  final val FixedProperty = new BooleanProperty("fixed")

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