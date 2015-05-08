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
package de.mineformers.core.util.renderer

import java.io.IOException
import javax.imageio.ImageIO

import de.mineformers.core.client.ui.util.font.MCFont
import de.mineformers.core.client.util.Color
import de.mineformers.core.client.util.RenderUtils._
import de.mineformers.core.util.ResourceUtils.Resource
import de.mineformers.core.util.math.shape2d.{Rectangle, Size}
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.{GlStateManager, OpenGlHelper, RenderHelper, Tessellator}
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL20._

/**
 * GuiUtils
 *
 * @author PaleoCrafter
 */
object GuiUtils {
  def init(): Unit = {
    shaders.init()
  }

  def resetColor(): Unit = glColor4f(1F, 1F, 1F, 1F)

  def guiScale: Int = {
    val mc: Minecraft = Minecraft.getMinecraft
    var scaleFactor: Int = 1
    var k: Int = mc.gameSettings.guiScale
    if (k == 0) {
      k = 1000
    }
    while (scaleFactor < k && mc.displayWidth / (scaleFactor + 1) >= 320 && mc.displayHeight / (scaleFactor + 1) >= 240) {
      scaleFactor += 1
    }
    scaleFactor
  }

  def customScale(size: Size, scale: Int): Size = {
    val res = scaledResolution
    val scaled = Size(size.width / res.getScaleFactor, size.height / res.getScaleFactor)
    Size(scaled.width * scale, size.width * scale)
  }

  def scaledResolution: ScaledResolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight)

  def scaledWidth(width: Int): Int = width / scaledResolution.getScaleFactor

  def scaledHeight(height: Int): Int = height / scaledResolution.getScaleFactor

  def scale(rectangle: Rectangle) = Rectangle(rectangle.start / scaledResolution.getScaleFactor, rectangle.end / scaledResolution.getScaleFactor)

  def revertScale(rectangle: Rectangle) = Rectangle(rectangle.start * scaledResolution.getScaleFactor, rectangle.end * scaledResolution.getScaleFactor)

  def colorFromRGB(r: Int, g: Int, b: Int): Int = (0xFF0000 & (r << 16)) | (0x00FF00 & (g << 8)) | (0x0000FF & b)

  def stringWidth(text: String): Int = mc.fontRendererObj.getStringWidth(text)

  def longestString(strings: String*): String = {
    var s: String = ""
    var longest: Int = 0
    for (string <- strings) {
      if (longest < string.length) {
        s = string
        longest = string.length
      }
    }
    s
  }

  def drawString(text: String, x: Int, y: Int, z: Int, font: MCFont): Unit = {
    font.draw(text, x, y, z)
  }

  def drawString(text: String, x: Int, y: Int, z: Int, color: Int, drawShadow: Boolean) {
    GL11.glPushMatrix()
    GL11.glDisable(GL11.GL_DEPTH_TEST)
    GL11.glTranslatef(0, 0, z)
    var i = 0
    val height = mc.fontRendererObj.FONT_HEIGHT
    for (line <- text.split("\\n").mkString("\\\\n").split("\\\\n")) {
      mc.fontRendererObj.drawString(line, x, y + i * (height + 1), color, drawShadow)
      i += 1
    }
    GL11.glTranslatef(0, 0, -z)
    GL11.glEnable(GL11.GL_DEPTH_TEST)
    GL11.glPopMatrix()
  }

  def drawSplitString(text: String, x: Int, y: Int, color: Int, drawShadow: Boolean) {
    val splits: Array[String] = text.split("<br>")
    var i: Int = 0
    while (i < splits.length) {
      mc.fontRendererObj.drawString(splits(i), x, y + i * (mc.fontRendererObj.FONT_HEIGHT + 1), color, drawShadow)
      i += 1
    }
  }

  def drawLine(color: Color, startX: Int, startY: Int, endX: Int, endY: Int, width: Float, zLevel: Int) {
    glDisable(GL11.GL_TEXTURE_2D)
    glEnable(GL11.GL_BLEND)
    glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    glColor4f(color.r, color.g, color.b, color.a)
    glLineWidth(width)
    glBegin(GL11.GL_LINES)
    glVertex3f(startX, startY, zLevel)
    glVertex3f(endX, endY, zLevel)
    glEnd()
    glDisable(GL11.GL_BLEND)
    glColor4f(1F, 1F, 1F, 1F)
    glEnable(GL11.GL_TEXTURE_2D)
  }

  def drawRectangle(color: Int, x: Int, y: Int, z: Int, width: Int, height: Int) {
    drawRectangle(color, 1F, x, y, z, width, height)
  }

  def drawRectangle(color: Int, alpha: Float, x: Int, y: Int, z: Int, width: Int, height: Int) {
    val rgb: Color = Color(color, alpha)
    drawRectangle(rgb, x, y, z, width, height)
  }

  def drawRectangle(color: Color, x: Int, y: Int, z: Int, width: Int, height: Int) {
    GL11.glDisable(GL11.GL_TEXTURE_2D)
    GL11.glColor4f(color.r, color.g, color.b, color.a)
    drawQuad(x, y, z, width, height, 0, 0, 1, 1)
    GL11.glColor4f(1F, 1F, 1F, 1F)
    GL11.glEnable(GL11.GL_TEXTURE_2D)
  }

  def drawRectangle(texture: ResourceLocation, x: Int, y: Int, z: Int, width: Int, height: Int, u: Int, v: Int) {
    drawRectangle(texture, x, y, z, width, height, u, v, 256, 256)
  }

  def drawRectangle(texture: ResourceLocation, x: Int, y: Int, z: Int, width: Int, height: Int, u: Int, v: Int, textureWidth: Int, textureHeight: Int) {
    bindTexture(texture)
    drawRectangle(x, y, z, width, height, u, v, textureWidth, textureHeight)
  }

  def drawRectangle(x: Int, y: Int, z: Int, width: Int, height: Int, u: Int, v: Int) {
    drawRectangle(x, y, z, width, height, u, v, 256, 256)
  }

  def drawRectangle(x: Int, y: Int, z: Int, width: Int, height: Int, u: Int, v: Int, textureWidth: Int, textureHeight: Int) {
    drawQuad(x, y, z, width, height, u.asInstanceOf[Float] / textureWidth, v.asInstanceOf[Float] / textureHeight, (u + width).asInstanceOf[Float] / textureWidth, (v + height).asInstanceOf[Float] / textureHeight)
  }

  def drawQuad(x: Int, y: Int, z: Int, width: Int, height: Int, u: Float, v: Float, uMax: Float, vMax: Float) {
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    val tessellator: Tessellator = Tessellator.getInstance
    val renderer = tessellator.getWorldRenderer
    renderer.startDrawingQuads()
    renderer.addVertexWithUV(x, y + height, z, u, vMax)
    renderer.addVertexWithUV(x + width, y + height, z, uMax, vMax)
    renderer.addVertexWithUV(x + width, y, z, uMax, v)
    renderer.addVertexWithUV(x, y, z, u, v)
    tessellator.draw()
    glDisable(GL_BLEND)
  }

  def drawRectangleRepeated(texture: ResourceLocation, x: Int, y: Int, z: Int, width: Int, height: Int, u: Float, v: Float, uMax: Float, vMax: Float, tileWidth: Int, tileHeight: Int) {
    bindTexture(texture)
    drawRectangleRepeated(x, y, z, width, height, u, v, uMax, vMax, tileWidth, tileHeight)
  }

  def drawRectangleRepeated(x: Int, y: Int, z: Int, width: Int, height: Int, u: Float, v: Float, uMax: Float, vMax: Float, tileWidth: Int, tileHeight: Int) {
    shaders.activate()
    shaders.setUniform1i("tex", 0)
    shaders.setUniform2f("iconOffset", u, v)
    shaders.setUniform2f("iconSize", uMax - u, vMax - v)
    drawQuad(x, y, z, width, height, 0, 0, scaledWidth(width).asInstanceOf[Float] / tileWidth, scaledHeight(height).asInstanceOf[Float] / tileHeight)
    shaders.deactivate()
  }

  def drawRectangleXRepeated(x: Int, y: Int, z: Int, width: Int, height: Int, u: Float, v: Float, uMax: Float, vMax: Float, tileWidth: Int) {
    shaders.activate()
    shaders.setUniform1i("tex", 0)
    shaders.setUniform2f("iconOffset", u, 0)
    shaders.setUniform2f("iconSize", uMax - u, 1)
    drawQuad(x, y, z, width, height, 0, v, scaledWidth(width).asInstanceOf[Float] / tileWidth, vMax)
    shaders.deactivate()
  }

  def drawRectangleYRepeated(x: Int, y: Int, z: Int, width: Int, height: Int, u: Float, v: Float, uMax: Float, vMax: Float, tileHeight: Int) {
    shaders.activate()
    shaders.setUniform1i("tex", 0)
    shaders.setUniform2f("iconOffset", 0, v)
    shaders.setUniform2f("iconSize", 1, vMax - v)
    drawQuad(x, y, z, width, height, u, 0, uMax, scaledHeight(height).asInstanceOf[Float] / tileHeight)
    shaders.deactivate()
  }

  def drawItemStack(stack: ItemStack, x: Int, y: Int, z: Int, customAmount: String = null) {
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F)
    RenderHelper.enableGUIStandardItemLighting()
    GlStateManager.enableDepth()
    GlStateManager.enableAlpha()
    GlStateManager.disableBlend()
    GlStateManager.enableRescaleNormal()
    val oldZ = renderItem.zLevel
    renderItem.zLevel = z - 101F
    renderItem.renderItemAndEffectIntoGUI(stack, x, y)
    val font = stack.getItem.getFontRenderer(stack)
    renderItem.renderItemOverlayIntoGUI(if (font != null) font else mc.fontRendererObj, stack, x, y, customAmount)
    renderItem.zLevel = oldZ
    GlStateManager.disableDepth()
    GlStateManager.enableAlpha()
    GlStateManager.enableBlend()
    RenderHelper.disableStandardItemLighting()
  }

  private def shaders: ShaderSystem = {
    if (_shaders == null) {
      _shaders = new ShaderSystem
      _shaders.addShader(REPEAT_SHADER, GL_FRAGMENT_SHADER)
    }
    _shaders
  }

  def imageSize(resource: Resource): Size = {
    try {
      val image = ImageIO.read(resource.toInputStream)
      Size(image.getWidth, image.getHeight)
    } catch {
      case e: IOException =>
        Size(0, 0)
    }
  }

  private final lazy val renderItem = mc.getRenderItem
  private var _shaders: ShaderSystem = null
  private final val REPEAT_SHADER: String =
    """#version 120
      |uniform sampler2D tex; uniform vec2 iconOffset; uniform vec2 iconSize;
      |void main() {
      | gl_FragColor = texture2D(tex, iconOffset + fract(gl_TexCoord[0].st) * iconSize) * gl_Color;
      |}
    """.stripMargin
}


