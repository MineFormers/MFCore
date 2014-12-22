package de.mineformers.core.client.util

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.item.Item
import net.minecraftforge.client.IItemRenderer

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
