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

package de.mineformers.core.client.renderer

import cpw.mods.fml.relauncher.ReflectionHelper
import de.mineformers.core.client.util.MultiPass
import net.minecraft.block.Block
import net.minecraft.client.renderer.{RenderHelper, Tessellator, RenderBlocks}
import net.minecraft.util.IIcon
import net.minecraft.world.IBlockAccess
import net.minecraftforge.client.model.obj.WavefrontObject
import org.lwjgl.opengl.GL11

/**
 * SimpleObjRenderer
 *
 * @author PaleoCrafter
 */
class SimpleObjRenderer(model: WavefrontObject, groups: Map[Int, Seq[String]] = Map.empty[Int, Seq[String]]) extends SimpleBlockRenderer {
  private val tes = Tessellator.instance

  override def renderInventoryBlock(block: Block, metadata: Int, modelId: Int, renderer: RenderBlocks): Unit = {
    RenderHelper.disableStandardItemLighting()
    GL11.glEnable(GL11.GL_BLEND)
    tes.startDrawing(GL11.GL_TRIANGLES)
    tes.addTranslation(0, 0, 1)
    tes.setColorOpaque_F(1, 1, 1)
    renderWithIcon(model, block.getIcon(0, metadata))
    tes.addTranslation(0, 0, -1)
    tes.draw()
    GL11.glDisable(GL11.GL_BLEND)
    RenderHelper.enableStandardItemLighting()
  }

  override def renderWorldBlock(world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, modelId: Int, renderer: RenderBlocks): Boolean = {
    tes.startDrawing(GL11.GL_TRIANGLES)
    tes.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z))
    tes.setColorOpaque_F(1, 1, 1)
    tes.addTranslation(x, y, z + 1)
    val pass = block match {
      case m: MultiPass => m.renderPass
      case _ => 0
    }
    renderWithIcon(model, block.getIcon(0, world.getBlockMetadata(x, y, z)), groupsByPass(pass): _*)
    tes.addTranslation(-x, -y, -z - 1)
    tes.draw()
    tes.startDrawingQuads()
    false
  }

  def groupsByPass(pass: Int): Seq[String] = groups.getOrElse(pass, Seq.empty[String])

  def renderWithIcon(model: WavefrontObject, icon: IIcon, groups: String*): Unit = {
    import scala.collection.JavaConversions._
    for (go <- model.groupObjects) {
      if (groups.length == 0 || groups.contains(go.name))
        for (f <- go.faces) {
          val n = f.faceNormal
          tes.setNormal(n.x, n.y, n.z)
          for (i <- 0 until f.vertices.length) {
            val v = f.vertices(i)
            val t = f.textureCoordinates(i)
            tes.addVertexWithUV(v.x, v.y, v.z,
              icon.getInterpolatedU(t.u * 16),
              icon.getInterpolatedV(t.v * 16))
          }
        }
    }
  }
}
