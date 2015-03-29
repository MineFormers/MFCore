package de.mineformers.core.util.world.snapshot

import java.util

import de.mineformers.core.util.Implicits.VBlockPos
import de.mineformers.core.util.world.{BlockCuboid, BlockPos}
import net.minecraft.entity.EnumCreatureType
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.IProgressUpdate
import net.minecraft.world.chunk.{Chunk, EmptyChunk, IChunkProvider}
import net.minecraft.world.storage.{SaveHandlerMP, WorldInfo}
import net.minecraft.world.{World, WorldProvider, WorldSettings, WorldType}

import scala.collection.immutable.IntMap

/**
 * WorldSnapshot
 *
 * @author PaleoCrafter
 */
class WorldSnapshot(width: Int, height: Int, length: Int, client: Boolean) extends World(new SaveHandlerMP, new WorldInfo(WorldSnapshot.Settings, "Snapshot"), new WorldSnapshot.WorldProviderSnapshot(), de.mineformers.core.Proxy.profiler, client) {
  provider.registerWorld(this)
  this.chunkProvider = createChunkProvider()
  val chunkWidth = math.ceil(width / 16f).toInt
  val chunkLength = math.ceil(length / 16f).toInt

  def bounds = new BlockCuboid(BlockPos(0, 0, 0), BlockPos(width - 1, height - 1, length - 1))

  def paste(world: World, pos: BlockPos): Unit = {
    bounds foreach {
      p =>
        world.setBlockState(pos + p, getBlockState(p))
        world.setTileEntity(pos + p, getTileEntity(p))
    }
  }

  override def isValid(pos: VBlockPos): Boolean = bounds.contains(pos)

  override def createChunkProvider(): IChunkProvider = new ChunkProviderSnapshot

  override def getRenderDistanceChunks: Int = 0

  class ChunkProviderSnapshot extends IChunkProvider {
    private val emptyChunk = new EmptyChunk(WorldSnapshot.this, 0, 0)
    lazy val chunks = IntMap[Chunk]((for {x <- 0 until chunkWidth
                                     z <- 0 until chunkLength} yield (x * 31 + z, new Chunk(WorldSnapshot.this, x, z))): _*)

    override def chunkExists(x: Int, z: Int): Boolean = x >= 0 && x < chunkWidth && z >= 0 && z < chunkLength

    override def canSave: Boolean = false

    override def getPossibleCreatures(creatureType: EnumCreatureType, p_177458_2_ : VBlockPos): util.List[_] = null

    override def makeString(): String = "WorldSnapshotChunkCache: " + getLoadedChunkCount

    override def getLoadedChunkCount: Int = chunkWidth * chunkLength

    override def saveExtraData(): Unit = ()

    override def saveChunks(p_73151_1_ : Boolean, p_73151_2_ : IProgressUpdate): Boolean = false

    override def func_177460_a(p_177460_1_ : IChunkProvider, p_177460_2_ : Chunk, p_177460_3_ : Int, p_177460_4_ : Int): Boolean = false

    override def populate(p_73153_1_ : IChunkProvider, p_73153_2_ : Int, p_73153_3_ : Int): Unit = ()

    override def recreateStructures(p_180514_1_ : Chunk, p_180514_2_ : Int, p_180514_3_ : Int): Unit = ()

    override def getStrongholdGen(worldIn: World, structureName: String, position: VBlockPos): VBlockPos = null

    override def provideChunk(x: Int, z: Int): Chunk = {
      chunks.get(x * 31 + z) match {
        case Some(c) => c
        case None => emptyChunk
      }
    }

    override def provideChunk(blockPosIn: VBlockPos): Chunk = provideChunk(blockPosIn.getX, blockPosIn.getZ)

    override def unloadQueuedChunks(): Boolean = false
  }

}

object WorldSnapshot {
  private final val Settings = new WorldSettings(0, WorldSettings.GameType.CREATIVE, false, false, WorldType.FLAT)

  def fromArea(source: World, area: BlockCuboid): WorldSnapshot = {
    val snapshot = new WorldSnapshot(area.width, area.height, area.length, source.isRemote)
    area foreach {
      p =>
        val localPos = area.local(p)
        snapshot.setBlockState(localPos, source.getBlockState(p))
        val tile = snapshot.getTileEntity(localPos)
        if(source.getTileEntity(p) != null) {
          val nbt = new NBTTagCompound
          source.getTileEntity(p).writeToNBT(nbt)
          nbt.setInteger("x", localPos.x)
          nbt.setInteger("y", localPos.y)
          nbt.setInteger("z", localPos.z)
          tile.readFromNBT(nbt)
        }
    }
    snapshot
  }

  class WorldProviderSnapshot extends WorldProvider {
    override def getDimensionName: String = "Snapshot"

    override def getInternalNameSuffix: String = "Snapshot"
  }

}
