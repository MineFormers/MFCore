package de.mineformers.core.item

import net.minecraft.util.IIcon
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.item.ItemStack

trait SubItem {

  def name: String

  def texture: String = name

  def getName(stack: ItemStack) = name

  def getIcon(stack: ItemStack, pass: Int) = icon

  def registerIcons(register: IIconRegister): Unit = register.registerIcon(texture)

  protected var icon: IIcon = null

}
