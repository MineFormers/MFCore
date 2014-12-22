package de.mineformers.core.client.util

import cpw.mods.fml.relauncher.{Side, SideOnly}
import de.mineformers.core.client.renderer.TranslucentItemRenderer
import net.minecraft.item.Item
import net.minecraftforge.client.IItemRenderer

/**
 * TranslucentIcon
 *
 * @author PaleoCrafter
 */
trait TranslucentIcon extends ItemRendering {
  this: Item =>
  @SideOnly(Side.CLIENT)
  override def createRenderer: IItemRenderer = new TranslucentItemRenderer
}
