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

import de.mineformers.core.client.ui.reaction.Publisher
import de.mineformers.core.client.shape2d.{Size, Rectangle, Point}
import net.minecraft.client.Minecraft
import de.mineformers.core.client.ui.proxy.Context
import de.mineformers.core.client.ui.component.container.Panel
import cpw.mods.fml.client.FMLClientHandler
import de.mineformers.core.client.ui.skin.Skin
import de.mineformers.core.util.renderer.GuiUtils

/**
 * Component
 *
 * @author PaleoCrafter
 */
trait Component[+A <: Component[A]] extends Publisher {
  this: A =>

  def init(channel: Publisher, context: Context): Unit = {
    listenTo(channel)
    this.context = context
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

  def hovered(mousePosition: Point) = screenBounds contains mousePosition

  def skin: Skin[A] = Skin(this).asInstanceOf[Skin[A]]

  def defaultSkin: Skin[A]

  lazy val mc: Minecraft = FMLClientHandler.instance.getClient
  val utils = GuiUtils
  var position = Point(0, 0)
  var screen = Point(0, 0)
  var size = Size(0, 0)
  var context: Context = _
  var enabled: Boolean = true
  var visible: Boolean = true
  var parent: Panel = _
  var name: String = _
  var tooltip: String = _
  var identifier: String = getClass.getSimpleName
  private var _bounds: Rectangle = Rectangle(position, width, height)
  private var _screenBounds: Rectangle = Rectangle(screen, width, height)

}
