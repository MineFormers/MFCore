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

import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs
import cpw.mods.fml.relauncher.{SideOnly, Side}
import net.minecraft.util.IIcon
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.world.World
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.client.Minecraft
import de.mineformers.core.client.ui.component.container.Frame
import de.mineformers.core.client.ui.component.decoration.Label
import de.mineformers.core.client.shape2d.Size
import de.mineformers.core.client.ui.reaction.Event

/**
 * TestBlock
 *
 * @author PaleoCrafter
 */
class TestBlock extends BaseBlock("test123", "test123", CreativeTabs.tabBlock, Material.rock) {
  @SideOnly(Side.CLIENT) private var field_149994_N: IIcon = null

  var pass = 0

  override def canRenderInPass(pass: Int): Boolean = {
    this.pass = pass
    true
  }

  override def getRenderBlockPass: Int = 1

  override def registerBlockIcons(p_149651_1_ : IIconRegister): Unit = {
    super.registerBlockIcons(p_149651_1_)
    field_149994_N = p_149651_1_.registerIcon("grass_side_overlay")
  }

  override def getIcon(p_149691_1_ : Int, p_149691_2_ : Int): IIcon = {
    if (pass == 0)
      blockIcon
    else
      field_149994_N
  }

  override def onBlockActivated(p_149727_1_ : World, p_149727_2_ : Int, p_149727_3_ : Int, p_149727_4_ : Int, p_149727_5_ : EntityPlayer, p_149727_6_ : Int, p_149727_7_ : Float, p_149727_8_ : Float, p_149727_9_ : Float): Boolean = {
    if (p_149727_1_.isRemote) {
      val panel = new Frame(Size(100, 100))
      panel add new Label("test")
      val proxy = panel.proxy
      proxy.listenTo(proxy)
      proxy.reactions += {
        case e: Event => println(e)
      }
      Minecraft.getMinecraft.displayGuiScreen(proxy)
    }
    false
  }
}
