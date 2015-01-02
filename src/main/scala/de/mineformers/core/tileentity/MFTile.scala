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

import de.mineformers.core.util.math.Vector3
import net.minecraft.server.gui.IUpdatePlayerListBox
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.util.Random

/**
 * MFTile
 *
 * @author PaleoCrafter
 */
class MFTile extends TileEntity with IUpdatePlayerListBox {
  private var initialized = false
  lazy val random = new Random(pos.hashCode)

  final override def update(): Unit = {
    if (!initialized) {
      init()
      initialized = true
    }
    if (worldObj.isRemote)
      updateClient()
    else
      updateServer()
  }

  def init(): Unit = ()

  @SideOnly(Side.CLIENT)
  def updateClient(): Unit = ()

  def updateServer(): Unit = ()

  def vecPos = Vector3(x, y, z)

  def destroy(): Unit = {
    worldObj.setBlockToAir(pos)
  }

  def x = pos.getX

  def y = pos.getY

  def z = pos.getZ

  def updateState(): Unit = {
    markDirty()
    world.markBlockForUpdate(pos)
    world.notifyBlockOfStateChange(pos, blockType)
  }

  def world = worldObj
}
