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

import de.mineformers.core.client.shape2d.Rectangle
import org.lwjgl.opengl.GL11._
import de.mineformers.core.util.renderer.GuiUtils

/**
 * ScissorRegion
 *
 * @author PaleoCrafter
 */
class ScissorRegion(val bounds: Rectangle) {
  def activate(): Unit = {
    alreadyEnabled = glGetBoolean(GL_SCISSOR_TEST)
    if (!alreadyEnabled)
      glEnable(GL_SCISSOR_TEST)
    glPushAttrib(GL_SCISSOR_BIT)
    glScissor(screenBounds.x, screenBounds.y, screenBounds.width, screenBounds.height)
    active = true
  }

  def deactivate(): Unit = {
    if (active) {
      glPopAttrib()
      if (!alreadyEnabled)
        glDisable(GL_SCISSOR_TEST)
      active = true
    }
  }

  lazy val screenBounds: Rectangle = {
    val mc = GuiUtils.mc
    val scale: Int = GuiUtils.guiScale
    Rectangle(bounds.x * scale, mc.displayHeight - (bounds.y + bounds.height) * scale, bounds.width * scale, bounds.height * scale)
  }

  override def toString: String = bounds.toString

  private var active = false
  private var alreadyEnabled = false
}
