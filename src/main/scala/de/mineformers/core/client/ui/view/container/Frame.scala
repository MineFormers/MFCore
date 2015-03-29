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
package de.mineformers.core.client.ui.view.container

import de.mineformers.core.client.ui.view.container.Frame.Anchor
import de.mineformers.core.client.ui.view.container.Panel.Padding
import de.mineformers.core.client.ui.view.interaction.FrameControl
import de.mineformers.core.client.ui.view.{Drag, View}
import de.mineformers.core.client.ui.proxy.{Context, UIScreen}
import de.mineformers.core.client.ui.state.{BooleanProperty, ViewState}
import de.mineformers.core.client.ui.util.MouseButton.MouseButton
import de.mineformers.core.client.ui.util.font.MCFont
import de.mineformers.core.reaction.GlobalPublisher
import de.mineformers.core.util.math.shape2d.{Point, Rectangle, Size}
import net.minecraft.client.Minecraft

import scala.collection.mutable.ListBuffer

/**
 * Frame
 *
 * @author PaleoCrafter
 */
class Frame(size0: Size) extends Panel with Drag {
  size = size0

  private val controls = ListBuffer.empty[FrameControl]
  var title = ""
  var proxy: UIScreen = _
  var anchor = Anchor.Center
  var fixed = true
  var resizable = false
  skin = new FrameSkin
  minSize = Size(50, 50)

  override def init(channel: GlobalPublisher, context: Context): Unit = {
    this.screen = position + anchor.pos(this, context)
    if (!fixed)
      padding = Padding(4, 14, 4, 4)
    super.init(channel, context)
    state.set(Frame.FixedProperty, fixed)
    state.set(Frame.ResizableProperty, resizable)
    for (i <- 0 until controls.size) {
      controls(i).screen = Point(screen.x + width - 11 - 10 * i, screen.y + 2)
    }

    controls.foreach(_.init(channel, context))
  }

  def showProxy(): Unit = {
    this.proxy = newProxy
    Minecraft.getMinecraft.displayGuiScreen(proxy)
  }

  def newProxy = new UIScreen(Seq(this))

  override def defaultState(state: ViewState): Unit = super.defaultState(state.set(Frame.FixedProperty, fixed).set(Frame.ResizableProperty, resizable))

  override def updateState(mousePos: Point): Unit = {
    super.updateState(mousePos)
    controls.foreach(_.updateState(mousePos))
  }

  override def update(mousePos: Point): Unit = {
    super.update(mousePos)
    if (fixed)
      this.screen = position + anchor.pos(this, context)
    for (i <- 0 until controls.size) {
      controls(i).screen = Point(screen.x + width - 11 - 10 * i, screen.y + 2)
      controls(i).zIndex = zIndex
    }
    controls.foreach(_.update(mousePos))
  }

  def addDefaultControls(): Unit = {
    val close = new FrameControl("close", () => context.close())
    close.tooltip = "Close"
    addControl(close)
  }

  def addControl(control: FrameControl): Unit = {
    controls += control
  }

  override def canDrag: Boolean = !fixed

  override def controlRegions: Map[String, Rectangle] = Map("move" -> Rectangle(0, 0, width, 11), "resize" -> Rectangle(width - 5, height - 5, 5, 5))

  override def onDrag(region: String, newPos: Point, lastPos: Point, delta: Point, button: MouseButton): Unit = region match {
    case "move" => super.onDrag(region, newPos, lastPos, delta, button)
    case "resize" =>
      if (resizable) {
        this.size += Size(delta.x, delta.y)
      }
  }

  override def findView(mousePos: Point, predicate: View => Boolean): View = {
    var result = super.findView(mousePos, predicate)
    if (result eq this) {
      controls.foreach(c =>
        if (predicate(c))
          result = c
      )
    }
    result
  }

  override def contains(view: View): Boolean = super.contains(view) || controls.contains(view)

  class FrameSkin extends PanelSkin {
    override def drawForeground(mousePos: Point): Unit = {
      super.drawForeground(mousePos)
      MCFont.Default.draw(title, screen.x + 2, screen.y + 3, zIndex, 0x6a6a6a)
      MCFont.DefaultLight.draw(title, screen.x + 2, screen.y + 2)
      controls.foreach(_.skin.draw(mousePos))
    }
  }

}

object Frame {

  final val FixedProperty = new BooleanProperty("fixed")
  final val ResizableProperty = new BooleanProperty("resizable")

  object Anchor extends Enumeration {
    val HorizontalCenter = Value("horizontalCenter", (frame: Frame, screen: Context) => Point((screen.size.width - frame.width) / 2, 0))
    val VerticalCenter = Value("verticalCenter", (frame: Frame, screen: Context) => Point(0, (screen.size.height - frame.height) / 2))
    val Center = Value("center", (frame: Frame, screen: Context) => Point((screen.size.width - frame.width) / 2, (screen.size.height - frame.height) / 2))
    val TopLeft = Value("topLeft", (frame: Frame, screen: Context) => Point(0, 0))
    val TopRight = Value("topRight", (frame: Frame, screen: Context) => Point(screen.size.width - frame.width, 0))
    val BottomLeft = Value("bottomLeft", (frame: Frame, screen: Context) => Point(0, screen.size.height - frame.height))
    val BottomRight = Value("bottomRight", (frame: Frame, screen: Context) => Point(screen.size.width - frame.width, screen.size.height - frame.height))

    protected final def Value(name: String, pos: (Frame, Context) => Point): AnchorVal = new AnchorVal(name, pos)

    class AnchorVal(name: String, val pos: (Frame, Context) => Point) extends Val(nextId, name)
  }

}