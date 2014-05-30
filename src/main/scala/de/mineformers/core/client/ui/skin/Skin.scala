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

package de.mineformers.core.client.ui.skin

import de.mineformers.core.client.ui.component.Component
import scala.collection.immutable.HashMap
import de.mineformers.core.util.renderer.GuiUtils
import de.mineformers.core.client.shape2d.Point
import de.mineformers.core.client.ui.skin.drawable.Drawable
import de.mineformers.core.client.ui._

/**
 * Skin
 *
 * @author PaleoCrafter
 */
object Skin {
  private var defaults = HashMap.empty[String, Skin[Comp]]
  private var activeSkins = HashMap.empty[String, Skin[Comp]]

  def apply[C <: Component[C]](comp: Component[C]): Skin[C] = {
    if (!activeSkins.contains(comp.identifier))
      if (defaults.contains(comp.identifier)) {
        activeSkins += comp.identifier -> defaults(comp.identifier)
      } else {
        defaults += comp.identifier -> comp.defaultSkin.asInstanceOf[Skin[Comp]]
        activeSkins += comp.identifier -> comp.defaultSkin.asInstanceOf[Skin[Comp]]
      }
    activeSkins(comp.identifier).asInstanceOf[Skin[C]]
  }


  def load(): Unit = {

  }

  def setDefault[C <: Component[C]](id: String, skin: Skin[C]): Unit = {
    defaults += id -> skin.asInstanceOf[Skin[Comp]]
  }

  def set[C <: Component[C]](id: String, skin: Skin[C]): Unit = {
    activeSkins += id -> skin.asInstanceOf[Skin[Comp]]
  }
}

trait Skin[+C <: Component[C]] {
  val utils = GuiUtils

  protected def drawBackground(mousePos: Point, component: C): Unit = {
    if (background != null) {
      background.size = component.size
      background.draw(mousePos, component.screen)
    }
  }

  protected def drawForeground(mousePos: Point, component: C): Unit

  def draw(mousePos: Point, component: C): Unit = {
    drawBackground(mousePos, component)
    drawForeground(mousePos, component)
  }

  var background: Drawable = null
}