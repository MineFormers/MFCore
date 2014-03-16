package de.mineformers.core.impl.asm

import net.minecraft.block.Block
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.world.IBlockAccess
import cpw.mods.fml.common.FMLCommonHandler
import de.mineformers.core.event.RenderBlockEvent

object ASMHooks {

  def onRenderByTypePre(renderer: RenderBlocks, world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, modelId: Int): Boolean = {
    FMLCommonHandler.instance().bus().post(new RenderBlockEvent.Pre(world, x, y, z, block, modelId, renderer))
  }

  def onRenderByTypePost(renderer: RenderBlocks, world: IBlockAccess, x: Int, y: Int, z: Int, block: Block): Unit =
    FMLCommonHandler.instance().bus().post(new RenderBlockEvent.Post(world, x, y, z, block, block.getRenderType, renderer))

}
