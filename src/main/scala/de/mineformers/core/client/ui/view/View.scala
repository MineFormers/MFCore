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
package de.mineformers.core.client.ui.view

import javafx.scene.control.Skin

import de.mineformers.core.util.math.shape2d.{Point, Rectangle, Size}
import de.mineformers.core.client.ui.view.container.Panel
import de.mineformers.core.client.ui.proxy.Context
import de.mineformers.core.client.ui.skin.TextureManager
import de.mineformers.core.client.ui.skin.drawable.StaticTexture
import de.mineformers.core.client.ui.state.{ViewState, Property}
import de.mineformers.core.client.ui.util.ViewEvent.ViewClicked
import de.mineformers.core.client.ui.util.{MouseButton, MouseEvent}
import de.mineformers.core.reaction.{GlobalPublisher, Publisher}
import de.mineformers.core.util.renderer.GuiUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.fml.client.FMLClientHandler

/**
 * View
 *
 * @author PaleoCrafter
 */
abstract class View extends GlobalPublisher {
  def init(channel: GlobalPublisher, context: Context): Unit = {
    this.state.set(Property.Name, name)
    listenTo(channel)
    this.context = context
    maxSize = Size(if(maxSize.width == 0) context.size.width else maxSize.width, if(maxSize.height == 0) context.size.height else maxSize.height)
  }

  reactions += {
    case MouseEvent.Click(p, code) =>
      if (hovered(p) && enabled && visible) context.publish(ViewClicked(this, local(p), MouseButton.get(code)))
  }

  def updateState(mousePos: Point): Unit = {
    state.set(Property.Hovered, context.findHoveredView(mousePos) eq this)
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

  def onParentResized(newSize: Size, oldSize: Size, delta: Size): Unit = {
    if(parent != null) {
      val usable = parent.usableSize(this)
      if (maxSize.width == Integer.MAX_VALUE) {
        this.size = Size(usable.width, size.height)
      }
      if (maxSize.height == Integer.MAX_VALUE) {
        this.size = Size(size.width, usable.height)
      }
    }
  }

  def x = position.x

  def y = position.y

  def width = size.width

  def height = size.height

  def hovered(mousePosition: Point): Boolean = {
    if (parent != null && parent.clip) (screenBounds contains mousePosition) && parent.hovered(mousePosition) && (parent.screenPaddingBounds contains mousePosition) else screenBounds contains mousePosition
  }

  def local(p: Point) = p - screen

  def enabled = _enabled

  def enabled_=(enabled: Boolean) = {
    _enabled = enabled
    state.set(Property.Enabled, enabled)
  }

  def size = _size

  def size_=(size: Size): Unit = {
    _size = Size(size.width.max(minSize.width), size.height.max(minSize.height))
    if (maxSize != Size(0, 0))
      _size = Size(this.size.width.min(maxSize.width), this.size.height.min(maxSize.height))
  }

  private def createState = {
    val state = ViewState.create(Property.Hovered, Property.Enabled, Property.Style)
    state.set(Property.Enabled, true)
    defaultState(state)
    state
  }

  def style = state(Property.Style)

  def style_=(style: String) = state.set(Property.Style, style)

  def defaultState(state: ViewState): Unit = ()

  def propertyString = s"size=$size, minSize=$minSize, maxSize=$maxSize, position=$position, screenPosition=$screen, bounds=$bounds, visible=$visible"

  var skin: Skin
  lazy val mc: Minecraft = FMLClientHandler.instance.getClient
  val state = createState
  val utils = GuiUtils
  var position = Point(0, 0)
  var screen = Point(0, 0)
  private var _size = Size(0, 0)
  var minSize = Size(0, 0)
  var maxSize = Size(0, 0)
  var context: Context = _
  private var _enabled: Boolean = true
  var visible: Boolean = true
  var parent: Panel = _
  var name: String = _
  var tooltip: String = _
  var identifier: String = getClass.getSimpleName()(0).toLower + getClass.getSimpleName.substring(1)
  var background = identifier
  var zIndex = 0
  private var _bounds: Rectangle = Rectangle(position, width, height)
  private var _screenBounds: Rectangle = Rectangle(screen, width, height)
  protected var hasBackground = true

  trait Skin {
    var stretchStatic = true
    val utils = GuiUtils
    val view = View.this

    protected def drawBackground(mousePos: Point): Unit = {
      GlStateManager.enableDepth()
      val drawable = TextureManager(view).getOrElse(TextureManager(background).orNull)
      if (drawable != null && hasBackground) {
        if (!drawable.isInstanceOf[StaticTexture] || stretchStatic)
          drawable.size = size
        else
          drawable.size = drawable.textureSize
        drawable.draw(mousePos, screen, zIndex)
      }
      GlStateManager.disableDepth()
    }

    protected def drawForeground(mousePos: Point): Unit

    def draw(mousePos: Point): Unit = {
      drawBackground(mousePos)
      drawForeground(mousePos)
    }
  }

}