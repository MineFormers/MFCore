package de.mineformers.core.client.renderer

import de.mineformers.core.client.renderer.StructureChunkRenderer._
import de.mineformers.core.structure.StructureWorld
import de.mineformers.core.util.Log
import de.mineformers.core.util.world.BlockPos
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.client.renderer.{OpenGlHelper, Tessellator}
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.client.ForgeHooksClient
import org.lwjgl.opengl.GL11

/**
 * StructureChunkRenderer
 *
 * @author PaleoCrafter
 */
class StructureChunkRenderer(val structure: StructureWorld, baseX: Int, baseY: Int, baseZ: Int) {
  val bounds = AxisAlignedBB.fromBounds(baseX * ChunkWidth, baseY * ChunkHeight, baseZ * ChunkLength, (baseX + 1) * ChunkWidth, (baseY + 1) * ChunkHeight, (baseZ + 1) * ChunkLength)
  val centered = BlockPos(((baseX + 0.5) * ChunkWidth).toInt, ((baseY + 0.5) * ChunkHeight).toInt, ((baseZ + 0.5) * ChunkLength).toInt)
  val glList = GL11.glGenLists(2)
  val mc = Minecraft.getMinecraft
  var tiles = (for ((pos, tile) <- structure.tiles if pos.containedBy(bounds)) yield tile).toList
  var update = true

  def dispose(): Unit = {
    tiles = null
    GL11.glDeleteLists(glList, 2)
  }

  def render(pass: Int): Unit = {
    this.mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture)
    GL11.glColor4f(1F, 1F, 1F, 1F)
    this.updateList()
    GL11.glCallList(glList + pass)

    renderTiles(pass)

    GL11.glEnable(GL11.GL_BLEND)
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
  }

  def updateList(): Unit = {
    if (update) {
      this.update = false
      val pos = structure.pos
      val minX = bounds.minX.toInt
      val maxX = bounds.maxX.toInt min structure.width
      val minY = bounds.minY.toInt
      val maxY = bounds.maxY.toInt min structure.height
      val minZ = bounds.minZ.toInt
      val maxZ = bounds.maxZ.toInt min structure.length
      this.mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture)
      for (pass <- 0 until 2) {
        GL11.glNewList(glList + pass, GL11.GL_COMPILE)
        //val renderBlocks = structure.localWorldAccess.getRenderBlocks
        val ambient = mc.gameSettings.ambientOcclusion
        mc.gameSettings.ambientOcclusion = 0

        Tessellator.getInstance().getWorldRenderer.startDrawingQuads()
        for (y <- minY until maxY; x <- minX until maxX; z <- minZ until maxZ) {
          try {
            val block = structure.getBlockState(BlockPos(x, y, z))
            if (mc.theWorld.isAirBlock(pos + BlockPos(x, y, z)) || mc.theWorld.getBlockState(pos + BlockPos(x, y, z)) == null)
              if (block != null && true) { // Can render in pass
                //renderBlocks.renderBlockByRenderType(block, x, y, z)
              }
          } catch {
            case e: Exception =>
              Log.error("Failed to render block", e)
          }
        }
        Tessellator.getInstance().draw()

        mc.gameSettings.ambientOcclusion = ambient
        GL11.glEndList()
      }
    }
  }

  def renderTiles(pass: Int): Unit = {
    ForgeHooksClient.setRenderPass(pass)
    try {
      for (tile <- tiles if tile.shouldRenderInPass(pass)) {
        val x = tile.getPos.getX
        val y = tile.getPos.getY
        val z = tile.getPos.getZ
        val pos = structure.pos
        val renderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(tile)
        if (mc.theWorld.isAirBlock(pos + BlockPos(x, y, z)) || mc.theWorld.getBlockState(pos + BlockPos(x, y, z)) == null)
          if (renderer != null) {
            try {
              renderer.renderTileEntityAt(tile, x, y, z, 0, -1)
              OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit)
              GL11.glDisable(GL11.GL_TEXTURE_2D)
              OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            } catch {
              case e: Exception =>
                Log.error("Failed to render a tile entity!", e)
            }
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1F)
          }
      }
    } catch {
      case e: Exception =>
        Log.error("Failed to render tiles", e)
    }
    ForgeHooksClient.setRenderPass(-1)
  }
}

object StructureChunkRenderer {
  final val ChunkWidth = 16
  final val ChunkHeight = 16
  final val ChunkLength = 16
}
