package de.mineformers.core.block

import net.minecraftforge.common.util.ForgeDirection
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.world.{IBlockAccess, World}
import net.minecraft.tileentity.TileEntity
import de.mineformers.core.util.world.{Vector3, BlockPos}
import net.minecraft.util.IIcon
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

trait SubBlock {

  def name: String

  def texture: String = name

  def getName(stack: ItemStack) = name

  def getIcon(world: IBlockAccess, pos: BlockPos, side: ForgeDirection): IIcon = getIcon(side)

  def getIcon(side: ForgeDirection): IIcon = icon

  def onActivated(player: EntityPlayer, world: World, pos: BlockPos, hitVec: Vector3, side: ForgeDirection): Boolean = false

  def registerIcons(register: IIconRegister): Unit = register.registerIcon(texture)

  def hasTileEntity: Boolean = false

  def createTileEntity(world: World): TileEntity = null

  protected var icon: IIcon = null

}
