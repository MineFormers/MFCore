package de.mineformers.core.client.ui.view.inventory

import de.mineformers.core.client.ui.view.container.Panel
import de.mineformers.core.client.util.RenderUtils
import de.mineformers.core.inventory.Inventory
import de.mineformers.core.util.math.shape2d.Point

/**
 * PlayerInventory
 *
 * @author PaleoCrafter
 */
class PlayerInventory extends Panel {
  val inventory = Inventory.enhance(RenderUtils.mc.thePlayer.inventory)
  for (i <- 0 until 9) {
    val slot = ItemSlot.createContainer(inventory(i))
    slot.position = Point(i * 18, 0)
    add(slot)
  }
}
