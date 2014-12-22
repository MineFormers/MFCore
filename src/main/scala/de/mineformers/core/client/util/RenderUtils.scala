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

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.ReflectionHelper
import de.mineformers.core.client.renderer.shape.Vertex
import de.mineformers.core.util.world.{Vector2, Vector3}
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.{EffectRenderer, EntityFX}
import net.minecraft.client.renderer.{EntityRenderer, OpenGlHelper, RenderHelper, Tessellator}
import net.minecraft.entity.Entity
import net.minecraft.util.{MathHelper, ResourceLocation, Timer}
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11._

import scala.util.Random

/**
 * RenderUtils
 *
 * @author PaleoCrafter
 */
object RenderUtils {
  def mc: Minecraft = FMLClientHandler.instance.getClient

  def bindTexture(path: ResourceLocation): Unit = {
    mc.getTextureManager.bindTexture(path)
  }

  def applyColor(color: Color): Unit = {
    GL11.glColor4f(color.r, color.g, color.b, color.a)
  }

  implicit def vec3ToVertex(vec: Vector3): Vertex = Vertex(vec)

  def rotate(vec: Vector3): Unit = {
    glRotated(vec.x, 1, 0, 0)
    glRotated(vec.y, 0, 1, 0)
    glRotated(vec.z, 0, 0, 1)
  }

  def rotateFacing(entity: Entity = Minecraft.getMinecraft.renderViewEntity, axis: Vector3 = Vector3(1, 1, 1)): Unit = {
    val yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks
    val pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks
    val roll = prevCamRoll + (camRoll - prevCamRoll) * partialTicks
    glRotated(-(yaw + 180) * axis.y, 0, 1, 0)
    glRotated(-pitch * axis.x, 1, 0, 0)
    glRotated(-roll * axis.z, 0, 0, 1)
  }

  def drawCircle(x: Double, y: Double, z: Double, radius: Double, vertices: Int, filled: Boolean = true): Unit = {
    if (!filled)
      glLineWidth(10)
    drawVertices(if (filled) GL_TRIANGLE_FAN else GL_LINE_LOOP, (0 until vertices) map {
      i =>
        val theta = i * 2 * math.Pi / vertices.toFloat
        // angle to ith vertex, in radians not degrees.
        // use i+0.5 if you want the polygon rotated like a stop sign.
        val xOff = radius * math.cos(theta)
        val zOff = radius * math.sin(theta)
        Vertex(Vector3(x + xOff, y + zOff, z))
    } reverse)
  }

  def createCircle(x: Double, y: Double, z: Double, radius: Double, vertices: Int) = (0 until vertices) map {
    i =>
      val theta = i * 2 * math.Pi / vertices.toFloat
      // angle to ith vertex, in radians not degrees.
      // use i+0.5 if you want the polygon rotated like a stop sign.
      val xOff = radius * math.cos(theta)
      val zOff = radius * math.sin(theta)
      Vertex(Vector3(x + xOff, y + zOff, z))
  } reverse

  import net.minecraftforge.common.util.ForgeDirection._

  def drawCuboid(x: Double, y: Double, z: Double, width: Double, height: Double, length: Double, uvs: Map[ForgeDirection, (Vector2, Vector2)] = Map.empty[ForgeDirection, (Vector2, Vector2)]): Unit = {
    val defaultUVs = (Vector2(0, 0), Vector2(1, 1))
    val upUVs = uvs.getOrElse(UP, defaultUVs)
    val downUVs = uvs.getOrElse(DOWN, defaultUVs)
    val eastUVs = uvs.getOrElse(EAST, defaultUVs)
    val westUVs = uvs.getOrElse(WEST, defaultUVs)
    val southUVs = uvs.getOrElse(SOUTH, defaultUVs)
    val northUVs = uvs.getOrElse(NORTH, defaultUVs)
    drawQuad(x, y + height, z, width, length, UP, upUVs._1.x, upUVs._1.y, upUVs._2.x, upUVs._2.y)
    drawQuad(x, y, z, width, length, DOWN, downUVs._1.x, downUVs._1.y, downUVs._2.x, downUVs._2.y)
    drawQuad(x + width, y, z, width, height, EAST, eastUVs._1.x, eastUVs._1.y, eastUVs._2.x, eastUVs._2.y)
    drawQuad(x, y, z, width, height, WEST, westUVs._1.x, westUVs._1.y, westUVs._2.x, westUVs._2.y)
    drawQuad(x, y, z + length, length, height, SOUTH, southUVs._1.x, southUVs._1.y, southUVs._2.x, southUVs._2.y)
    drawQuad(x, y, z, length, height, NORTH, northUVs._1.x, northUVs._1.y, northUVs._2.x, northUVs._2.y)
  }

  def drawQuad(x: Double, y: Double, z: Double, width: Double, length: Double, facing: ForgeDirection, uMin: Double = 0, vMin: Double = 0, uMax: Double = 1, vMax: Double = 1): Unit = {
    val vertices = facing match {
      case UP =>
        Seq(Vertex(Vector3(x, y, z), Vector2(uMin, vMin)),
          Vertex(Vector3(x, y, z + length), Vector2(uMin, vMax)),
          Vertex(Vector3(x + width, y, z + length), Vector2(uMax, vMax)),
          Vertex(Vector3(x + width, y, z), Vector2(uMax, vMin)))
      case EAST =>
        Seq(Vertex(Vector3(x, y + length, z), Vector2(uMin, vMin)),
          Vertex(Vector3(x, y, z), Vector2(uMin, vMax)),
          Vertex(Vector3(x, y, z + width), Vector2(uMax, vMax)),
          Vertex(Vector3(x, y + length, z + width), Vector2(uMax, vMin))).reverse
      case WEST =>
        Seq(Vertex(Vector3(x, y + length, z), Vector2(uMin, vMin)),
          Vertex(Vector3(x, y, z), Vector2(uMin, vMax)),
          Vertex(Vector3(x, y, z + width), Vector2(uMax, vMax)),
          Vertex(Vector3(x, y + length, z + width), Vector2(uMax, vMin)))
      case SOUTH =>
        Seq(Vertex(Vector3(x, y + length, z), Vector2(uMin, vMin)),
          Vertex(Vector3(x, y, z), Vector2(uMin, vMax)),
          Vertex(Vector3(x + width, y, z), Vector2(uMax, vMax)),
          Vertex(Vector3(x + width, y + length, z), Vector2(uMax, vMin)))
      case NORTH =>
        Seq(Vertex(Vector3(x, y + length, z), Vector2(uMin, vMin)),
          Vertex(Vector3(x, y, z), Vector2(uMin, vMax)),
          Vertex(Vector3(x + width, y, z), Vector2(uMax, vMax)),
          Vertex(Vector3(x + width, y + length, z), Vector2(uMax, vMin))).reverse
      case _ =>
        Seq(Vertex(Vector3(x, y, z), Vector2(uMin, vMin)),
          Vertex(Vector3(x, y, z + length), Vector2(uMin, vMax)),
          Vertex(Vector3(x + width, y, z + length), Vector2(uMax, vMax)),
          Vertex(Vector3(x + width, y, z), Vector2(uMax, vMin))).reverse
    }
    drawVertices(GL11.GL_QUADS, vertices)
  }

  def drawVertices(mode: Int, vertices: Seq[Vertex]): Unit = {
    val tessellator: Tessellator = Tessellator.instance
    tessellator.startDrawing(mode)
    for (v <- vertices) {
      v.addToTessellator(tessellator)
    }
    tessellator.draw()
  }

  final val beamTexture = new ResourceLocation("textures/entity/beacon_beam.png")

  def drawBeaconBeam(x: Double, y: Double, z: Double): Unit = {
    // GL11.glDisable(GL11.GL_TEXTURE_2D)
    val heightFactor = 1F
    glPushMatrix()
    this.bindTexture(beamTexture)
    glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F)
    glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0F)
    glTranslatef(0.5F, 0.5F, 0.5F)
    glRotated(2880.0 * (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL, 0, 1, 0)
    glTranslatef(-0.33F / 2, 0F, -0.33F / 2)
    glDisable(GL11.GL_LIGHTING)
    glDisable(GL11.GL_BLEND)
    glDisable(GL11.GL_CULL_FACE)
    glDepthMask(true)
    OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO)
    glColor4f(1F, 1, 0.2F, 0.125F)
    val width = 0.33
    val f2 = mc.theWorld.getTotalWorldTime + partialTicks
    val f3 = -f2 * 0.2F - MathHelper.floor_float(-f2 * 0.1F)
    val vMin = -1 + f3
    val faces = (Vector2(0, (256.0F * heightFactor) * (0.5D / 0.2) + vMin), Vector2(1, vMin))
    drawCuboid(x, y, z, width, 256, width, Map(EAST -> faces, WEST -> faces, NORTH -> faces, SOUTH -> faces))
    glEnable(GL11.GL_BLEND)
    OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
    glEnable(GL11.GL_CULL_FACE)
    glDepthMask(false)
    glTranslatef(0.33F / 2, 0F, 0.33F / 2)
    glTranslatef(-0.3F, 0F, -0.3F)
    drawCuboid(x, y, z, 0.6, 256, 0.6, Map(EAST -> faces, WEST -> faces, NORTH -> faces, SOUTH -> faces))
    glDepthMask(true)
    glEnable(GL11.GL_LIGHTING)
    glPopMatrix()
  }

  def drawBeams(speed: Float, colorGenerator: Random => Color = random => Color.fromHSV(1, 0, random.nextFloat()), random: Random = new Random(432L)): Unit = {
    glShadeModel(GL11.GL_SMOOTH)
    glEnable(GL11.GL_BLEND)
    glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    glDisable(GL11.GL_ALPHA_TEST)
    glEnable(GL11.GL_CULL_FACE)
    glDepthMask(false)
    RenderHelper.disableStandardItemLighting()
    val step: Float = (Minecraft.getSystemTime % 720000L) / 36000.0F

    // Beams rendering
    glPushMatrix()
    glRotated(720.0 * (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL * speed, 0, 1, 0)
    val tessellator = Tessellator.instance
    val scale = 1 / 150F
    for (i <- 0 until 12) {
      glRotatef(random.nextFloat * 360.0F, 1.0F, 0.0F, 0.0F)
      glRotatef(random.nextFloat * 360.0F, 0.0F, 1.0F, 0.0F)
      glRotatef(random.nextFloat * 360.0F, 0.0F, 0.0F, 1.0F)
      glRotatef(random.nextFloat * 360.0F, 1.0F, 0.0F, 0.0F)
      glRotatef(random.nextFloat * 360.0F, 0.0F, 1.0F, 0.0F)
      glRotatef(random.nextFloat * 360.0F, 0.0F, 0.0F, 1.0F)
      glRotatef(step * 2 * 90.0F, 0, 0, 1)
      tessellator.startDrawing(GL_TRIANGLE_FAN)
      val length = (360 - random.nextInt(20) + 10) * scale
      val horOff = (80 - random.nextInt(10) + 5) * scale

      val color = colorGenerator(random).integer
      tessellator.setColorRGBA_I(color, 127)
      tessellator.addVertex(0.0D, 0.0D, 0.0D)
      tessellator.setColorRGBA_I(color, 0)
      tessellator.addVertex(-0.866D * horOff, length, -0.5F * horOff)
      tessellator.addVertex(0.866D * horOff, length, -0.5F * horOff)
      tessellator.addVertex(0.0D, length, 1.0F * horOff)
      tessellator.addVertex(-0.866D * horOff, length, -0.5F * horOff)
      tessellator.draw()
    }
    glPopMatrix()

    glDepthMask(true)
    glDisable(GL11.GL_CULL_FACE)
    glDisable(GL11.GL_BLEND)
    glShadeModel(GL11.GL_FLAT)
    glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    RenderHelper.enableStandardItemLighting()
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

  def particles: Array[Iterable[EntityFX]] = {
    particlesField.setAccessible(true)
    import scala.collection.JavaConversions.collectionAsScalaIterable
    particlesField.get(Minecraft.getMinecraft.effectRenderer).asInstanceOf[Array[java.util.List[EntityFX]]] map collectionAsScalaIterable
  }

  private val particlesField = ReflectionHelper.findField(classOf[EffectRenderer], "fxLayers", "field_78876_b")
  private val timerField = ReflectionHelper.findField(classOf[Minecraft], "timer", "field_71428_T")
  private val rollField = ReflectionHelper.findField(classOf[EntityRenderer], "camRoll", "field_78495_O")
  private val prevRollField = ReflectionHelper.findField(classOf[EntityRenderer], "prevCamRoll", "field_78505_P")
}
