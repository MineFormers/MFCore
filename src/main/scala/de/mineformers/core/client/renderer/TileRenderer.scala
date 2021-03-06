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

import de.mineformers.core.util.math.Vector3
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import org.lwjgl.opengl.GL11

/**
 * TileRenderer
 *
 * @author PaleoCrafter
 */
abstract class TileRenderer[T <: TileEntity] extends TileEntitySpecialRenderer {
  protected val renderer = new Renderer

  def render(tile: T, x: Double, y: Double, z: Double, partialTicks: Float)

  override def renderTileEntityAt(tile: TileEntity, x: Double, y: Double, z: Double, partialTicks: Float, destroyProgress: Int): Unit = {
    renderer.context.pos = Vector3(x, y, z)
    renderer.context.block = RenderContext.Block(tile.getWorld, tile.getPos.getX, tile.getPos.getY, tile.getPos.getZ)
    GL11.glPushMatrix()
    GL11.glTranslated(x, y, z)
    render(tile.asInstanceOf[T], x, y, z, partialTicks)
    GL11.glPopMatrix()
  }
}
