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
package de.mineformers.core.item

import de.mineformers.core.mod.MFMod
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraftforge.fml.common.Loader

/**
 * BaseItem
 *
 * @param name the unlocalized name of the item
 * @param modelName the texture of the item
 * @param tab the creative tab for this item
 * @author PaleoCrafter
 */
class BaseItem(name: String, modelName: String, tab: CreativeTabs) extends Item {
  def this(name: String, tab: CreativeTabs) = this(name, name, tab)

  val model = {
    val mod = Loader.instance().activeModContainer()
    mod.getMod match {
      case m: MFMod =>
        m.model(modelName)
      case _ => mod.getModId.toLowerCase + ":" + modelName
    }
  }

  this.setUnlocalizedName({
    val mod = Loader.instance().activeModContainer()
    mod.getMod match {
      case m: MFMod =>
        m.name(name)
      case _ => mod.getModId.toLowerCase + ":" + name
    }
  })
  this.setCreativeTab(tab)

  def getModel(damage: Int) = model
}
