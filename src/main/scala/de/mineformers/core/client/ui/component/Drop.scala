package de.mineformers.core.client.ui.component

import de.mineformers.core.client.shape2d.{Rectangle, Point}

/**
 * Drop
 *
 * @author PaleoCrafter
 */
trait Drop extends Component with Drag {
  def onDrop(pos: Point, success: Boolean): Unit

  def dropRegion: Rectangle = dragRegion

  override def onStopDragging(mousePos: Point): Unit = {
    if(dropRegion.contains(mousePos))
      onDrop(dropRegion.local(mousePos), success = true)
    else
      onDrop(mousePos, success = false)
  }
}
