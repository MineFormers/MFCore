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
package de.mineformers.core.structure

import de.mineformers.core.client.renderer.StructureChunkRenderer
import de.mineformers.core.util.Implicits.VBlockPos
import de.mineformers.core.util.world.BlockPos
import net.minecraft.block.state.IBlockState
import net.minecraft.client.multiplayer.ChunkProviderClient
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util
import net.minecraft.util.{AxisAlignedBB, EnumFacing, MathHelper}
import net.minecraft.world._
import net.minecraft.world.chunk.IChunkProvider
import net.minecraft.world.storage.{SaveHandlerMP, WorldInfo}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.mutable

/**
 * StructureWorld
 *
 * @author PaleoCrafter
 */
class StructureWorld(_structure: Structure, val pos: BlockPos, val side: Side) extends World(new SaveHandlerMP, new WorldInfo(StructureWorld.Settings, "Structure"), null, null, side.isClient) {
  import scala.collection.JavaConversions._
  val structure = _structure.copy
  val tiles = mutable.HashMap.empty[BlockPos, TileEntity]
  for (y <- 0 until structure.getHeight; layer = structure.getLayer(y); x <- 0 until layer.width; z <- 0 until layer.length) {
    val info = layer.get(x, z)
    if (info != null && info.getTileEntity != null) {
      val tile = info.getBlock.createTileEntity(this, info.getBlock.getStateFromMeta(info.getMetadata))
      if (tile != null) {
        tile.setWorldObj(this)
        tile.setPos(new VBlockPos(x, y, z))
        tile.validate()
        tile.readFromNBT(info.getTileEntity)
        tiles.put(BlockPos(x, y, z), tile)
      }
    }
  }
  val localWorldAccess: StructureWorldAccess = if (side.isClient) new StructureWorldAccessClient(this) else null
  if (localWorldAccess != null)
    worldAccesses.asInstanceOf[java.util.List[IWorldAccess]].add(localWorldAccess)

  override def getBlockState(pos: VBlockPos): IBlockState = {
    val info = structure.getBlock(pos)
    if (info != null) info.getBlock.getStateFromMeta(info.getMetadata) else null
  }

  override def getTileEntity(pos: VBlockPos): TileEntity = tiles.getOrElse(pos, null)

  override def isAirBlock(pos: VBlockPos): Boolean = {
    val block = getBlockState(pos).getBlock
    if (block == null)
      true
    else
      block.isAir(this, pos)
  }

  override def getRenderDistanceChunks: Int = 10

  override def extendedLevelsInChunkCache(): Boolean = false

  override def setBlockState(pos: VBlockPos, state: IBlockState, flags: Int): Boolean = {
    val info = structure.getBlock(pos)
    if (info != null) {
      info.setBlock(state.getBlock)
      info.setMetadata(state.getBlock.getMetaFromState(state))
      markBlockForUpdate(pos)
      true
    } else false
  }

  override def isSideSolid(pos: VBlockPos, side: EnumFacing, default: Boolean): Boolean = {
    val block = getBlockState(pos).getBlock
    if (block == null)
      false
    else
      block.isSideSolid(this, pos, side)
  }

  def dispose(): Unit = {
    for (a <- worldAccesses) {
      a match {
        case s: StructureWorldAccess =>
          s.dispose()
        case _ =>
      }
    }
    tiles.clear()
  }

  def checkChunks(pos: BlockPos): Unit = {
    if (pos.sharesChunk(bounds)) {
      update()
    }
  }

  def update(): Unit = {
    for (a <- worldAccesses) {
      a match {
        case s: StructureWorldAccess =>
          s.update()
        case _ =>
      }
    }
  }

  def bounds = AxisAlignedBB.fromBounds(pos.x, pos.y, pos.z, pos.x + width, pos.y + height, pos.z + length)

  def width = structure.getWidth

  def length = structure.getLength

  def height = structure.getHeight

  override def tick(): Unit = {
    structure.update(this)
  }

  def getTileTag(x: Int, y: Int, z: Int): NBTTagCompound = {
    val info = structure.getBlock(BlockPos(x, y, z))
    if (info != null)
      info.getTileEntity
    else
      null
  }

  def rotate(): Unit = {
    for (y <- 0 until height; x <- 0 until width; z <- 0 until length) {
      val info = structure.getBlock(BlockPos(x, y, z))
      if (info != null)
        info.rotate(this, y, EnumFacing.UP)
    }
  }

  override def createChunkProvider(): IChunkProvider = new ChunkProviderClient(this)

  override def getEntityByID(var1: Int): Entity = null
}

object StructureWorld {
  private final val Settings = new WorldSettings(0, WorldSettings.GameType.CREATIVE, false, false, WorldType.FLAT)
}

trait StructureWorldAccess extends IWorldAccess {
  def update(): Unit

  def dispose(): Unit

  override def markBlockRangeForRenderUpdate(minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int): Unit = {
    val boundXLow: Int = MathHelper.bucketInt(minX - 1, 16)
    val boundYLow: Int = MathHelper.bucketInt(minY - 1, 16)
    val boundZLow: Int = MathHelper.bucketInt(minZ - 1, 16)
    val boundXUp: Int = MathHelper.bucketInt(maxX + 1, 16)
    val boundYUp: Int = MathHelper.bucketInt(maxY + 1, 16)
    val boundZUp: Int = MathHelper.bucketInt(maxZ + 1, 16)
    for (x <- boundXLow to boundXUp;
         y <- boundYLow to boundYUp;
         z <- boundZLow to boundZUp) {
      markBlockForUpdate(BlockPos(x, y, z))
    }
  }

  @SideOnly(Side.CLIENT)
  def getChunkRenderers: List[StructureChunkRenderer] = null
}

class StructureWorldAccessClient(world: StructureWorld) extends StructureWorldAccess {
  var chunkRenderers = createRenderChunkList()

  @SideOnly(Side.CLIENT)
  override def getChunkRenderers: List[StructureChunkRenderer] = chunkRenderers

  override def update(): Unit = {
    for (render <- chunkRenderers) {
      render.update = true
    }
  }

  override def dispose(): Unit = {
    chunkRenderers foreach {
      _.dispose()
    }
  }

  override def markBlockForUpdate(pos: VBlockPos): Unit = for (render <- chunkRenderers) {
    if (BlockPos(pos).containedBy(render.bounds)) {
      render.update = true
    }
  }

  override def playRecord(recordName: String, blockPosIn: util.BlockPos): Unit = ()

  override def onEntityAdded(entityIn: Entity): Unit = ()

  override def spawnParticle(p_180442_1_ : Int, p_180442_2_ : Boolean, p_180442_3_ : Double, p_180442_5_ : Double, p_180442_7_ : Double, p_180442_9_ : Double, p_180442_11_ : Double, p_180442_13_ : Double, p_180442_15_ : Int*): Unit = ()

  override def playSound(soundName: String, x: Double, y: Double, z: Double, volume: Float, pitch: Float): Unit = ()

  override def onEntityRemoved(entityIn: Entity): Unit = ()

  override def broadcastSound(p_180440_1_ : Int, p_180440_2_ : util.BlockPos, p_180440_3_ : Int): Unit = ()

  override def playSoundToNearExcept(except: EntityPlayer, soundName: String, x: Double, y: Double, z: Double, volume: Float, pitch: Float): Unit = ()

  override def playAusSFX(p_180439_1_ : EntityPlayer, p_180439_2_ : Int, blockPosIn: util.BlockPos, p_180439_4_ : Int): Unit = ()

  override def sendBlockBreakProgress(breakerId: Int, pos: util.BlockPos, progress: Int): Unit = ()

  override def notifyLightSet(pos: util.BlockPos): Unit = ()

  def createRenderChunkList(): List[StructureChunkRenderer] = {
    val width = (world.width - 1) / 16 + 1
    val height = (world.height - 1) / 16 + 1
    val length = (world.length - 1) / 16 + 1
    var list = List.empty[StructureChunkRenderer]

    for (x <- 0 until width; y <- 0 until height; z <- 0 until length) {
      list :+= new StructureChunkRenderer(this.world, x, y, z)
    }

    list
  }
}