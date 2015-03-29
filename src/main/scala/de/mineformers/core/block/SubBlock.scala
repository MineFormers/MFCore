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
import de.mineformers.core.util.world.BlockPos
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{EnumFacing, IStringSerializable}
import net.minecraft.world.World

/**
 * SubBlock
 *
 * @author PaleoCrafter
 */
trait SubBlock extends Comparable[SubBlock] with IStringSerializable {
  /**
   * @return the unlocalized name for this SubBlock
   */
  def name: String

  /**
   * @return the texture for this SubBlock, normally the name
   */
  def texture: String = name

  /**
   * @param stack the stack representing the block
   * @return the name for the given stack
   */
  def getName(stack: ItemStack) = name

  /**
   * Called whenever this SubBlock is activated
   * @param player the player who activated the block
   * @param world the world the block is in
   * @param pos the position of the block
   * @param hitVec the vector the block was clicked on
   * @param side the side the block was clicked on
   * @return true, if something was done
   */
  def onActivated(player: EntityPlayer, world: World, pos: BlockPos, hitVec: Vector3, side: EnumFacing): Boolean = false

  /**
   * Called whenever this SubBlock is broken
   * @param world the world the block is in
   * @param pos the position of the block
   */
  def onBreak(world: World, pos: BlockPos): Unit = ()

  /**
   * @return true, if this SubBlock has a TE
   */
  def hasTileEntity: Boolean = false

  /**
   * Create a new TE
   * @param world world passed on from createNewTileEntity
   * @return a TE, null by default
   */
  def createTileEntity(world: World): TileEntity = null

  override def compareTo(o: SubBlock): Int = {
    o.name.compareTo(name)
  }

  override def getName: String = name
}
