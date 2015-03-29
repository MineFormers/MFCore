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

import de.mineformers.core.util.math.Vector3
import net.minecraft.block.material.Material
import net.minecraft.block.state.{BlockState, IBlockState}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{BlockPos => VBlockPos, EnumFacing, MovingObjectPosition}
import net.minecraft.world.World

/**
 * MetaBlock
 *
 * @param baseName base unlocalized name
 * @param tab creative tab for the block
 * @param material material for the block
 * @param subBlocks a sequence of [[SubBlock]]s
 * @author PaleoCrafter
 */
class MetaBlock(baseName: String, tab: CreativeTabs, material: Material, variantName: String, subBlocks: Seq[SubBlock]) extends BaseBlock(baseName, baseName, tab, material) {
  final val property = new PropertySubBlock(variantName, subBlocks)

  setDefaultState(blockState.getBaseState.withProperty(property, subBlocks.head))

  /**
   * Get the sub block for the corresponding meta.
   * Automatically limits the passed metadata to available values.
   * @param state the state to get the sub block for
   * @return the sub block for the given meta
   */
  def apply(state: IBlockState): SubBlock = state.getValue(property).asInstanceOf[SubBlock]

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
   * @param state the state of the block
   * @return by default, the value of meta
   */
  override def damageDropped(state: IBlockState): Int = subBlocks.indexOf(this(state))

  /**
   * @param target the target
   * @param world the world the block is in
   * @param pos the block's position
   * @return an ItemStack representing the block with metadata
   */
  override def getPickBlock(target: MovingObjectPosition, world: World, pos: VBlockPos): ItemStack = new ItemStack(this, 1, getMetaFromState(world.getBlockState(pos)))

  /**
   * Redirect the "event" to a SubBlock
   * @param world the world the block is in
   * @param pos the block's position
   * @param state the block's state
   * @param player the player who activated the block
   * @param side the side the block was clicked on
   * @param hitX the x coordinate where the block was clicked (0..1)
   * @param hitY the y coordinate where the block was clicked (0..1)
   * @param hitZ the z coordinate where the block was clicked (0..1)
   * @return true, if something was done here
   */
  override def onBlockActivated(world: World, pos: VBlockPos, state: IBlockState, player: EntityPlayer, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean =
    this(state).onActivated(player, world, pos, Vector3(hitX, hitY, hitZ), side)

  /**
   * Called whenever the block is broken, redirect to sub block.
   * @param world the world the block is in
   * @param pos the block's position
   * @param state the block's state
   */
  override def breakBlock(world: World, pos: VBlockPos, state: IBlockState): Unit = {
    super.breakBlock(world, pos, state)
    apply(state).onBreak(world, pos)
  }

  override def hasTileEntity(state: IBlockState): Boolean = apply(state).hasTileEntity

  override def createTileEntity(world: World, state: IBlockState): TileEntity = apply(state).createTileEntity(world)

  /**
   * @param state the state of the block
   * @return the unlocalized name for the according SubBlock
   */
  def getUnlocalizedName(state: IBlockState): String =
    "tile." + baseName + "." + apply(state).name

  /**
   * @param stack the stack representing the block with metadata
   * @return the unlocalized name for the according SubBlock
   */
  def getUnlocalizedName(stack: ItemStack): String =
    "tile." + baseName + "." + apply(getStateFromMeta(stack.getItemDamage)).getName(stack)

  /**
   * @return the first SubBlock's unlocalized name
   */
  override def getUnlocalizedName: String = getUnlocalizedName(getDefaultState)

  override def createBlockState(): BlockState = new BlockState(this, property)

  override def getStateFromMeta(meta: Int): IBlockState = getDefaultState.withProperty(property, subBlocks(meta))

  override def getMetaFromState(state: IBlockState): Int = subBlocks.indexOf(this(state))
}
