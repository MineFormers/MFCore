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

import cpw.mods.fml.relauncher.SideOnly
import de.mineformers.core.block.Rotatable.Side
import de.mineformers.core.tileentity.Rotation
import de.mineformers.core.util.Implicits.RichWorld
import de.mineformers.core.util.world.Vector3
import net.minecraft.block.{Block, BlockPistonBase}
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{AxisAlignedBB, IIcon, MathHelper}
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.common.util.ForgeDirection

/**
 * Rotatable
 *
 * @author PaleoCrafter
 */
trait Rotatable {
  this: Block =>
  def hasSingleIcon = false

  def canRotate(world: World, x: Int, y: Int, z: Int, axis: ForgeDirection): Boolean = true

  def setRotation(world: World, x: Int, y: Int, z: Int, rotation: ForgeDirection): Boolean

  def getRotation(world: IBlockAccess, x: Int, y: Int, z: Int): ForgeDirection

  @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
  def getIcon(world: World, x: Int, y: Int, z: Int, side: Rotatable.Side): IIcon = getIcon(world.getBlockMetadata(x, y, z), side)

  @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
  def getIcon(meta: Int, side: Rotatable.Side): IIcon

  @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
  def getIconSide(side: ForgeDirection, rotation: ForgeDirection): Rotatable.Side

  @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
  override def getIcon(side: Int, meta: Int): IIcon = getIcon(meta, Rotatable.Side.fromDirection(ForgeDirection.getOrientation(side)))

  @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
  override def getIcon(world: IBlockAccess, x: Int, y: Int, z: Int, side: Int): IIcon = {
    val w = Minecraft.getMinecraft.theWorld
    val direction = ForgeDirection.getOrientation(side)
    getIcon(w, x, y, z, getIconSide(direction, getRotation(w, x, y, z)))
  }

  @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
  override def registerBlockIcons(iconRegister: IIconRegister): Unit = {
    for (s <- iconSuffixes)
      icons += s -> iconRegister.registerIcon(this.textureProxy + (if (!hasSingleIcon) "_" + s else ""))
    if (defaultIcon != null)
      blockIcon = iconRegister.registerIcon(this.textureProxy + (if (!hasSingleIcon) "_" + defaultIcon else ""))
  }

  @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
  def textureProxy: String

  @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
  def iconSuffixes: Seq[String]

  @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
  def defaultIcon: String = null

  def getBoundingBox(world: IBlockAccess, x: Int, y: Int, z: Int): AxisAlignedBB = AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1)

  def getRotatedBoundingBox(world: IBlockAccess, x: Int, y: Int, z: Int) = rotateBox(world, x, y, z, getBoundingBox(world, x, y, z), getRotation(world, x, y, z))

  def rotateBox(world: IBlockAccess, x: Int, y: Int, z: Int, box: AxisAlignedBB, rotation: ForgeDirection): AxisAlignedBB = {
    import net.minecraftforge.common.util.ForgeDirection._
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
    AxisAlignedBB.getBoundingBox(min.x, min.y, min.z, max.x, max.y, max.z)
  }

  override def setBlockBoundsBasedOnState(world: IBlockAccess, x: Int, y: Int, z: Int): Unit = {
    val box = getRotatedBoundingBox(world, x, y, z)
    setBlockBounds(box.minX.toFloat, box.minY.toFloat, box.minZ.toFloat, box.maxX.toFloat, box.maxY.toFloat, box.maxZ.toFloat)
  }

  override def getCollisionBoundingBoxFromPool(world: World, x: Int, y: Int, z: Int): AxisAlignedBB = getRotatedBoundingBox(world, x, y, z).offset(x, y, z)

  protected val icons = collection.mutable.Map.empty[String, IIcon]
}

object Rotatable {
  type Side = Side.ValueLike[Any]

  object Side extends Enumeration {
    val Front: SideLike = Value(0)
    val Back: SideLike = Value(1)
    val Top: SideLike = Value(2)
    val Bottom: SideLike = Value(3)
    val Left: SideLike = Value(4)
    val Right: SideLike = Value(5)
    val Sides: SidesLike = Seq(Top.value, Bottom.value, Left.value, Right.value)
    val VerticalSides: SidesLike = Seq(Top.value, Bottom.value)
    val HorizontalSides: SidesLike = Seq(Left.value, Right.value)
    val Mantle: SidesLike = Sides.value :+ Back.value

    import net.minecraftforge.common.util.ForgeDirection._

    def fromDirection(dir: ForgeDirection) = dir match {
      case UP => Top
      case DOWN => Bottom
      case SOUTH => Front
      case NORTH => Back
      case WEST => Left
      case EAST => Right
      case _ => Sides
    }

    sealed trait ValueLike[+T] {
      def value: T
    }

    implicit class SideLike(val value: Value) extends ValueLike[Value]

    implicit class SidesLike(val value: Seq[Value]) extends ValueLike[Seq[Value]] {
      override def equals(obj: scala.Any): Boolean =
        obj match {
          case other: SidesLike => value == other.value
          case other: SideLike => value contains other.value
          case _ => false
        }
    }

  }

}

trait Rotatable4D extends Rotatable {
  this: Block =>

  import net.minecraftforge.common.util.ForgeDirection._

  override def rotateBlock(world: World, x: Int, y: Int, z: Int, axis: ForgeDirection): Boolean = axis match {
    case UP | DOWN =>
      this.setRotation(world, x, y, z, getRotation(world, x, y, z).getRotation(UP))
    case _ =>
      this.setRotation(world, x, y, z, axis)
  }

  override def onBlockPlacedBy(world: World, x: Int, y: Int, z: Int, entity: EntityLivingBase, stack: ItemStack): Unit = {
    if (world.isServer) {
      val dir = MathHelper.floor_double((entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3
      this.setRotation(world, x, y, z, dir match {
        case 0 => NORTH
        case 1 => EAST
        case 2 => SOUTH
        case 3 => WEST
        case _ => UNKNOWN
      })
    }
  }

  @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
  override def getIconSide(side: ForgeDirection, rotation: ForgeDirection): Rotatable.Side = {
    val opposite = rotation.getOpposite
    import de.mineformers.core.block.Rotatable.Side._
    side match {
      case `rotation` => Front
      case `opposite` => Back
      case UP => Top
      case DOWN => Bottom
      case _ => HorizontalSides
    }
  }

  @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
  override def getIcon(meta: Int, side: Side): IIcon = {
    import de.mineformers.core.block.Rotatable.Side._
    side match {
      case Front => icons("front")
      case Back => icons("back")
      case Top => icons("top")
      case Bottom => icons("bottom")
      case _ => icons("sides")
    }
  }

  @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
  override def iconSuffixes: Seq[String] = Seq("front", "back", "top", "bottom", "sides")
}

trait Rotatable6D extends Rotatable {
  this: Block =>
  override def rotateBlock(world: World, x: Int, y: Int, z: Int, axis: ForgeDirection): Boolean = this.setRotation(world, x, y, z, axis)

  override def onBlockPlacedBy(world: World, x: Int, y: Int, z: Int, entity: EntityLivingBase, stack: ItemStack): Unit = {
    if (world.isServer) {
      val dir = BlockPistonBase.determineOrientation(world, x, y, z, entity)
      this.setRotation(world, x, y, z, ForgeDirection.getOrientation(dir))
    }
  }

  @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
  override def getIconSide(side: ForgeDirection, rotation: ForgeDirection): Rotatable.Side = {
    val opposite = rotation.getOpposite
    import de.mineformers.core.block.Rotatable.Side._
    side match {
      case `rotation` => Front
      case `opposite` => Back
      case _ => Sides
    }
  }

  @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
  override def getIcon(meta: Int, side: Side): IIcon = {
    import de.mineformers.core.block.Rotatable.Side._
    side match {
      case Front => icons("front")
      case Back => icons("back")
      case _ => icons("sides")
    }
  }

  @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
  override def iconSuffixes: Seq[String] = Seq("front", "back", "sides")
}

trait MetaRotation {
  this: Block with Rotatable =>
  override def setRotation(world: World, x: Int, y: Int, z: Int, rotation: ForgeDirection): Boolean = {
    if (canRotate(world, x, y, z, rotation)) {
      world.setBlockMetadataWithNotify(x, y, z, rotation.ordinal(), 2)
      true
    } else {
      ForgeDirection.VALID_DIRECTIONS find {
        d =>
          canRotate(world, x, y, z, d)
      } match {
        case Some(d) =>
          setRotation(world, x, y, z, d)
        case None => false
      }
    }
  }

  override def getRotation(world: IBlockAccess, x: Int, y: Int, z: Int): ForgeDirection = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z))
}

trait TileRotation {
  this: Block with TileProvider[_ <: TileEntity with Rotation] with Rotatable =>
  override def setRotation(world: World, x: Int, y: Int, z: Int, rotation: ForgeDirection): Boolean = {
    if (canRotate(world, x, y, z, rotation)) {
      world.getTileEntity(x, y, z).asInstanceOf[Rotation].rotation = rotation
      world.markBlockForUpdate(x, y, z)
      true
    } else {
      setRotation(world, x, y, z, rotation.getRotation(ForgeDirection.UP))
      false
    }
  }

  override def getRotation(world: IBlockAccess, x: Int, y: Int, z: Int): ForgeDirection = world.getTileEntity(x, y, z).asInstanceOf[Rotation].rotation
}