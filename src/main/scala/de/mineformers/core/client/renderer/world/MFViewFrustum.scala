package de.mineformers.core.client.renderer.world

import de.mineformers.core.util.world.BlockPos
import net.minecraft.client.renderer.ViewFrustum
import net.minecraft.client.renderer.chunk.IRenderChunkFactory
import net.minecraft.world.World

/**
 * MFViewFrustum
 *
 * @author PaleoCrafter
 */
class MFViewFrustum(world: World, renderDistance: Int, chunkFactory: IRenderChunkFactory) extends ViewFrustum(world, renderDistance, null, chunkFactory) {
  def apply(pos: BlockPos) = this.getRenderChunk(pos)
}
