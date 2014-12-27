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

import de.mineformers.core.client.shape2d.{Point, Rectangle, Size}
import de.mineformers.core.client.ui.component.container.Panel
import de.mineformers.core.client.ui.proxy.Context
import de.mineformers.core.client.ui.skin.TextureManager
import de.mineformers.core.client.ui.state.{ComponentState, Property}
import de.mineformers.core.client.ui.util.ComponentEvent.ComponentClicked
import de.mineformers.core.client.ui.util.{MouseButton, MouseEvent}
import de.mineformers.core.reaction.Publisher
import de.mineformers.core.util.renderer.GuiUtils
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.client.FMLClientHandler

/**
 * Component
 *
 * @author PaleoCrafter
 */
abstract class Component extends Publisher {
  def init(channel: Publisher, context: Context): Unit = {
    listenTo(channel)
    this.context = context
  }

  reactions += {
    case MouseEvent.Click(p, code) => if (hovered(p) && enabled && visible) context.publish(ComponentClicked(this, MouseButton(code)))
  }

  def updateState(mousePos: Point): Unit = {
    state.set(Property.Hovered, hovered(mousePos))
  }

  def update(mousePos: Point): Unit

  def dispose(): Unit = {
    if (context != null)
      deafTo(context)
  }

  def bounds: Rectangle = {
    if (_bounds == null || _bounds.width != width || _bounds.height != height || _bounds.start != position)
      _bounds = Rectangle(position, width, height)
    _bounds
  }

  def screenBounds: Rectangle = {
    if (_screenBounds == null || _screenBounds.width != width || _screenBounds.height != height || _screenBounds.start != screen)
      _screenBounds = Rectangle(screen, width, height)
    _screenBounds
  }

  def x = position.x

  def y = position.y

  def width = size.width

  def height = size.height

  def hovered(mousePosition: Point): Boolean = {
    if (parent != null && parent.clip) (screenBounds contains mousePosition) && parent.hovered(mousePosition) else screenBounds contains mousePosition
  }

  def local(p: Point) = p - screen

  def enabled = _enabled

  def enabled_=(enabled: Boolean) = {
    _enabled = enabled
    state.set(Property.Enabled, enabled)
  }

  private def createState = {
    val state = ComponentState.create(Property.Hovered, Property.Enabled)
    state.set(Property.Enabled, true)
    defaultState(state)
    state
  }

  def defaultState(state: ComponentState): Unit = ()

  var skin: Skin
  lazy val mc: Minecraft = FMLClientHandler.instance.getClient
  val state = createState
  val utils = GuiUtils
  var position = Point(0, 0)
  var screen = Point(0, 0)
  var size = Size(0, 0)
  var context: Context = _
  private var _enabled: Boolean = true
  var visible: Boolean = true
  var parent: Panel = _
  var name: String = _
  var tooltip: String = _
  var identifier: String = getClass.getSimpleName.toLowerCase
  var background = identifier
  var zIndex = 0
  private var _bounds: Rectangle = Rectangle(position, width, height)
  private var _screenBounds: Rectangle = Rectangle(screen, width, height)

  trait Skin {
    val utils = GuiUtils
    val component = Component.this

    protected def drawBackground(mousePos: Point): Unit = {
      val drawable = TextureManager(component).getOrElse(TextureManager(background).orNull)
      if (drawable != null) {
        drawable.size = size
        drawable.draw(mousePos, screen, zIndex)
      }
    }

    protected def drawForeground(mousePos: Point): Unit

    def draw(mousePos: Point): Unit = {
      drawBackground(mousePos)
      drawForeground(mousePos)
    }
  }

}