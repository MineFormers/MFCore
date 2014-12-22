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

import cpw.mods.fml.relauncher.{Side, SideOnly}
import de.mineformers.core.client.shape2d.{Point, Size}
import de.mineformers.core.client.ui.component.container.{Frame, ScrollPanel}
import de.mineformers.core.client.ui.component.decoration.Label
import de.mineformers.core.client.ui.component.interaction.{Button, TextBox}
import de.mineformers.core.client.util.MultiPass
import de.mineformers.core.util.world.RichWorld._
import net.minecraft.block.material.Material
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.IIcon
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.common.util.ForgeDirection

/**
 * TestBlock
 *
 * @author PaleoCrafter
 */
class TestBlock extends BaseBlock("test123", "test123", CreativeTabs.tabBlock, Material.rock) with MultiPass {
  @SideOnly(Side.CLIENT) private var overlay: IIcon = null

  override def registerBlockIcons(p_149651_1_ : IIconRegister): Unit = {
    super.registerBlockIcons(p_149651_1_)
    overlay = p_149651_1_.registerIcon("stone")
  }

  override def getIcon(side: ForgeDirection, meta: Int, pass: Int): IIcon = {
    if (pass == 0) blockIcon
    else overlay
  }

  override def getMixedBrightnessForBlock(access: IBlockAccess, x: Int, y: Int, z: Int): Int = {
    if(renderPass == 0)
      super.getMixedBrightnessForBlock(access, x, y, z)
    else {
      println(access.getLightBrightnessForSkyBlocks(x, y, z, 15))
      15728880
    }
  }

  override def onBlockActivated(world: World, x: Int, y: Int, z: Int, side: EntityPlayer, player: Int, p_149727_7_ : Float, p_149727_8_ : Float, p_149727_9_ : Float): Boolean = {
    if (world.isClient) {
      val panel = new Frame(Size(100, 100))
      val scroll = new ScrollPanel(Size(92, 92), true, false)
      val label = new Label("test")
      val text = new TextBox("test", Size(50, 20))
      text.position = Point(0, 40)
      label.position = Point(-10, 5)
      val button = new Button("test")
      button.size = Size(100, 20)
      button.position = Point(10, 10)
      scroll add text
      scroll add label
      scroll add button
      panel add scroll
      val proxy = panel.newProxy
      Minecraft.getMinecraft.displayGuiScreen(proxy)
    }
    false
  }
}
