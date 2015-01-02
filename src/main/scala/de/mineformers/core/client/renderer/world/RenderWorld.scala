package de.mineformers.core.client.renderer.world

import de.mineformers.core.client.util.RenderUtils
import de.mineformers.core.util.Implicits.VBlockPos
import de.mineformers.core.util.math.Vector3
import de.mineformers.core.util.world.BlockPos
import net.minecraft.client.renderer._
import net.minecraft.client.renderer.chunk._
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.client.renderer.vertex.{DefaultVertexFormats, VertexFormatElement}
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{EnumFacing, EnumWorldBlockLayer, MathHelper}
import net.minecraft.world.{IWorldAccess, World}
import org.lwjgl.opengl.GL11

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * RenderWorld
 *
 * @author PaleoCrafter
 */
class RenderWorld(world: World) extends IWorldAccess {
  private final val mc = RenderUtils.mc
  private final val renderEngine = mc.getTextureManager
  private final val renderManager = mc.getRenderManager
  private final val renderDispatcher = new ChunkRenderDispatcher()
  val vboEnabled = OpenGlHelper.useVbo
  private final val renderContainer = {
    if (vboEnabled)
      new VboRenderList
    else
      new RenderList
  }
  private final val renderChunkFactory = {
    if (vboEnabled)
      new VboChunkFactory
    else
      new ListChunkFactory
  }
  private var renderDistance = 0
  private var viewFrustum: MFViewFrustum = _
  private val renderInfos = ListBuffer.empty[RenderInfo]
  private var hasToUpdate = false
  private val chunksToUpdate = mutable.LinkedHashSet.empty[RenderChunk]
  private var renderSort = Vector3(0, 0, 0)

  def dispose(): Unit = {
    if(viewFrustum != null)
      viewFrustum.deleteGlResources()
  }

  def reloadRenderers(viewEntity: Entity): Unit = {
    if (this.world != null) {
      hasToUpdate = true
      this.renderDistance = mc.gameSettings.renderDistanceChunks

      if (viewFrustum != null) {
        viewFrustum.deleteGlResources()
      }

      viewFrustum = new MFViewFrustum(world, mc.gameSettings.renderDistanceChunks, this.renderChunkFactory)
      if (viewEntity != null) {
        this.viewFrustum.updateChunkPositions(viewEntity.posX, viewEntity.posZ)
      }
    }
  }

  def renderTileEntities(viewEntity: Entity, camera: ICamera, partialTicks: Float): Unit = {
    val pass = net.minecraftforge.client.MinecraftForgeClient.getRenderPass
    //    val interpX = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * partialTicks
    //    val interpY = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * partialTicks
    //    val interpZ = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * partialTicks
    TileEntityRendererDispatcher.instance.cacheActiveRenderInfo(this.world, this.mc.getTextureManager, this.mc.fontRendererObj, viewEntity, partialTicks)
    val camPos = RenderUtils.calculateCamPosition(viewEntity)
    TileEntityRendererDispatcher.staticPlayerX = camPos.x
    TileEntityRendererDispatcher.staticPlayerY = camPos.y
    TileEntityRendererDispatcher.staticPlayerZ = camPos.z
    RenderHelper.enableStandardItemLighting()
    import scala.collection.JavaConversions._
    for {info <- renderInfos
         tile <- info.renderChunk.getCompiledChunk.getTileEntities.asInstanceOf[java.util.List[TileEntity]]} {
      if (tile.shouldRenderInPass(pass) && camera.isBoundingBoxInFrustum(tile.getRenderBoundingBox))
        TileEntityRendererDispatcher.instance.renderTileEntity(tile, partialTicks, -1)
    }
  }

  def setupTerrain(viewEntity: Entity, partialTicks: Double, camera: ICamera, frameCount: Int): Unit = {
    if (this.mc.gameSettings.renderDistanceChunks != this.renderDistance) {
      this.reloadRenderers(viewEntity)
    }

    val camPos = RenderUtils.calculateCamPosition(viewEntity)
    renderContainer.initialize(camPos.x, camPos.y, camPos.z)
    val posRender = (camPos +(0, viewEntity.getEyeHeight, 0)).toBlockPos
    val tmp = this.viewFrustum(posRender)
    val renderChunk: RenderChunk = if(tmp == null || tmp.getCompiledChunk.isEmpty) null else tmp
    val pos = camPos.toBlockPos / 16 * 16
    hasToUpdate = hasToUpdate || chunksToUpdate.nonEmpty

    if (hasToUpdate) {
      hasToUpdate = false
      this.renderInfos.clear()

      val buffer = new mutable.Queue[RenderInfo]()

      if (renderChunk == null) {
        val y: Int = if (posRender.getY > 0) 248 else 8
        for (x <- -renderDistance to this.renderDistance;
             z <- -renderDistance to this.renderDistance) {
          val renderChunk: RenderChunk = this.viewFrustum(BlockPos(x * 16 + 8, 8, z * 16 + 8))
          if (renderChunk != null) {
            renderChunk.setFrameIndex(frameCount)
            buffer.enqueue(RenderInfo(renderChunk, null, 0))
          }
        }
      } else {
        val info = RenderInfo(renderChunk, null, 0)
        val visibleFacings = getVisibleFacings(posRender)

        if (visibleFacings.nonEmpty && visibleFacings.size == 1) {
          val view = Vector3.fromEntityView(viewEntity, partialTicks)
          visibleFacings.remove(EnumFacing.getFacingFromVector(view.x.toFloat, view.y.toFloat, view.z.toFloat))
        }

        if (visibleFacings.isEmpty) {
          renderInfos += info
        } else {
          renderChunk.setFrameIndex(frameCount)
          buffer.enqueue(info)
        }
      }

      val renderMany = this.mc.renderChunksMany

      while (buffer.nonEmpty) {
        val info = buffer.dequeue()
        val render = info.renderChunk
        val facing = info.facing
        val pos = BlockPos(render.getPosition)
        renderInfos += info

        for (face <- EnumFacing.values()) {
          val otherRender = getRenderChunkOffset(posRender, pos, face)

          if ((!renderMany || !info.facings.contains(face.getOpposite)) && (!renderMany || facing == null || render.getCompiledChunk.isVisible(facing.getOpposite, face)) && otherRender != null && otherRender.setFrameIndex(frameCount) && camera.isBoundingBoxInFrustum(otherRender.boundingBox)) {
            val otherInfo = RenderInfo(otherRender, face, info.counter + 1)
            info.facings.foreach(otherInfo.facings.add)
            otherInfo.facings.add(face)
            buffer.enqueue(otherInfo)
          }
        }
      }
    }

    this.renderDispatcher.clearChunkUpdates()
    val toUpdate = chunksToUpdate.toSet
    chunksToUpdate.clear()

    for (info <- renderInfos) {
      val render = info.renderChunk

      if (render.isNeedsUpdate || render.isCompileTaskPending || toUpdate.contains(render)) {
        this.hasToUpdate = true
        if (this.isPositionInRenderChunk(pos, render)) {
          this.renderDispatcher.updateChunkNow(render)
          render.setNeedsUpdate(false)
        }
        else {
          this.chunksToUpdate.add(render)
        }
      }
    }

    toUpdate.foreach(chunksToUpdate.add)
  }

  def renderBlockLayer(layer: EnumWorldBlockLayer, entity: Entity): Int = {
    RenderHelper.disableStandardItemLighting()

    if (layer == EnumWorldBlockLayer.TRANSLUCENT) {
      val sort = -renderSort + entity.getPositionVector

      if (sort.magSq > 1) {
        this.renderSort = sort
        var count = 0

        for (info <- renderInfos) {
          if (info.renderChunk.getCompiledChunk.isLayerStarted(layer) && count < 15) {
            count += 1
            this.renderDispatcher.updateTransparencyLater(info.renderChunk)
          }
        }
      }
    }

    var affected = 0
    val translucent = layer == EnumWorldBlockLayer.TRANSLUCENT
    val range = if (translucent) (renderInfos.size - 1) to 0 by -1 else 0 until renderInfos.size

    for (i <- range; render = renderInfos(i).renderChunk) {
      if (!render.getCompiledChunk.isLayerEmpty(layer)) {
        affected += 1
        renderContainer.addRenderChunk(render, layer)
      }
    }

    this.renderBlockLayer(layer)

    affected
  }

  private def renderBlockLayer(layer: EnumWorldBlockLayer): Unit = {
    if (OpenGlHelper.useVbo()) {
      GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY)
      OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit)
      GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY)
      OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit)
      GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY)
      OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit)
      GL11.glEnableClientState(GL11.GL_COLOR_ARRAY)
    }

    this.renderContainer.renderChunkLayer(layer)

    import scala.collection.JavaConversions._

    if (OpenGlHelper.useVbo) {
      val list = DefaultVertexFormats.BLOCK.getElements.toList.asInstanceOf[List[VertexFormatElement]]
      for (element <- list) {
        val usage: VertexFormatElement.EnumUsage = element.getUsage
        val i = element.getIndex
        usage match {
          case VertexFormatElement.EnumUsage.POSITION =>
            GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY)
          case VertexFormatElement.EnumUsage.UV =>
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + i)
            GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY)
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit)
          case VertexFormatElement.EnumUsage.COLOR =>
            GL11.glDisableClientState(GL11.GL_COLOR_ARRAY)
            GlStateManager.resetColor()
          case _ =>
        }
      }
    }
  }

  private def isPositionInRenderChunk(pos: BlockPos, render: RenderChunk): Boolean = {
    val chunkPos = render.getPosition
    if (MathHelper.abs_int(pos.x - chunkPos.getX) > 16) false else if (MathHelper.abs_int(pos.y - chunkPos.getY) > 16) false else MathHelper.abs_int(pos.z - chunkPos.getZ) <= 16
  }

  private def getRenderChunkOffset(renderPos: BlockPos, pos: BlockPos, face: EnumFacing): RenderChunk = {
    val offset = pos.offset(face, 16)
    if (MathHelper.abs_int(renderPos.x - offset.getX) > this.renderDistance * 16) null else if (offset.getY >= 0 && offset.getY < 256) if (MathHelper.abs_int(renderPos.getZ - offset.getZ) > this.renderDistance * 16) null else this.viewFrustum(offset) else null
  }

  private def getVisibleFacings(pos: BlockPos): mutable.Set[EnumFacing] = {
    val visGraph = new VisGraph
    val chunkOrigin = BlockPos(pos.getX >> 4 << 4, pos.getY >> 4 << 4, pos.getZ >> 4 << 4)
    val chunk = this.world.getChunkFromBlockCoords(chunkOrigin)
    import scala.collection.JavaConversions._
    val iterator = net.minecraft.util.BlockPos.getAllInBoxMutable(chunkOrigin, chunkOrigin.add(15, 15, 15))
    for (e <- iterator; pos = e.asInstanceOf[VBlockPos]) {
      if (chunk.getBlock(pos).isOpaqueCube)
        visGraph.func_178606_a(pos)
    }
    mutable.Set(visGraph.func_178609_b(pos).toSeq.asInstanceOf[Seq[EnumFacing]]: _*)
  }

  def updateChunks(time: Long): Unit = {
    this.hasToUpdate |= this.renderDispatcher.runChunkUploads(time)
    chunksToUpdate --= chunksToUpdate.takeWhile(c => this.renderDispatcher.updateChunkLater(c)) map {
      c => c.setNeedsUpdate(false)
        c
    }
  }

  override def markBlockForUpdate(pos: VBlockPos): Unit = {
    viewFrustum.markBlocksForUpdate(pos.getX - 1, pos.getY - 1, pos.getZ - 1, pos.getX + 1, pos.getY + 1, pos.getZ + 1)
  }

  override def playRecord(recordName: String, blockPosIn: VBlockPos): Unit = ()

  override def onEntityAdded(entityIn: Entity): Unit = ()

  override def spawnParticle(p_180442_1_ : Int, p_180442_2_ : Boolean, p_180442_3_ : Double, p_180442_5_ : Double, p_180442_7_ : Double, p_180442_9_ : Double, p_180442_11_ : Double, p_180442_13_ : Double, p_180442_15_ : Int*): Unit = ()

  override def playSound(soundName: String, x: Double, y: Double, z: Double, volume: Float, pitch: Float): Unit = ()

  override def onEntityRemoved(entityIn: Entity): Unit = ()

  override def broadcastSound(p_180440_1_ : Int, p_180440_2_ : VBlockPos, p_180440_3_ : Int): Unit = ()

  override def playSoundToNearExcept(except: EntityPlayer, soundName: String, x: Double, y: Double, z: Double, volume: Float, pitch: Float): Unit = ()

  override def markBlockRangeForRenderUpdate(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int): Unit = viewFrustum.markBlocksForUpdate(x1 - 1, y1 - 1, z1 - 1, x2 + 1, y2 + 1, z2 + 1)

  override def playAusSFX(p_180439_1_ : EntityPlayer, p_180439_2_ : Int, blockPosIn: VBlockPos, p_180439_4_ : Int): Unit = ()

  override def sendBlockBreakProgress(breakerId: Int, pos: VBlockPos, progress: Int): Unit = ()

  override def notifyLightSet(pos: VBlockPos): Unit = markBlockForUpdate(pos)

  case class RenderInfo(renderChunk: RenderChunk, facing: EnumFacing, counter: Int, facings: mutable.Set[EnumFacing] = mutable.Set.empty[EnumFacing])

}