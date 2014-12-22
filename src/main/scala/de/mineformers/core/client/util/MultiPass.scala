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
package de.mineformers.core.client.util

import net.minecraft.block.Block
import net.minecraft.util.IIcon
import net.minecraft.world.IBlockAccess
import net.minecraftforge.common.util.ForgeDirection

/**
 * MultiPass
 *
 * @author PaleoCrafter
 */
trait MultiPass {
  this: Block =>
  var renderPass = 0

  def getIcon(side: ForgeDirection, meta: Int, pass: Int): IIcon = getIcon(side.ordinal(), meta)

  def getIcon(access: IBlockAccess, x: Int, y: Int, z: Int, side: ForgeDirection, pass: Int): IIcon = getIcon(side, access.getBlockMetadata(x, y, z), pass)

  override def getIcon(access: IBlockAccess, x: Int, y: Int, z: Int, side: Int): IIcon = getIcon(access, x, y, z, ForgeDirection.getOrientation(side), renderPass)

  override def canRenderInPass(pass: Int): Boolean = {
    renderPass = pass
    true
  }

  override def getRenderBlockPass: Int = 1
}
