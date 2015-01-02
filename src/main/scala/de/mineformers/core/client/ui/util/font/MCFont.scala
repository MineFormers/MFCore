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
package de.mineformers.core.client.ui.util.font

import de.mineformers.core.util.math.shape2d.Size
import de.mineformers.core.client.util.Color
import de.mineformers.core.client.util.RenderUtils.mc
import de.mineformers.core.util.renderer.GuiUtils
import net.minecraft.client.gui.FontRenderer
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.{GL11, GL12}

import scala.collection.mutable

/**
 * Font
 *
 * @author PaleoCrafter
 */
object MCFont {
  private val renderers: mutable.HashMap[String, FontRenderer] = mutable.HashMap[String, FontRenderer]("default" -> mc.fontRendererObj, "small" -> new FontRenderer(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.renderEngine, true))
  val Default = MCFont("default")
  val DefaultShadow = MCFont("default", drawShadow = true)
  val DefaultDark = MCFont("default", 0x404040)
  val DefaultDarkShadow = MCFont("default", 0x404040, drawShadow = true)
  val DefaultLight = MCFont("default", 0xF5F5F5)
  val DefaultLightShadow = MCFont("default", 0xF5F5F5, drawShadow = true)
  val Small = MCFont("small")
  val SmallShadow = MCFont("small", drawShadow = true)
  val SmallDark = MCFont("small", 0x404040)
  val SmallDarkShadow = MCFont("small", 0x404040, drawShadow = true)
  val SmallLight = MCFont("small", 0xF5F5F5)
  val SmallLightShadow = MCFont("small", 0xF5F5F5, drawShadow = true)

  def apply(name: String, color: Int = 0xe0e0e0, drawShadow: Boolean = false) = new MCFont(name, color, drawShadow)

  def rendererFor(id: String) = renderers.getOrElse(id, renderers("default"))

  def addRenderer(id: String, renderer: FontRenderer) = renderers + id -> renderer
}

class MCFont(name: String, var color: Int = 0xe0e0e0, var drawShadow: Boolean = false) extends Font {
  override def draw(text: String, x: Int, y: Int, z: Int, color: Int): Unit = {
    GL11.glDisable(GL11.GL_DEPTH_TEST)
    GL11.glDisable(GL12.GL_RESCALE_NORMAL)
    GL11.glTranslatef(0, 0, z)
    val rgba = Color(color)
//    GL11.glColor4f(rgba.r, rgba.g, rgba.b, rgba.a)
    var i = 0
    for (line <- text.split("\\n").mkString("<br>").split("<br>")) {
      renderer.drawString(line, x, y + i * (height + 1), color, drawShadow)
      i += 1
    }
    GL11.glTranslatef(0, 0, -z)
    GL11.glEnable(GL12.GL_RESCALE_NORMAL)
    GL11.glEnable(GL11.GL_DEPTH_TEST)
  }

  override def height = renderer.FONT_HEIGHT

  override def width(lines: String*): Int = renderer.getStringWidth(GuiUtils.longestString(lines: _*))

  override def height(lines: String*): Int = (height + 1) * lines.length - 1

  override def charWidth(char: Char): Int = renderer.getCharWidth(char)

  override def fit(text: String, width: Int, reverse: Boolean): String = renderer.trimStringToWidth(text, width, reverse)

  lazy val renderer = MCFont.rendererFor(name)
}
