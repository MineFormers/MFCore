package de.mineformers.core.item

import net.minecraft.item.{ItemStack, ItemBlock}
import net.minecraft.block.Block
import de.mineformers.core.block.MetaBlock

class ItemBlockMeta(block: Block) extends ItemBlock(block) {
  this.setHasSubtypes(true)


  override def getMetadata(dmg: Int): Int = dmg

  override def getUnlocalizedName(stack: ItemStack): String = block.asInstanceOf[MetaBlock].getUnlocalizedName(stack)
}
