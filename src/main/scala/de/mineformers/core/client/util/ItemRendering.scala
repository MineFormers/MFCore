package de.mineformers.core.client.util

import net.minecraft.item.Item
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
 * ItemRendering
 *
 * @author PaleoCrafter
 */
trait ItemRendering {
  this: Item =>
  @SideOnly(Side.CLIENT)
  def createRenderer: IItemRenderer
}
