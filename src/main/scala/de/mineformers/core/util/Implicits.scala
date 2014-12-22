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
package de.mineformers.core.util

import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.world.World
import net.minecraft.world.chunk.storage.ExtendedBlockStorage

import scala.language.implicitConversions

/**
 * RichWorld
 *
 * @author PaleoCrafter
 */
object Implicits {
  implicit def funcToRunnable(func: => Unit) = new Runnable {
    override def run(): Unit = func
  }

  implicit class RichWorld(world: World) {
    def selectEntities[A <: Entity](clazz: Class[A], box: AxisAlignedBB): List[Entity] = selectEntities(box, clazz)(null)

    def selectEntitiesBut(but: Entity, box: AxisAlignedBB) = selectEntities(box) { case e if e != but => true
    case _ => false
    }

    def selectEntitiesBut[A <: Entity](but: A, box: AxisAlignedBB, clazz: Class[A]) = selectEntities(box, clazz) { case e if e != but => true
    case _ => false
    }

    def selectEntities(box: AxisAlignedBB)(implicit precondition: PartialFunction[Entity, Boolean] = null): List[Entity] = selectEntities(box, classOf[Entity])(precondition)

    def selectEntities[A <: Entity](box: AxisAlignedBB, clazz: Class[A])(implicit precondition: PartialFunction[A, Boolean]): List[A] = {
      import scala.collection.JavaConversions._
      world.getEntitiesWithinAABB(clazz, box).map(_.asInstanceOf[A]).filter(e => if (precondition != null && e != null) precondition.applyOrElse(e, (e1: A) => false) else true).toList
    }

    def isServer = !world.isRemote

    def isClient = !isServer

    def setBlockFast(x: Int, y: Int, z: Int, block: Block, meta: Int): Unit = {
      val chunk = world.getChunkFromBlockCoords(x, z)
      val chunkX = x & 15
      val chunkY = y & 15
      val chunkZ = z & 15
      val oldBlock = chunk.getBlock(chunkX, y, chunkZ)
      val oldMeta = chunk.getBlockMetadata(chunkX, y, chunkZ)
      if (oldBlock != block && !(oldBlock == block && oldMeta == meta)) {
        var storage = chunk.getBlockStorageArray()(y >> 4)
        if (storage == null) {
          if (block == Blocks.air)
            return
          storage = new ExtendedBlockStorage(y >> 4 << 4, !world.provider.hasNoSky)
          chunk.getBlockStorageArray.update(y >> 4, storage)
        }
        oldBlock.onBlockPreDestroy(world, x, y, z, oldMeta)
        storage.func_150818_a(chunkX, chunkY, chunkZ, block)
        storage.setExtBlockMetadata(chunkX, chunkY, chunkZ, meta)
        oldBlock.breakBlock(world, x, y, z, oldBlock, oldMeta)
        val te: TileEntity = chunk.getTileEntityUnsafe(chunkX, y, chunkZ)
        if (te != null && te.shouldRefresh(oldBlock, block, oldMeta, meta, world, x, y, z)) {
          chunk.removeTileEntity(chunkX, chunkY, chunkZ)
        }
      }
    }
  }

}