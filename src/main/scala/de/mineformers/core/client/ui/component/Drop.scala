package de.mineformers.core.client.ui.component

import de.mineformers.core.util.math.shape2d.{Rectangle, Point}

/**
 * Drop
 *
 * @author PaleoCrafter
 */
trait Drop extends View with Drag {
  def onDrop(pos: Point, success: Boolean): Unit

  def dropRegion: Rectangle = dragRegion

  override def onStopDragging(region: String, mousePos: Point): Unit = {
    if(dropRegion.contains(mousePos))
      onDrop(dropRegion.local(mousePos), success = true)
    else
      onDrop(mousePos, success = false)
  }
}
