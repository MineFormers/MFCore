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

import cpw.mods.fml.client.registry.{ClientRegistry, ISimpleBlockRenderingHandler, RenderingRegistry}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import de.mineformers.core.client.renderer.TileRenderer
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.client.{MinecraftForgeClient, IItemRenderer}

/**
 * RenderingProxy
 *
 * @author PaleoCrafter
 */
trait RenderingProxy {
  @SideOnly(Side.CLIENT)
  def createTileRenderer: TileRenderer[_] = null

  @SideOnly(Side.CLIENT)
  def createItemRenderer: IItemRenderer = null

  @SideOnly(Side.CLIENT)
  def createSimpleRenderer: ISimpleBlockRenderingHandler = null

  @SideOnly(Side.CLIENT)
  def registerRenderers(block: Block with Rendering, tileClass: Class[_ <: TileEntity]): Unit = {
    val itemRenderer = createItemRenderer
    if (itemRenderer != null)
      MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(block), itemRenderer)
    val tileRenderer = createTileRenderer
    if (tileRenderer != null && tileClass != null)
      ClientRegistry.bindTileEntitySpecialRenderer(tileClass, tileRenderer)
    val simpleRenderer = createSimpleRenderer
    if (simpleRenderer != null) {
      block.renderType = simpleRenderer.getRenderId
      RenderingRegistry.registerBlockHandler(simpleRenderer.getRenderId, simpleRenderer)
    }
  }
}
