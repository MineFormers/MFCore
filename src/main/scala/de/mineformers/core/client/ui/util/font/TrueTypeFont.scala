package de.mineformers.core.client.ui.util.font

/*
 * TrueTyper: Open Source TTF implementation for Minecraft.
 * Copyright (C) 2013 - Mr_okushama, Modified by PaleoCrafter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.image.{BufferedImage, DataBuffer, DataBufferByte, DataBufferInt}
import java.awt.{FontMetrics, Graphics2D, RenderingHints, Color => JColor, Font => JFont}
import java.io.File
import java.nio.{ByteBuffer, ByteOrder, IntBuffer}
import javax.imageio.ImageIO

import de.mineformers.core.client.util.Color
import de.mineformers.core.util.MathUtils
import de.mineformers.core.util.renderer.GuiUtils
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.{GL11, GL12}
import org.lwjgl.util.glu.GLU

import scala.collection.mutable

/**
 * TrueTypeFont
 *
 * @author PaleoCrafter
 */
class TrueTypeFont(font: JFont, antiAlias: Boolean, additionalChars: Array[Char] = null) extends Font {

  /**
   * Array that holds necessary information about the font characters
   */
  private val charArray = Array.ofDim[CharContainer](256)
  /**
   * Map of user defined font characters (Character <-> IntObject)
   */
  private val customChars: mutable.Map[Char, CharContainer] = mutable.HashMap.empty[Char, CharContainer]
  private val fontSize = font.getSize + 3
  /**
   * Default font texture height
   */
  private val textureHeight: Int = 1024
  var color: Int = 0x404040
  private var fontHeight = 0f
  private var fontMetrics: FontMetrics = null
  private var textureId = -1
  /**
   * Default font texture width
   */
  private var textureWidth: Int = 1024

  createSet(additionalChars)

  override def withColor(color: Int): Font = {
    val temp = new TrueTypeFont(font, antiAlias, additionalChars)
    temp.color = color
    temp
  }

  override def fit(text: String, width: Int, reverse: Boolean): String = text

  override def draw(text: String, x: Int, y: Int, z: Int, color: Int): Unit = {
    GL11.glPushMatrix()
    val sr = GuiUtils.scaledResolution
    val scaleX = 1F
    val scaleY = 1F
    var totalWidth: Float = 0
    val c: Int = 8
    var startY = 0
    GL11.glDisable(GL11.GL_DEPTH_TEST)
    GL11.glDisable(GL12.GL_RESCALE_NORMAL)
    GL11.glTranslatef(0, 0, z)

    GL11.glScalef(scaleX, scaleY, 1.0f)

    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId)
    val rgba = Color(color)
    GL11.glColor4f(rgba.r, rgba.g, rgba.b, rgba.a)

    for (charCurrent <- text) {
      val charContainer = if (charCurrent < 256) charArray(charCurrent) else customChars(charCurrent)
      if (charContainer != null) {
        if (charCurrent == '\n') {
          startY += fontHeight.toInt
          totalWidth = 0
        }
        else {
          val uMin = charContainer.x / 1024F
          val vMin = charContainer.y / 1024F
          val uMax = (charContainer.x + charContainer.width) / 1024F
          val vMax = (charContainer.y + charContainer.height) / 1024F
          GuiUtils.drawQuad((x / scaleX + totalWidth).toInt, (y / scaleY + startY).toInt, z, charContainer.width.toInt, charContainer.height.toInt, uMin, vMin, uMax, vMax)
          //drawQuad((totalWidth + charContainer.width) + x / scaleX, startY + y / scaleY, totalWidth + x / scaleX, (startY + charContainer.height) + y / scaleY, charContainer.x + charContainer.width, charContainer.y + charContainer.height, charContainer.x, charContainer.y)
          totalWidth += (charContainer.width - c)
        }
      }
    }
    GL11.glTranslatef(0, 0, -z)
    GL11.glEnable(GL12.GL_RESCALE_NORMAL)
    GL11.glEnable(GL11.GL_DEPTH_TEST)
    GL11.glPopMatrix()
  }

  override def charWidth(char: Char): Int = if (char.toInt > 255) customChars(char).width.toInt else charArray(char).width.toInt

  override def height: Int = fontHeight.toInt - 3

  override def width(lines: String*): Int = fontMetrics.stringWidth(GuiUtils.longestString(lines: _*)) + GuiUtils.longestString(lines: _*).length

  def destroy(): Unit = {
    val scratch: IntBuffer = BufferUtils.createIntBuffer(1)
    scratch.put(0, textureId)
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
    GL11.glDeleteTextures(scratch)
  }

  private def createSet(customCharsArray: Array[Char]) {
    if (customCharsArray != null && customCharsArray.length > 0) {
      textureWidth *= 2
    }
    try {
      val imgTemp = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB)
      val g = imgTemp.getGraphics.asInstanceOf[Graphics2D]
      g.setColor(new java.awt.Color(0, 0, 0, 1))
      g.fillRect(0, 0, textureWidth, textureHeight)
      var rowHeight = 0F
      var positionX = 0F
      var positionY = 0F
      val customCharsLength: Int = if (customCharsArray != null) customCharsArray.length else 0
      for (i <- 0 until 256 + customCharsLength) {
        val ch = if (i < 256) i.toChar else customCharsArray(i - 256)
        var fontImage = getFontImage(ch)
        if (positionX + fontImage.getWidth >= textureWidth) {
          positionX = 0
          positionY += rowHeight
          rowHeight = 0
        }

        val charContainer = CharContainer(positionX, positionY, fontImage.getWidth, fontImage.getHeight)

        if (charContainer.height > fontHeight) {
          fontHeight = charContainer.height
        }
        if (charContainer.height > rowHeight) {
          rowHeight = charContainer.height
        }
        g.drawImage(fontImage, positionX.asInstanceOf[Int], positionY.asInstanceOf[Int], null)
        positionX += charContainer.width
        if (i < 256) {
          charArray(i) = charContainer
        }
        else {
          customChars += ch -> charContainer
        }
        fontImage = null
      }
      ImageIO.write(imgTemp, "png", new File("texture.png"))
      textureId = loadImage(imgTemp)
    }
    catch {
      case e: Exception =>
        System.err.println("Failed to create font.")
        e.printStackTrace()
    }
  }

  private def getFontImage(ch: Char): BufferedImage = {
    val tempfontImage: BufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
    val g: Graphics2D = tempfontImage.getGraphics.asInstanceOf[Graphics2D]
    if (antiAlias) {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    }
    g.setFont(font)
    fontMetrics = g.getFontMetrics
    var charwidth: Float = fontMetrics.charWidth(ch) + 8
    if (charwidth <= 0) {
      charwidth = 7
    }
    var charheight: Float = fontMetrics.getHeight + 3
    if (charheight <= 0) {
      charheight = fontSize
    }
    var fontImage: BufferedImage = null
    fontImage = new BufferedImage(charwidth.asInstanceOf[Int], charheight.asInstanceOf[Int], BufferedImage.TYPE_INT_ARGB)
    val gt: Graphics2D = fontImage.getGraphics.asInstanceOf[Graphics2D]
    if (antiAlias) {
      gt.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    }
    gt.setFont(font)
    gt.setColor(java.awt.Color.WHITE)
    val charx: Int = 3
    val chary: Int = 1
    gt.drawString(String.valueOf(ch), charx, chary + fontMetrics.getAscent)
    fontImage
  }

  def loadImage(bufferedImage: BufferedImage): Int = {
    try {
      val width: Short = bufferedImage.getWidth.asInstanceOf[Short]
      val height: Short = bufferedImage.getHeight.asInstanceOf[Short]
      val bpp: Int = bufferedImage.getColorModel.getPixelSize.asInstanceOf[Byte]
      var byteBuffer: ByteBuffer = null
      val db: DataBuffer = bufferedImage.getData.getDataBuffer
      if (db.isInstanceOf[DataBufferInt]) {
        val intI = bufferedImage.getData.getDataBuffer.asInstanceOf[DataBufferInt].getData
        val newI = Array.ofDim[Byte](intI.length * 4)
        for (i <- 0 until intI.length) {
          val b = MathUtils.intToByteArray(intI(i))
          val newIndex: Int = i * 4
          newI(newIndex) = b(1)
          newI(newIndex + 1) = b(2)
          newI(newIndex + 2) = b(3)
          newI(newIndex + 3) = b(0)
        }
        byteBuffer = ByteBuffer.allocateDirect(width * height * (bpp / 8)).order(ByteOrder.nativeOrder).put(newI)
      }
      else {
        byteBuffer = ByteBuffer.allocateDirect(width * height * (bpp / 8)).order(ByteOrder.nativeOrder).put(bufferedImage.getData.getDataBuffer.asInstanceOf[DataBufferByte].getData)
      }
      byteBuffer.flip
      val internalFormat: Int = GL11.GL_RGBA8
      val format: Int = GL11.GL_RGBA
      val textureId: IntBuffer = BufferUtils.createIntBuffer(1)
      GL11.glGenTextures(textureId)
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId.get(0))
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP)
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP)
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
      GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE)
      GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, internalFormat, width, height, format, GL11.GL_UNSIGNED_BYTE, byteBuffer)
      return textureId.get(0)
    }
    catch {
      case e: Exception =>
        throw e
    }
    -1
  }

  private case class CharContainer(x: Float, y: Float, width: Float, height: Float)

}
