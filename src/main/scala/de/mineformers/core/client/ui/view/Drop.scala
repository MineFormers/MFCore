package de.mineformers.core.client.ui.view

import de.mineformers.core.client.ui.util.MouseButton.MouseButton
import de.mineformers.core.util.math.shape2d.{Point, Rectangle}

/**
 * Drop
 *
 * @author PaleoCrafter
 */
trait Drop extends View with Drag {
  def onDrop(pos: Point, success: Boolean): Unit

  def dropRegion: Rectangle = dragRegion

  override def onStopDragging(region: String, mousePos: Point, button: MouseButton): Unit = {
    if (dropRegion.contains(mousePos))
      onDrop(dropRegion.local(mousePos), success = true)
    else
      onDrop(mousePos, success = false)
  }
}
