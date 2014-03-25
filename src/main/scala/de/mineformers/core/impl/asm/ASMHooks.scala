/*******************************************************************************
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
 ******************************************************************************/
package de.mineformers.core.impl.asm

import net.minecraft.block.Block
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.world.IBlockAccess
import cpw.mods.fml.common.FMLCommonHandler
import de.mineformers.core.event.RenderBlockEvent

/**
 * ASMHooks
 *
 * @author PaleoCrafter
 */
object ASMHooks {

  /**
   * Called before the switch statement in RenderBlocks#renderBlockByRenderType.
   * Posts a [[RenderBlockEvent.Pre]] to the FML bus
   * @param renderer the [[RenderBlocks]] instance used for this block
   * @param world the [[IBlockAccess]] this block is in
   * @param x the x position of the block rendered
   * @param y the y position of the block rendered
   * @param z the z position of the block rendered
   * @param block the [[Block]] instance of the block rendered
   * @return true if the posted event is cancelled (prevents the block from being rendered)
   */
  def onRenderByTypePre(renderer: RenderBlocks, world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, renderPass: Int): Boolean =
    FMLCommonHandler.instance().bus().post(new RenderBlockEvent.Pre(world, x, y, z, block, block.getRenderType, renderPass, renderer))

  /**
   * Called after RenderBlocks#renderBlockByRenderType in WorldRenderer#updateRenderer.
   * Posts a [[RenderBlockEvent.Post]] to the FML bus
   * @param renderer the [[RenderBlocks]] instance used for this block
   * @param world the [[IBlockAccess]] this block is in
   * @param x the x position of the block rendered
   * @param y the y position of the block rendered
   * @param z the z position of the block rendered
   * @param block the [[Block]] instance of the block rendered
   */
  def onRenderByTypePost(renderer: RenderBlocks, world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, renderPass: Int): Unit =
    FMLCommonHandler.instance().bus().post(new RenderBlockEvent.Post(world, x, y, z, block, block.getRenderType, renderPass, renderer))

}
