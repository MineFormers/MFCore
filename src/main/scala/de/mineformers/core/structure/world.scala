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

import cpw.mods.fml.relauncher.{SideOnly, Side}
import de.mineformers.core.client.renderer.StructureChunkRenderer
import de.mineformers.core.util.world.BlockPos
import net.minecraft.block.Block
import net.minecraft.client.multiplayer.ChunkProviderClient
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{MathHelper, AxisAlignedBB}
import net.minecraft.world.biome.BiomeGenBase
import net.minecraft.world.chunk.IChunkProvider
import net.minecraft.world.storage.SaveHandlerMP
import net.minecraft.world._
import net.minecraftforge.common.util.ForgeDirection

import scala.collection.mutable

/**
 * StructureWorld
 *
 * @author PaleoCrafter
 */
class StructureWorld(_structure: Structure, val pos: BlockPos, val side: Side) extends World(new SaveHandlerMP, "DRASH", null, StructureWorld.Settings, null) {
  isRemote = side.isClient
  val structure = _structure.copy
  val tiles = mutable.HashMap.empty[BlockPos, TileEntity]
  for (y <- 0 until structure.getHeight; layer = structure.getLayer(y); x <- 0 until layer.width; z <- 0 until layer.length) {
    val info = layer.get(x, z)
    if (info != null && info.getTileEntity != null) {
      val tile = info.getBlock.createTileEntity(this, info.getMetadata)
      if (tile != null) {
        tile.setWorldObj(this)
        tile.xCoord = x
        tile.yCoord = y
        tile.zCoord = z
        tile.validate()
        tile.readFromNBT(info.getTileEntity)
        tiles.put(BlockPos(x, y, z), tile)
      }
    }
  }

  val localWorldAccess: StructureWorldAccess = if (side.isClient) new StructureWorldAccessClient(this) else null

  if (localWorldAccess != null)
    worldAccesses.asInstanceOf[java.util.List[IWorldAccess]].add(localWorldAccess)

  def update(): Unit = {
    import scala.collection.JavaConversions._
    for (a <- worldAccesses) {
      a match {
        case s: StructureWorldAccess =>
          s.update()
        case _ =>
      }
    }
  }

  override def getBlock(x: Int, y: Int, z: Int): Block = {
    val info = structure.getBlock(x, y, z)
    if (info != null) info.getBlock else Blocks.air
  }

  override def getBlockMetadata(x: Int, y: Int, z: Int): Int = {
    val info = structure.getBlock(x, y, z)
    if (info != null) info.getMetadata else 0
  }

  override def func_152379_p(): Int = 10

  override def getTileEntity(x: Int, y: Int, z: Int): TileEntity = tiles.getOrElse(BlockPos(x, y, z), null)

  override def isBlockNormalCubeDefault(x: Int, y: Int, z: Int, default: Boolean): Boolean = {
    val block = getBlock(x, y, z)
    if (block == null)
      false
    else if (block.isNormalCube)
      true
    else
      default
  }

  override def isAirBlock(x: Int, y: Int, z: Int): Boolean = {
    val block = getBlock(x, y, z)
    if (block == null)
      true
    else
      block.isAir(this, x, y, z)
  }

  override def getBiomeGenForCoords(x: Int, z: Int): BiomeGenBase = BiomeGenBase.jungle

  override def extendedLevelsInChunkCache(): Boolean = false

  override def blockExists(x: Int, y: Int, z: Int): Boolean = false

  override def getSkyBlockTypeBrightness(par1EnumSkyBlock: EnumSkyBlock, par2: Int, par3: Int, par4: Int): Int = 15

  override def getLightBrightness(par1: Int, par2: Int, par3: Int): Float = 1f

  override def setBlock(x: Int, y: Int, z: Int, block: Block, meta: Int, flags: Int): Boolean = {
    val info = structure.getBlock(x, y, z)
    if (info != null) {
      info.setBlock(block)
      info.setMetadata(meta)
      markBlockForUpdate(x, y, z)
      true
    } else false
  }

  override def setBlockMetadataWithNotify(x: Int, y: Int, z: Int, metadata: Int, flag: Int): Boolean = {
    val info = structure.getBlock(x, y, z)
    if (info != null) {
      info.setMetadata(metadata)
      markBlockForUpdate(x, y, z)
      true
    } else false
  }

  override def isSideSolid(x: Int, y: Int, z: Int, side: ForgeDirection, default: Boolean): Boolean = {
    val block = getBlock(x, y, z)
    if (block == null)
      false
    else
      block.isSideSolid(this, x, y, z, side)
  }

  def dispose(): Unit = {
    import scala.collection.JavaConversions._
    for (a <- worldAccesses) {
      a match {
        case s: StructureWorldAccess =>
          s.dispose()
        case _ =>
      }
    }
    tiles.clear()
  }

  def bounds = AxisAlignedBB.getBoundingBox(pos.x, pos.y, pos.z, pos.x + width, pos.y + height, pos.z + length)

  def width = structure.getWidth

  def length = structure.getLength

  def height = structure.getHeight

  def checkChunks(): Unit = {
    if (pos.sharesChunk(bounds))
      update()
  }

  override def tick(): Unit = {
    structure.update(this)
  }

  def getTileTag(x: Int, y: Int, z: Int): NBTTagCompound = {
    val info = structure.getBlock(x, y, z)
    if (info != null)
      info.getTileEntity
    else
      null
  }

  def rotate(): Unit = {
    for (y <- 0 until height; x <- 0 until width; z <- 0 until length) {
      val info = structure.getBlock(x, y, z)
      if (info != null)
        info.rotate(this, y, ForgeDirection.UP)
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
      markBlockForRenderUpdate(x, y, z)
    }
  }

  override def playRecord(p_72702_1_ : String, p_72702_2_ : Int, p_72702_3_ : Int, p_72702_4_ : Int): Unit = ()

  override def playAuxSFX(p_72706_1_ : EntityPlayer, p_72706_2_ : Int, p_72706_3_ : Int, p_72706_4_ : Int, p_72706_5_ : Int, p_72706_6_ : Int): Unit = ()

  override def onEntityDestroy(p_72709_1_ : Entity): Unit = ()

  override def destroyBlockPartially(p_147587_1_ : Int, p_147587_2_ : Int, p_147587_3_ : Int, p_147587_4_ : Int, p_147587_5_ : Int): Unit = ()

  override def spawnParticle(p_72708_1_ : String, p_72708_2_ : Double, p_72708_4_ : Double, p_72708_6_ : Double, p_72708_8_ : Double, p_72708_10_ : Double, p_72708_12_ : Double): Unit = ()

  override def playSound(p_72704_1_ : String, p_72704_2_ : Double, p_72704_4_ : Double, p_72704_6_ : Double, p_72704_8_ : Float, p_72704_9_ : Float): Unit = ()

  override def broadcastSound(p_82746_1_ : Int, p_82746_2_ : Int, p_82746_3_ : Int, p_82746_4_ : Int, p_82746_5_ : Int): Unit = ()

  override def playSoundToNearExcept(p_85102_1_ : EntityPlayer, p_85102_2_ : String, p_85102_3_ : Double, p_85102_5_ : Double, p_85102_7_ : Double, p_85102_9_ : Float, p_85102_10_ : Float): Unit = ()

  override def onEntityCreate(p_72703_1_ : Entity): Unit = ()

  override def onStaticEntitiesChanged(): Unit = ()

  @SideOnly(Side.CLIENT)
  def getChunkRenderers: List[StructureChunkRenderer] = null

  @SideOnly(Side.CLIENT)
  def getRenderBlocks: RenderBlocks = null
}

class StructureWorldAccessClient(world: StructureWorld) extends StructureWorldAccess {
  val renderer = new RenderBlocks(world)
  var chunkRenderers = createRenderChunkList()

  @SideOnly(Side.CLIENT)
  override def getChunkRenderers: List[StructureChunkRenderer] = chunkRenderers

  @SideOnly(Side.CLIENT)
  override def getRenderBlocks: RenderBlocks = renderer

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

  override def markBlockForUpdate(x: Int, y: Int, z: Int): Unit = {
    for (render <- chunkRenderers) {
      if (BlockPos(x, y, z).containedBy(render.bounds)) {
        render.update = true
      }
    }
  }

  override def markBlockForRenderUpdate(x: Int, y: Int, z: Int): Unit = markBlockForUpdate(x, y, z)

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