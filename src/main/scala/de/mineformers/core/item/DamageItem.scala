package de.mineformers.core.item

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack

class DamageItem(baseName: String, tab: CreativeTabs, subItems: Array[SubItem]) extends BaseItem(baseName, tab) {
  this.setHasSubtypes(true)
  this.setMaxDamage(0)

  override def getUnlocalizedName(stack: ItemStack): String = {
    val dmg = stack.getItemDamage.max(0).min(subItems.length - 1)
    "item." + baseName + "." + subItems(dmg).getName(stack)
  }
}
