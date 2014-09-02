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

package de.mineformers.core.client.util

import cpw.mods.fml.relauncher.ReflectionHelper
import de.mineformers.core.util.world.{Vector2, Vector3}
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.{EntityRenderer, Tessellator}
import net.minecraft.util.Timer
import org.lwjgl.opengl.GL11

/**
 * RenderUtils
 *
 * @author PaleoCrafter
 */
object RenderUtils {
  implicit def vec3ToVertex(vec: Vector3): Vertex = Vertex(vec)

  def rotateFacing(): Unit = {
    val camera = Minecraft.getMinecraft.renderViewEntity
    val yaw = camera.prevRotationYaw + (camera.rotationYaw - camera.prevRotationYaw) * partialTicks
    val pitch = camera.prevRotationPitch + (camera.rotationPitch - camera.prevRotationPitch) * partialTicks
    val roll = prevCamRoll + (camRoll - prevCamRoll) * partialTicks
    GL11.glRotatef(-(yaw + 180), 0, 1, 0)
    GL11.glRotatef(-pitch, 1, 0, 0)
    GL11.glRotatef(-roll, 0, 0, 1)
  }

  def drawCircle(x: Double, y: Double, z: Double, radius: Double, vertices: Int, filled: Boolean = true): Unit = {
    if (!filled)
      GL11.glLineWidth(10)
    drawVertices(if (filled) GL11.GL_TRIANGLE_FAN else GL11.GL_LINE_LOOP, (0 until vertices) map {
      i =>
        val theta = i * 2 * math.Pi / vertices.toFloat
        // angle to ith vertex, in radians not degrees.
        // use i+0.5 if you want the polygon rotated like a stop sign.
        val xOff = radius * math.cos(theta)
        val zOff = radius * math.sin(theta)
        Vertex(Vector3(x + xOff, y + zOff, z))
    } reverse)
  }

  def drawQuad(x: Double, y: Double, z: Double, width: Double, length: Double): Unit = {
    drawVertices(GL11.GL_QUADS, Seq(Vertex(Vector3(x, y, z), Vector2(0, 0)),
      Vertex(Vector3(x, y, z + length), Vector2(0, 1)),
      Vertex(Vector3(x + width, y, z + length), Vector2(1, 1)),
      Vertex(Vector3(x + width, y, z), Vector2(1, 0))))
  }

  def drawVertices(mode: Int, vertices: Seq[Vertex]): Unit = {
    val tessellator: Tessellator = Tessellator.instance
    tessellator.startDrawing(mode)
    for (v <- vertices) {
      v.addToTessellator(tessellator)
    }
    tessellator.draw()
  }

  def partialTicks: Float = {
    timerField.setAccessible(true)
    timerField.get(Minecraft.getMinecraft).asInstanceOf[Timer].renderPartialTicks
  }

  def camRoll: Float = {
    rollField.setAccessible(true)
    rollField.get(Minecraft.getMinecraft.entityRenderer).asInstanceOf[Float]
  }

  def prevCamRoll: Float = {
    prevRollField.setAccessible(true)
    prevRollField.get(Minecraft.getMinecraft.entityRenderer).asInstanceOf[Float]
  }

  private val timerField = ReflectionHelper.findField(classOf[Minecraft], "timer", "field_71428_T")
  private val rollField = ReflectionHelper.findField(classOf[EntityRenderer], "camRoll", "field_78495_O")
  private val prevRollField = ReflectionHelper.findField(classOf[EntityRenderer], "prevCamRoll", "field_78505_P")
}
