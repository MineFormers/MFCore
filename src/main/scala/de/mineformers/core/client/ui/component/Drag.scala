package de.mineformers.core.client.ui.component

import de.mineformers.core.client.shape2d.{Rectangle, Point}
import de.mineformers.core.client.ui.util.{MouseButton, MouseEvent}

/**
 * Draggable
 *
 * @author PaleoCrafter
 */
trait Drag extends Component {
  reactions += {
    case e: MouseEvent.Click if e.button == MouseButton.Left =>
      val localPos = local(e.pos)
      if (hovered(e.pos) && controlRegion.contains(localPos)) {
        lastDragPosition = e.pos
        _dragged = true
      }
    case e: MouseEvent.Release if e.button == MouseButton.Left =>
      _dragged = false
      onStopDragging(e.pos)
    case e: MouseEvent.Move =>
      if (dragged) {
        this.screen += e.pos - lastDragPosition
        lastDragPosition = e.pos
      }
      snapToWindow()
  }

  def snapToWindow(): Unit = {
    val region = dragRegion
    if (screen.x < region.start.x)
      screen = Point(region.start.x, screen.y)
    if (screen.y < region.start.y)
      screen = Point(screen.x, region.start.y)
    if (screenBounds.end.x > region.end.x)
      screen = Point(region.end.x - width, screen.y)
    if (screenBounds.end.y > region.end.y)
      screen = Point(screen.x, region.end.y - height)
  }

  def onStopDragging(mousePos: Point): Unit = ()

  def canDrag: Boolean

  def dragRegion: Rectangle = Rectangle(Point(0, 0), context.size)

  def controlRegion: Rectangle = bounds

  def dragged = _dragged

  private var lastDragPosition = Point(0, 0)
  private var _dragged = false
}
