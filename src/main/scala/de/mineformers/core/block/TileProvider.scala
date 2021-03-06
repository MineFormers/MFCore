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

import de.mineformers.core.util.Implicits.VBlockPos
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
 * TileProvider
 *
 * @author PaleoCrafter
 */
trait TileProvider[T <: TileEntity] {
  this: Block =>
  /**
   * @param state check if there is a TE for the given state
   * @return true, if the supplied teClass is not null (by default)
   */
  override def hasTileEntity(state: IBlockState): Boolean = true

  /**
   * By default, creates a new TE from the teClass
   * @param world a world object
   * @param state the state of the block to get the TE for
   * @return a TileEntity instance, if hasTileEntity returns true
   */
  override def createTileEntity(world: World, state: IBlockState): TileEntity = tileClass.newInstance()

  override def onBlockEventReceived(world: World, pos: VBlockPos, state: IBlockState, eventId: Int, eventArgument: Int): Boolean = {
    val tileentity: TileEntity = world.getTileEntity(pos)
    if (tileentity != null) tileentity.receiveClientEvent(eventId, eventArgument) else false
  }

  def tileClass: Class[T]
}
