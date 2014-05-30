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

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity

import net.minecraft.world.World
import cpw.mods.fml.common.Loader
import net.minecraft.creativetab.CreativeTabs

/**
 * BaseBlock
 *
 * @param name unlocalized name of the block
 * @param texture texture of the block
 * @param tab creative tab for the block
 * @param material material for the block
 * @author PaleoCrafter
 */
class BaseBlock(name: String, texture: String, tab: CreativeTabs, material: Material) extends Block(material) {

  this.setBlockName(name)
  this.setCreativeTab(tab)
  this.setBlockTextureName(Loader.instance().activeModContainer().getModId.toLowerCase + ":" + texture)

}
