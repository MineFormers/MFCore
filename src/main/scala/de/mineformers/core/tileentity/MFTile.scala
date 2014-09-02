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

package de.mineformers.core.tileentity

import cpw.mods.fml.relauncher.{Side, SideOnly}
import de.mineformers.core.util.world.{Vector3, BlockPos}
import net.minecraft.tileentity.TileEntity

/**
 * MFTile
 *
 * @author PaleoCrafter
 */
class MFTile extends TileEntity {
  def world = worldObj

  def pos = BlockPos(x, y, z)

  def vecPos = Vector3(x, y, z)

  def x = xCoord

  def y = yCoord

  def z = zCoord

  def init(): Unit = ()

  @SideOnly(Side.CLIENT)
  def updateClient(): Unit = ()

  def updateServer(): Unit = ()

  def destroy(): Unit = {
    worldObj.setBlockToAir(x, y, z)
  }

  def update(): Unit = {
    markDirty()
    world.markBlockForUpdate(x, y, z)
    world.notifyBlockChange(x, y, z, blockType)
  }

  final override def updateEntity(): Unit = {
    if (!initialized) {
      init()
      initialized = true
    }
    if (worldObj.isRemote)
      updateClient()
    else
      updateServer()
  }

  private var initialized = false
}
