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
package de.mineformers.core.client.particle.updater

import de.mineformers.core.client.particle.ParticleFX
import de.mineformers.core.client.util.RenderUtils
import net.minecraft.client.particle.EntityFX
import net.minecraft.client.renderer.Tessellator
import org.lwjgl.opengl.GL11

/**
 * ParticleUpdater
 *
 * @author PaleoCrafter
 */
trait ParticleUpdater {
  def update(particle: ParticleFX): Unit

  def render(tessellator: Tessellator, partialTicks: Float, arX: Float, arXZ: Float, arZ: Float, arYZ: Float, arXY: Float, fx: ParticleFX) {
    GL11.glPushMatrix()

    GL11.glDepthMask(false)
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, 1)

    RenderUtils.bindTexture(fx.resource)
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.75F)

    val minU = 0F
    val maxU = 1F
    val minV = 0.0F
    val maxV = 1.0F

    val x = (fx.prevPosX + (fx.posX - fx.prevPosX) * partialTicks - EntityFX.interpPosX).toFloat
    val y = (fx.prevPosY + (fx.posY - fx.prevPosY) * partialTicks - EntityFX.interpPosY).toFloat
    val z = (fx.prevPosZ + (fx.posZ - fx.prevPosZ) * partialTicks - EntityFX.interpPosZ).toFloat

    tessellator.startDrawingQuads()
    tessellator.setBrightness(240)

    tessellator.setColorRGBA(fx.r, fx.g, fx.b, fx.alpha)
    tessellator.addVertexWithUV(x - arX * fx.scale - arYZ * fx.scale, y
      - arXZ * fx.scale, z - arZ * fx.scale - arXY * fx.scale, maxU, maxV)
    tessellator.addVertexWithUV(x - arX * fx.scale + arYZ * fx.scale, y
      + arXZ * fx.scale, z - arZ * fx.scale + arXY * fx.scale, maxU, minV)
    tessellator.addVertexWithUV(x + arX * fx.scale + arYZ * fx.scale, y
      + arXZ * fx.scale, z + arZ * fx.scale + arXY * fx.scale, minU, minV)
    tessellator.addVertexWithUV(x + arX * fx.scale - arYZ * fx.scale, y
      - arXZ * fx.scale, z + arZ * fx.scale - arXY * fx.scale, minU, maxV)

    tessellator.draw()

    GL11.glDisable(GL11.GL_BLEND)
    GL11.glDepthMask(true)

    GL11.glPopMatrix()
  }
}
