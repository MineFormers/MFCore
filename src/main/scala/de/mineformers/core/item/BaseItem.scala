package de.mineformers.core.item

import net.minecraft.item.Item
import net.minecraft.creativetab.CreativeTabs

class BaseItem(name: String, texture: String, tab: CreativeTabs) extends Item {

  def this(name: String, tab: CreativeTabs) = this(name, name, tab)

  this.setUnlocalizedName(name)
  this.setTextureName(texture)

}
