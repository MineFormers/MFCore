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
package de.mineformers.core.client.ui.skin.drawable

import com.google.gson.JsonObject
import de.mineformers.core.client.shape2d.{Point, Rectangle, Size}
import de.mineformers.core.client.ui.skin.DrawableDeserializer
import de.mineformers.core.client.ui.skin.drawable.DynamicTexture.{Corner, Side}
import de.mineformers.core.client.util.RenderUtils
import de.mineformers.core.util.ResourceUtils.Resource
import de.mineformers.core.util.renderer.GuiUtils

/**
 * DynamicTexture
 *
 * @author PaleoCrafter
 */
object DynamicTexture {

  object Corner {
    final val TopLeft = 0
    final val TopRight = 1
    final val BottomRight = 2
    final val BottomLeft = 3
  }

  object Side {
    final val Top = 0
    final val Right = 1
    final val Bottom = 2
    final val Left = 3
  }

  class Deserializer extends DrawableDeserializer {
    override def deserialize(typ: String, json: JsonObject): DrawableTexture = {
      var texture: DynamicTexture = null
      if (json.has("topLeft")) {
        val tlObject = json.getAsJsonObject("topLeft")
        val tObject = json.getAsJsonObject("top")
        val iObject = json.getAsJsonObject("inner")
        texture = new DynamicTexture(getUVsFromObject(tlObject), getUVsFromObject(tObject), getUVsFromObject(iObject))
      } else if (json.has("corners")) {
        val c = json.getAsJsonObject("corners")
        val s = json.getAsJsonObject("sides")
        val inner = json.getAsJsonObject("inner")
        val uvR: JsonObject => Rectangle = getUVsFromObject
        texture = new DynamicTexture(Array(uvR(c.getAsJsonObject("topLeft")), uvR(c.getAsJsonObject("topRight")), uvR(c.getAsJsonObject("bottomLeft")), uvR(c.getAsJsonObject("bottomRight"))),
          Array(uvR(s.getAsJsonObject("top")), uvR(s.getAsJsonObject("right")), uvR(s.getAsJsonObject("bottom")), uvR(s.getAsJsonObject("left"))), getUVsFromObject(inner))
      } else {
        texture = new DynamicTexture(Rectangle(0, 0, 0, 0), Rectangle(0, 0, 0, 0), Rectangle(0, 0, 0, 0))
      }
      if (json.has("repeatSides"))
        texture.repeatSides = json.getAsJsonPrimitive("repeatSides").getAsBoolean
      if (json.has("repeatInner"))
        texture.repeatInner = json.getAsJsonPrimitive("repeatInner").getAsBoolean
      texture
    }
  }

}

class DynamicTexture(_texture: Resource, textureWidth: Int, textureHeight: Int, private var corners: Array[Rectangle], private var sides: Array[Rectangle], private var inner: Rectangle) extends DrawableTexture {
  this.texture = _texture
  this.textureSize = Size(textureWidth, textureHeight)

  def this(corners: Array[Rectangle], sides: Array[Rectangle], inner: Rectangle) = this(null, 0, 0, corners, sides, inner)

  def this(texture: Resource, textureWidth: Int = 16, textureHeight: Int = 16, topLeft: Rectangle, top: Rectangle, inner: Rectangle) = this(texture, textureWidth, textureHeight, Array(topLeft, topLeft.translate(topLeft.width + top.width, 0), topLeft.translate(topLeft.width + top.width, topLeft.height + top.width), topLeft.translate(0, topLeft.height + top.width)), Array(top, top.translate(top.width, topLeft.height).resize(top.size.invert), top.translate(0, top.width + top.height), top.translate(-topLeft.width, top.height).resize(top.size.invert)), inner)

  def this(topLeft: Rectangle, top: Rectangle, inner: Rectangle) = this(null, 0, 0, topLeft, top, inner)

  override def init(): Unit = {
    if (inner.width == 0) {
      val topLeft = Rectangle(0, 0, textureSize.width / 3, textureSize.height / 3)
      val top = Rectangle(topLeft.width, 0, textureSize.width / 3, textureSize.height / 3)
      inner = Rectangle(topLeft.width, topLeft.height, textureSize.width / 3, textureSize.height / 3)
      corners = Array(topLeft, topLeft.translate(topLeft.width + top.width, 0), topLeft.translate(topLeft.width + top.width, topLeft.height + top.width), topLeft.translate(0, topLeft.height + top.width))
      sides = Array(top, top.translate(top.width, topLeft.height).resize(top.size.invert), top.translate(0, top.width + top.height), top.translate(-topLeft.width, top.height).resize(top.size.invert))
    }
  }

  override def draw(mousePos: Point, pos: Point, z: Int): Unit = {
    utils.resetColor()
    RenderUtils.bindTexture(texture)
    for (i <- 0 until corners.length)
      drawCorner(i, pos + cornerPos(i), z)

    for (i <- 0 until sides.length)
      drawSide(i, pos + sidePos(i), i % 2 != 0, z)

    drawInner(pos +(corners(Corner.TopLeft).width, corners(Corner.TopLeft).height), z)
  }

  def drawCorner(cornerId: Int, pos: Point, z: Int): Unit = {
    val corner = corners(cornerId)
    GuiUtils.drawQuad(pos.x, pos.y, z, corner.width, corner.height, scaleU(corner.x), scaleV(corner.y), scaleU(corner.end.x), scaleV(corner.end.y))
  }

  def drawSide(sideId: Int, pos: Point, vertical: Boolean, z: Int): Unit = {
    val side = sides(sideId)
    val width = if (vertical) side.width else size.width - corners(Corner.TopLeft).width - corners(Corner.TopRight).width
    val height = if (vertical) size.height - corners(Corner.TopLeft).height - corners(Corner.BottomLeft).height else side.height
    if (repeatSides) {
      if (vertical)
        GuiUtils.drawRectangleYRepeated(pos.x, pos.y, z, width, height, scaleU(side.x), scaleV(side.y), scaleU(side.end.x), scaleV(side.end.y), side.height)
      else
        GuiUtils.drawRectangleXRepeated(pos.x, pos.y, z, width, height, scaleU(side.x), scaleV(side.y), scaleU(side.end.x), scaleV(side.end.y), side.width)
    } else
      GuiUtils.drawQuad(pos.x, pos.y, z, width, height, scaleU(side.x), scaleV(side.y), scaleU(side.end.x), scaleV(side.end.y))
  }

  def drawInner(pos: Point, z: Int): Unit = {
    val width = size.width - corners(Corner.TopLeft).width - corners(Corner.TopRight).width
    val height = size.height - corners(Corner.TopLeft).height - corners(Corner.BottomLeft).height
    if (repeatInner)
      GuiUtils.drawRectangleRepeated(pos.x, pos.y, z, width, height, scaleU(inner.x), scaleV(inner.y), scaleU(inner.end.x), scaleV(inner.end.y), inner.width, inner.height)
    else
      GuiUtils.drawQuad(pos.x, pos.y, z, width, height, scaleU(inner.x), scaleV(inner.y), scaleU(inner.end.x), scaleV(inner.end.y))
  }

  def cornerPos(corner: Int): Point = corner match {
    case Corner.TopLeft => Point(0, 0)
    case Corner.TopRight => Point(size.width - corners(Corner.TopRight).width, 0)
    case Corner.BottomRight => Point(size.width - corners(Corner.BottomRight).width, corners(Corner.TopLeft).height + innerHeight)
    case Corner.BottomLeft => Point(0, corners(Corner.TopLeft).height + innerHeight)
    case _ => Point(0, 0)
  }

  def sidePos(side: Int): Point = side match {
    case Side.Top => Point(corners(Corner.TopLeft).width, 0)
    case Side.Right => cornerPos(Corner.TopRight) +(0, corners(Corner.TopRight).height)
    case Side.Bottom => cornerPos(Corner.BottomLeft) +(corners(Corner.BottomLeft).width, 0)
    case Side.Left => Point(0, corners(Corner.TopLeft).height)
    case _ => Point(0, 0)
  }

  def innerWidth = size.width - corners(Corner.TopLeft).width - corners(Corner.TopRight).width

  def innerHeight = size.height - corners(Corner.TopLeft).height - corners(Corner.BottomLeft).height

  def scaleU(u: Int): Float = scaleU * u

  def scaleV(v: Int): Float = scaleV * v

  override def toString: String = s"$corners,$sides,$inner"

  var repeatSides = true
  var repeatInner = true
  lazy val scaleU = 1F / textureSize.width
  lazy val scaleV = 1F / textureSize.height
}
