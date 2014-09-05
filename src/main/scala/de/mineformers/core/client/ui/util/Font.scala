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

package de.mineformers.core.client.ui.util

import scala.collection.mutable
import net.minecraft.client.gui.FontRenderer
import de.mineformers.core.client.util.RenderUtils.mc
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.{GL12, GL11}
import de.mineformers.core.util.renderer.GuiUtils
import de.mineformers.core.client.shape2d.Size

/**
 * Font
 *
 * @author PaleoCrafter
 */
object Font {
  private val renderers: mutable.HashMap[String, FontRenderer] = mutable.HashMap[String, FontRenderer]("default" -> mc.fontRenderer, "small" -> new FontRenderer(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.renderEngine, false))

  val Default = Font("default")
  val DefaultShadow = Font("default", drawShadow = true)
  val DefaultDark = Font("default", 0x404040)
  val DefaultDarkShadow = Font("default", 0x404040, drawShadow = true)
  val DefaultLight = Font("default", 0xF5F5F5)
  val DefaultLightShadow = Font("default", 0xF5F5F5, drawShadow = true)
  val Small = Font("small")
  val SmallShadow = Font("small", drawShadow = true)
  val SmallDark = Font("small", 0x404040)
  val SmallDarkShadow = Font("small", 0x404040, drawShadow = true)
  val SmallLight = Font("default", 0xF5F5F5)
  val SmallLightShadow = Font("default", 0xF5F5F5, drawShadow = true)

  def apply(name: String, color: Int = 0xe0e0e0, drawShadow: Boolean = false) = new Font(name, color, drawShadow)

  def rendererFor(id: String) = renderers.getOrElse(id, renderers("default"))

  def addRenderer(id: String, renderer: FontRenderer) = renderers + id -> renderer

}

class Font(name: String, var color: Int = 0xe0e0e0, var drawShadow: Boolean = false) {

  def draw(text: String, x: Int, y: Int): Unit = draw(text, x, y, 0, color)

  def draw(text: String, x: Int, y: Int, z: Int): Unit = draw(text, x, y, z, color)

  def draw(text: String, x: Int, y: Int, z: Int, color: Int): Unit = {
    GL11.glDisable(GL11.GL_DEPTH_TEST)
    GL11.glDisable(GL12.GL_RESCALE_NORMAL)
    GL11.glTranslatef(0, 0, z)
    var i = 0
    for (line <- text.split("\\n").mkString("\\\\n").split("\\\\n")) {
      renderer.drawString(line, x, y + i * (height + 1), color, drawShadow)
      i += 1
    }
    GL11.glTranslatef(0, 0, -z)
    GL11.glEnable(GL12.GL_RESCALE_NORMAL)
    GL11.glEnable(GL11.GL_DEPTH_TEST)
  }

  def height = renderer.FONT_HEIGHT

  def width(text: String): Int = width(text.split("\\n").mkString("\\\\n").split("\\\\n"): _*)

  def width(lines: String*): Int = renderer.getStringWidth(GuiUtils.longestString(lines: _*))

  def height(text: String): Int = height(text.split("\\n").mkString("\\\\n").split("\\\\n"): _*)

  def height(lines: String*): Int = (height + 1) * lines.length - 1

  def charWidth(char: Char): Int = renderer.getCharWidth(char)

  def fit(text: String, width: Int): String = fit(text, width, reverse = false)

  def fit(text: String, width: Int, reverse: Boolean): String = renderer.trimStringToWidth(text, width, reverse)

  def size(text: String): Size = Size(width(text), height(text))

  lazy val renderer = Font.rendererFor(name)

}
