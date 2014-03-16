package de.mineformers.core.registry

import net.minecraft.item.Item
import cpw.mods.fml.common.registry.GameRegistry

case class ItemEntry(item: Item, modId: String = null)

object SharedItemRegistry extends SharedRegistry[String, ItemEntry] {
  def add(key: String, item: Item, modId: String = null): Unit = this.add(key, ItemEntry(item, modId))

  override protected def put(key: String, value: ItemEntry): Unit = GameRegistry.registerItem(value.item, key, value.modId)
}
