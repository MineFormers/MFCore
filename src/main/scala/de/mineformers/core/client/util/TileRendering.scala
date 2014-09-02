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

import cpw.mods.fml.client.registry.ClientRegistry
import cpw.mods.fml.relauncher.{Side, SideOnly}
import de.mineformers.core.block.TileProvider
import de.mineformers.core.client.renderer.TileRenderer
import net.minecraft.block.Block

import net.minecraft.tileentity.TileEntity

/**
 * TileRendering
 *
 * @author PaleoCrafter
 */
trait TileRendering[T <: TileEntity, R <: TileRenderer[T]] {
  this: Block with TileProvider[T] =>

  @SideOnly(Side.CLIENT)
  def createTileRenderer: R

  @SideOnly(Side.CLIENT)
  def registerRenderer(): Unit = {
    val renderer = createTileRenderer
    ClientRegistry.bindTileEntitySpecialRenderer(tileClass, renderer)
  }

  override def getRenderType: Int = -1

  override def renderAsNormalBlock(): Boolean = false

  override def isOpaqueCube: Boolean = false
}
