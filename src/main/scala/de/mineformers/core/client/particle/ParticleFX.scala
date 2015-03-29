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
package de.mineformers.core.client.particle

import java.lang.reflect.Field

import de.mineformers.core.client.particle.updater.ParticleUpdater
import de.mineformers.core.util.math.Vector3
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.{EffectRenderer, EntityFX}
import net.minecraft.client.renderer.{Tessellator, WorldRenderer}
import net.minecraft.command.CommandResultStats.Type
import net.minecraft.command.ICommandSender
import net.minecraft.entity.Entity
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World

/**
 * ParticleFX
 *
 * @author PaleoCrafter
 */
class ParticleFX(world: World, startPos: Vector3, updater: ParticleUpdater, val lifespan: Int, _texture: String) extends EntityFX(world, startPos.x, startPos.y, startPos.z) with ICommandSender {
  this.motionX = 0
  this.motionY = 0
  this.motionZ = 0

  override def onUpdate(): Unit = {
    updater.update(this)
    this.prevPosX = this.posX
    this.prevPosY = this.posY
    this.prevPosZ = this.posZ
    if (this.ticksExisted + 1 >= lifespan) {
      this.setDead()
    }
  }

  override def renderParticle(renderer: WorldRenderer, entity: Entity, partialTicks: Float, arX: Float, arXZ: Float, arZ: Float, arYZ: Float, arXY: Float): Unit = {
    Tessellator.getInstance().draw()
    updater.render(renderer, partialTicks, arX, arXZ, arZ, arYZ, arXY, this)
    try {
      val particleTextures: Field = classOf[EffectRenderer].getField("particleTextures")
      particleTextures.setAccessible(true)
      Minecraft.getMinecraft.renderEngine.bindTexture(particleTextures.get(null).asInstanceOf[ResourceLocation])
    }
    catch {
      case e: Exception =>
        throw new RuntimeException("Error during particle texturing!", e)
    }
    renderer.startDrawingQuads()
  }

  def pos = Vector3(posX, posY, posZ)

  def setColor(r: Int, g: Int, b: Int) {
    this.setColor(r, g, b, 1F)
  }

  def setColor(r: Int, g: Int, b: Int, alpha: Int) {
    this.setColor(r, g, b, (1F / 255) * alpha)
  }

  def setColor(r: Int, g: Int, b: Int, alpha: Float) {
    this.setColor(((r << 16) & 0xFF0000) | ((g << 8) & 0x00FF00) | (b & 0x0000FF), alpha)
  }

  def setColor(hexCode: Int) {
    this.setColor(hexCode, 1F)
  }

  def setColor(hexCode: Int, alpha: Int) {
    this.setColor(hexCode, (1F / 255f) * alpha)
  }

  def setColor(hexCode: Int, alpha: Float) {
    this.color = hexCode
    this.particleAlpha = alpha
  }

  def r_=(r: Int): Unit = {
    color &= 0x00FFFF
    color |= (r << 16) & 0xFF0000
  }

  def g_=(g: Int): Unit = {
    color &= 0xFF00FF
    color |= (g << 8) & 0x00FF00
  }

  def b_=(b: Int): Unit = {
    color &= 0xFFFF00
    color |= b & 0x0000FF
  }

  def r: Int = (color & 0xFF0000) >> 16

  def g: Int = (color & 0x00FF00) >> 8

  def b: Int = color & 0x0000FF

  def alpha: Int = (particleAlpha * 255).asInstanceOf[Int]

  def start: Vector3 = startPos

  def texture: String = _texture

  def resource = _resource

  var scale: Float = 1
  private val _resource = new ResourceLocation(texture)
  private var color: Int = 0
}