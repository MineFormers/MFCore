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
package de.mineformers.core.client.renderer.shape

import de.mineformers.core.client.util.Color
import de.mineformers.core.util.math.{Vector2, Vector3}
import net.minecraft.client.renderer.WorldRenderer

/**
 * Vertex
 *
 * @author PaleoCrafter
 */
object Vertex {
  def apply(pos: Vector3): Vertex = this(pos, null, null)

  def apply(pos: Vector3, uv: Vector2): Vertex = this(pos, uv, null)
}

case class Vertex(pos: Vector3, uv: Vector2, color: Color) {
  def x = pos.x

  def y = pos.y

  def z = pos.z

  def u = if (hasUV) uv.x else -1D

  def v = if (hasUV) uv.y else -1D

  def hasUV = uv != null

  def isColored = color != null

  def addToTessellator(renderer: WorldRenderer): Unit = {
    if (isColored)
      renderer.setColorRGBA_F(color.r, color.g, color.b, color.a)
    if (hasUV)
      renderer.addVertexWithUV(x, y, z, u, v)
    else
      renderer.addVertex(x, y, z)
  }
}