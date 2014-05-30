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

import net.minecraft.client.Minecraft
import cpw.mods.fml.client.FMLClientHandler
import org.lwjgl.util.Color
import org.lwjgl.opengl.{GL12, GL11}
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.entity.RenderItem
import net.minecraft.util.{IIcon, ResourceLocation}
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL20._
import net.minecraft.client.gui.ScaledResolution
import de.mineformers.core.client.ui.util.Font

/**
 * GuiUtils
 *
 * @author PaleoCrafter
 */
object GuiUtils {
  def mc: Minecraft = FMLClientHandler.instance.getClient

  def bindTexture(path: ResourceLocation): Unit = {
    mc.getTextureManager.bindTexture(path)
  }

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

  def scaledResolution: ScaledResolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight)

  def scaledWidth(width: Int): Int = width / scaledResolution.getScaleFactor

  def scaledHeight(height: Int): Int = height / scaledResolution.getScaleFactor

  def colorFromRGB(r: Int, g: Int, b: Int): Int = (0xFF0000 & (r << 16)) | (0x00FF00 & (g << 8)) | (0x0000FF & b)

  def colorFromRGB(color: Color): Int = (0xFF0000 & (color.getRed << 16)) | (0x00FF00 & (color.getGreen << 8)) | (0x0000FF & color.getBlue)

  def rgbFromColor(color: Int): Color = new Color((0xFF0000 & color) >> 16, (0x00FF00 & color) >> 8, 0x0000FF & color)

  def stringWidth(text: String): Int = mc.fontRenderer.getStringWidth(text)

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

  def drawString(text: String, x: Int, y: Int, z: Int, font: Font): Unit = {
    font.draw(text, x, y, z)
  }

  def drawString(text: String, x: Int, y: Int, color: Int, drawShadow: Boolean, zLevel: Int) {
    GL11.glDisable(GL11.GL_DEPTH_TEST)
    GL11.glDisable(GL12.GL_RESCALE_NORMAL)
    GL11.glTranslatef(0, 0, zLevel)
    mc.fontRenderer.drawString(text, x, y, color, drawShadow)
    GL11.glTranslatef(0, 0, -zLevel)
    GL11.glEnable(GL12.GL_RESCALE_NORMAL)
    GL11.glEnable(GL11.GL_DEPTH_TEST)
  }

  def drawSplitString(text: String, x: Int, y: Int, color: Int, drawShadow: Boolean) {
    val splits: Array[String] = text.split("<br>")
    var i: Int = 0
    while (i < splits.length) {
      mc.fontRenderer.drawString(splits(i), x, y + i * (mc.fontRenderer.FONT_HEIGHT + 1), color, drawShadow)
      i += 1
    }
  }

  def drawLine(color: Color, startX: Int, startY: Int, endX: Int, endY: Int, width: Float, zLevel: Int) {
    val multiplier: Float = 1F / 255F
    glDisable(GL11.GL_TEXTURE_2D)
    glEnable(GL11.GL_BLEND)
    glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    glColor4f(multiplier * color.getRed, multiplier * color.getGreen, multiplier * color.getBlue, multiplier * color.getAlpha)
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
    val rgb: Color = rgbFromColor(color)
    rgb.setAlpha((alpha * 255).asInstanceOf[Int])
    drawRectangle(rgb, x, y, z, width, height)
  }

  def drawRectangle(color: Color, x: Int, y: Int, z: Int, width: Int, height: Int) {
    GL11.glDisable(GL11.GL_TEXTURE_2D)
    GL11.glColor4f(color.getRed / 255F, color.getGreen / 255F, color.getBlue / 255F, color.getAlpha / 255F)
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
    val tessellator: Tessellator = Tessellator.instance
    tessellator.startDrawingQuads()
    tessellator.addVertexWithUV(x, y + height, z, u, vMax)
    tessellator.addVertexWithUV(x + width, y + height, z, uMax, vMax)
    tessellator.addVertexWithUV(x + width, y, z, uMax, v)
    tessellator.addVertexWithUV(x, y, z, u, v)
    tessellator.draw
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

  def drawIcon(icon: IIcon, x: Int, y: Int, z: Int, width: Int, height: Int) {
    drawQuad(x, y, z, width, height, icon.getMinU, icon.getMinV, icon.getMaxU, icon.getMaxV)
  }

  def renderItemIntoGUI(stack: ItemStack, x: Int, y: Int) {
    renderItem.renderItemIntoGUI(mc.fontRenderer, mc.getTextureManager, stack, x, y)
  }

  private final val renderItem: RenderItem = new RenderItem
  private lazy val shaders: ShaderSystem = {
    val system = new ShaderSystem
    shaders.addShader(REPEAT_SHADER, GL_FRAGMENT_SHADER)
    system
  }
  private final val REPEAT_SHADER: String = "#version 120\n" + "uniform sampler2D tex; uniform vec2 iconOffset; uniform vec2 iconSize;\n" + "void main() {\n" + "gl_FragColor = texture2D(tex, iconOffset + fract(gl_TexCoord[0].st) * iconSize) * gl_Color;\n" + "}"
}

