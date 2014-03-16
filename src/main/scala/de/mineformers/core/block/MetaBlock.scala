/*******************************************************************************
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
 ******************************************************************************/
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

/**
 * MetaBlock
 *
 * @param baseName base unlocalized name
 * @param tab creative tab for the block
 * @param material material for the block
 * @param subBlocks an array of [[SubBlock]]s
 * @author PaleoCrafter
 */
class MetaBlock(baseName: String, tab: CreativeTabs, material: Material, subBlocks: Array[SubBlock]) extends BaseBlock(baseName, baseName, tab, material) {

  /**
   * Add all the sub blocks to the creative tab
   * @param item the ItemBlock representation of the block
   * @param tab the creative tab to add to
   * @param stacks a list of stacks to add to
   */
  override def getSubBlocks(item: Item, tab: CreativeTabs, stacks: java.util.List[_]): Unit = {
    for (i <- 0 until subBlocks.length)
      stacks.asInstanceOf[java.util.List[ItemStack]].add(new ItemStack(this, 1, i))
  }

  /**
   * @param meta the metadata of the block
   * @return by default, the value of meta
   */
  override def damageDropped(meta: Int): Int = meta

  /**
   * @param target the target
   * @param world the world the block is in
   * @param x the block's x coordinate
   * @param y the block's y coordinate
   * @param z the block's z coordinate
   * @return an ItemStack representing the block with metadata
   */
  override def getPickBlock(target: MovingObjectPosition, world: World, x: Int, y: Int, z: Int): ItemStack = new ItemStack(this, 1, world.getBlockMetadata(x, y, z))

  /**
   * Redirect the "event" to a SubBlock
   * @param world the world the block is in
   * @param x the block's x coordinate
   * @param y the block's y coordinate
   * @param z the block's z coordinate
   * @param player the player who activated the block
   * @param side the side the block was clicked on
   * @param hitX the x coordinate where the block was clicked (0..1)
   * @param hitY the y coordinate where the block was clicked (0..1)
   * @param hitZ the z coordinate where the block was clicked (0..1)
   * @return true, if something was done here
   */
  override def onBlockActivated(world: World, x: Int, y: Int, z: Int, player: EntityPlayer, side: Int, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    world.getWorldInfo.getWorldName
    val meta = world.getBlockMetadata(x, y, z).max(0).min(subBlocks.length - 1)
    subBlocks(meta).onActivated(player, world, BlockPos(x, y, z), Vector3(hitX, hitY, hitZ), ForgeDirection.getOrientation(side))
  }

  /**
   * Register the SubBlocks' icons
   * @param iconRegister the icon register to add the icons to
   */
  override def registerBlockIcons(iconRegister: IIconRegister): Unit = subBlocks foreach (block => block.registerIcons(iconRegister))

  /**
   * Get the icon for the according SubBlock
   * @param world the world the block is in
   * @param x the block's x coordinate
   * @param y the block's y coordinate
   * @param z the block's z coordinate
   * @param side the side the icon is drawn on
   * @return the icon for the given parameters
   */
  override def getIcon(world: IBlockAccess, x: Int, y: Int, z: Int, side: Int): IIcon = {
    val meta = world.getBlockMetadata(x, y, z).max(0).min(subBlocks.length - 1)
    subBlocks(meta).getIcon(world, BlockPos(x, y, z), ForgeDirection.getOrientation(side))
  }

  /**
   * @param meta the metadata of the block
   * @return the unlocalized name for the according SubBlock
   */
  def getUnlocalizedName(meta: Int): String = {
    val realMeta = meta.max(0).min(subBlocks.length - 1)
    "tile." + baseName + "." + subBlocks(realMeta).name
  }

  /**
   * @param stack the stack representing the block with metadata
   * @return the unlocalized name for the according SubBlock
   */
  def getUnlocalizedName(stack: ItemStack): String = {
    val realMeta = stack.getItemDamage.max(0).min(subBlocks.length - 1)
    "tile." + baseName + "." + subBlocks(realMeta).getName(stack)
  }

  /**
   * @return the first SubBlock's unlocalized name
   */
  override def getUnlocalizedName: String = getUnlocalizedName(0)
}
