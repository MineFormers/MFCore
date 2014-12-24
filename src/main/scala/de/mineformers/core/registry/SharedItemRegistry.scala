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
package de.mineformers.core.registry

import de.mineformers.core.client.util.ItemRendering
import net.minecraft.item.Item
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
 * ItemEntry representing an item in the registry's map
 * @param item the item associated with the entry
 * @param modId the mod id for the item
 */
case class ItemEntry(item: Item, modId: String = null)

/**
 * SharedItemRegistry
 * Register your items here :)
 *
 * @author PaleoCrafter
 */
object SharedItemRegistry extends SharedRegistry[String, Item] {
  @SideOnly(Side.CLIENT)
  def registerRenderers(): Unit = {
    this foreach {
      e =>
        val item = e._2
        item match {
          case r: ItemRendering =>
            MinecraftForgeClient.registerItemRenderer(item, r.createRenderer)
          case _ =>
        }
    }
  }

  /**
   * Registers the given item
   * @param key the mapping's key
   * @param value the mapping's value
   */
  override protected def put(key: String, value: Item): Unit = {
    GameRegistry.registerItem(value, key)
  }
}
