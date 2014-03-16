package de.mineformers.core.block

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.world.{World, IBlockAccess}
import net.minecraft.util.{MovingObjectPosition, IIcon}
import net.minecraftforge.common.util.ForgeDirection
import de.mineformers.core.util.world.{Vector3, BlockPos}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}

class MetaBlock(baseName: String, tab: CreativeTabs, material: Material, subBlocks: Array[SubBlock]) extends BaseBlock(baseName, baseName, tab, material) {

  override def getSubBlocks(item: Item, tab: CreativeTabs, stacks: java.util.List[_]): Unit = {
    for (i <- 0 until subBlocks.length)
      stacks.asInstanceOf[java.util.List[ItemStack]].add(new ItemStack(this, 1, i))
  }

  override def damageDropped(meta: Int): Int = meta

  override def getPickBlock(target: MovingObjectPosition, world: World, x: Int, y: Int, z: Int): ItemStack = new ItemStack(this, 1, world.getBlockMetadata(x, y, z))

  override def onBlockActivated(world: World, x: Int, y: Int, z: Int, player: EntityPlayer, side: Int, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    world.getWorldInfo.getWorldName
    val meta = world.getBlockMetadata(x, y, z).max(0).min(subBlocks.length - 1)
    subBlocks(meta).onActivated(player, world, BlockPos(x, y, z), Vector3(hitX, hitY, hitZ), ForgeDirection.getOrientation(side))
  }

  override def registerBlockIcons(iconRegister: IIconRegister): Unit = subBlocks foreach (block => block.registerIcons(iconRegister))

  override def getIcon(world: IBlockAccess, x: Int, y: Int, z: Int, side: Int): IIcon = {
    val meta = world.getBlockMetadata(x, y, z).max(0).min(subBlocks.length - 1)
    subBlocks(meta).getIcon(world, BlockPos(x, y, z), ForgeDirection.getOrientation(side))
  }

  def getUnlocalizedName(meta: Int): String = {
    val realMeta = meta.max(0).min(subBlocks.length - 1)
    "tile." + baseName + "." + subBlocks(realMeta).name
  }

  def getUnlocalizedName(stack: ItemStack): String = {
    val realMeta = stack.getItemDamage.max(0).min(subBlocks.length - 1)
    "tile." + baseName + "." + subBlocks(realMeta).getName(stack)
  }

  override def getUnlocalizedName: String = getUnlocalizedName(0)
}
