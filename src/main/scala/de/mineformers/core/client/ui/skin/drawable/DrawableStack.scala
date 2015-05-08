package de.mineformers.core.client.ui.skin.drawable

import de.mineformers.core.util.math.shape2d.Point
import net.minecraft.item.ItemStack

/**
 * DrawableStack
 *
 * @author PaleoCrafter
 */
class DrawableStack(stack: ItemStack) extends Drawable {
  override def draw(mousePos: Point, pos: Point, z: Int): Unit = {
    utils.drawItemStack(stack, pos.x, pos.y, z, null)
  }
}
