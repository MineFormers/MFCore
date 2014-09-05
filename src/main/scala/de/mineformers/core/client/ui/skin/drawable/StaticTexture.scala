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

import de.mineformers.core.client.util.RenderUtils
import de.mineformers.core.util.ResourceUtils.Resource
import de.mineformers.core.client.shape2d.{Rectangle, Size, Point}
import de.mineformers.core.client.ui.skin.DrawableDeserializer
import com.google.gson.JsonObject

/**
 * StaticTexture
 *
 * @author PaleoCrafter
 */
object StaticTexture {

  class Deserializer extends DrawableDeserializer {
    override def deserialize(typ: String, json: JsonObject): DrawableTexture = new StaticTexture(null, 0, 0, if (json.has("uv")) getUVsFromObject(json.getAsJsonObject("uv")) else null)
  }

}

class StaticTexture(_texture: Resource, var textureWidth: Int, var textureHeight: Int, var uv: Rectangle) extends DrawableTexture {
  texture = _texture
  textureSize = Size(textureWidth, textureHeight)

  override def draw(mousePos: Point, pos: Point, z: Int): Unit = {
    utils.resetColor()
    RenderUtils.bindTexture(texture)
    if (uv != null)
      utils.drawQuad(pos.x, pos.y, z, size.width, size.height, scaledU(uv.start), scaledV(uv.start), scaledU(uv.end), scaledV(uv.end))
    else
      utils.drawQuad(pos.x, pos.y, z, size.width, size.height, 0, 0, 1, 1)
  }

  def scaledU(p: Point): Float = p.x.toFloat / textureSize.width.toFloat

  def scaledV(p: Point): Float = p.y.toFloat / textureSize.height.toFloat
}
