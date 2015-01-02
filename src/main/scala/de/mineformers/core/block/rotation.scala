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
package de.mineformers.core.block

import com.google.common.base.Predicate
import de.mineformers.core.tileentity.Rotation
import de.mineformers.core.util.Implicits._
import de.mineformers.core.util.math.Vector3
import de.mineformers.core.util.world.RichWorld._
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.block.state.{BlockState, IBlockState}
import net.minecraft.block.{Block, BlockPistonBase}
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing._
import net.minecraft.util.{AxisAlignedBB, EnumFacing, MathHelper}
import net.minecraft.world.{IBlockAccess, World}

/**
 * Rotatable
 *
 * @author PaleoCrafter
 */
trait Rotatable {
  this: Block =>
  def hasSingleIcon = false

  def createProperty: PropertyDirection

  def canRotate(world: World, x: Int, y: Int, z: Int, axis: EnumFacing): Boolean = true

  def setRotation(world: World, x: Int, y: Int, z: Int, rotation: EnumFacing): Boolean

  def getRotation(world: IBlockAccess, x: Int, y: Int, z: Int): EnumFacing

  def getBoundingBox(world: IBlockAccess, x: Int, y: Int, z: Int): AxisAlignedBB = AxisAlignedBB.fromBounds(0, 0, 0, 1, 1, 1)

  def getRotatedBoundingBox(world: IBlockAccess, x: Int, y: Int, z: Int) = rotateBox(world, x, y, z, getBoundingBox(world, x, y, z), getRotation(world, x, y, z))

  def rotateBox(world: IBlockAccess, x: Int, y: Int, z: Int, box: AxisAlignedBB, rotation: EnumFacing): AxisAlignedBB = {
    val min = rotation match {
      case UP => Vector3(box.minX, box.minY, box.minZ)
      case DOWN => Vector3(1 - box.maxX, 1 - box.maxY, 1 - box.maxZ)
      case SOUTH => Vector3(box.minX, box.minZ, box.minY)
      case NORTH => Vector3(1 - box.maxX, 1 - box.maxZ, 1 - box.maxY)
      case EAST => Vector3(box.minY, box.minX, box.minZ)
      case WEST => Vector3(1 - box.maxY, 1 - box.maxX, 1 - box.maxZ)
      case _ => Vector3(0, 0, 0)
    }
    val max = rotation match {
      case UP => Vector3(box.maxX, box.maxY, box.maxZ)
      case DOWN => Vector3(1 - box.minX, 1 - box.minY, 1 - box.minZ)
      case SOUTH => Vector3(box.maxX, box.maxZ, box.maxY)
      case NORTH => Vector3(1 - box.minX, 1 - box.minZ, 1 - box.minY)
      case EAST => Vector3(box.maxY, box.maxX, box.maxZ)
      case WEST => Vector3(1 - box.minY, 1 - box.minX, 1 - box.minZ)
      case _ => Vector3(1, 1, 1)
    }
    AxisAlignedBB.fromBounds(min.x, min.y, min.z, max.x, max.y, max.z)
  }

  override def setBlockBoundsBasedOnState(world: IBlockAccess, pos: VBlockPos): Unit = {
    val box = getRotatedBoundingBox(world, pos.getX, pos.getY, pos.getZ)
    setBlockBounds(box.minX.toFloat, box.minY.toFloat, box.minZ.toFloat, box.maxX.toFloat, box.maxY.toFloat, box.maxZ.toFloat)
  }

  override def getCollisionBoundingBox(world: World, pos: VBlockPos, state: IBlockState): AxisAlignedBB = getRotatedBoundingBox(world, pos.getX, pos.getY, pos.getZ).offset(pos.getX, pos.getY, pos.getZ)
}

trait Rotatable4D extends Rotatable {
  this: Block =>
  override def onBlockPlacedBy(world: World, pos: VBlockPos, state: IBlockState, entity: EntityLivingBase, stack: ItemStack): Unit = {
    if (world.isServer) {
      val dir = MathHelper.floor_double((entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3
      this.setRotation(world, pos.getX, pos.getY, pos.getZ, dir match {
        case 0 => NORTH
        case 1 => EAST
        case 2 => SOUTH
        case 3 => WEST
        case _ => NORTH
      })
    }
  }

  override def createProperty: PropertyDirection = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL.asInstanceOf[Predicate[_]])
}

trait Rotatable6D extends Rotatable {
  this: Block =>
  override def onBlockPlacedBy(world: World, pos: VBlockPos, state: IBlockState, entity: EntityLivingBase, stack: ItemStack): Unit = {
    if (world.isServer) {
      val dir = BlockPistonBase.getFacingFromEntity(world, pos, entity)
      this.setRotation(world, pos.getX, pos.getY, pos.getZ, dir)
    }
  }

  override def createProperty: PropertyDirection = PropertyDirection.create("facing")
}

trait StateRotation {
  this: Block with Rotatable =>

  val property = createProperty

  this.setDefaultState(this.getBlockState.getBaseState.withProperty(property, EnumFacing.NORTH))

  override def setRotation(world: World, x: Int, y: Int, z: Int, rotation: EnumFacing): Boolean = {
    if (canRotate(world, x, y, z, rotation)) {
      val pos = new VBlockPos(x, y, z)
      val state = world.getBlockState(pos)
      world.setBlockState(pos, state.withProperty(property, rotation), 2)
      true
    } else {
      EnumFacing.values() find {
        d =>
          canRotate(world, x, y, z, d)
      } match {
        case Some(d) =>
          setRotation(world, x, y, z, d)
        case None => false
      }
    }
  }

  override def getRotation(world: IBlockAccess, x: Int, y: Int, z: Int): EnumFacing = world.getBlockState(new VBlockPos(x, y, z)).getValue(property).asInstanceOf[EnumFacing]

  override def createBlockState(): BlockState = new BlockState(this, property)

  override def getMetaFromState(state: IBlockState): Int = state.getValue(property).asInstanceOf[EnumFacing].getIndex

  override def getStateFromMeta(meta: Int): IBlockState = getDefaultState.withProperty(property, EnumFacing.values()(meta))
}

trait TileRotation {
  this: Block with TileProvider[_ <: TileEntity with Rotation] with Rotatable =>
  override def setRotation(world: World, x: Int, y: Int, z: Int, rotation: EnumFacing): Boolean = {
    if (canRotate(world, x, y, z, rotation)) {
      val pos = new VBlockPos(x, y, z)
      world.getTileEntity(pos).asInstanceOf[Rotation].rotation = rotation
      world.markBlockForUpdate(pos)
      true
    } else {
      setRotation(world, x, y, z, rotation.rotateY())
      false
    }
  }

  override def getRotation(world: IBlockAccess, x: Int, y: Int, z: Int): EnumFacing = world.getTileEntity(new VBlockPos(x, y, z)).asInstanceOf[Rotation].rotation
}