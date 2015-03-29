package de.mineformers.core.client.ui.view

import de.mineformers.core.client.ui.util.MouseButton.MouseButton
import de.mineformers.core.client.ui.util.MouseEvent
import de.mineformers.core.util.math.shape2d.{Point, Rectangle}

import scala.collection.mutable

/**
 * Draggable
 *
 * @author PaleoCrafter
 */
trait Drag extends View {
  reactions += {
    case e: MouseEvent.Click =>
      val localPos = local(e.pos)
      if (hovered(e.pos)) {
        controlRegions.find(_._2.contains(localPos)) match {
          case Some((region, _)) =>
            lastDragPositions += (region, e.button) -> e.pos
            _dragged += (region, e.button) -> true
          case _ =>
        }
      }
  }

  globalReactions += {
    case e: MouseEvent.Release =>
      _dragged.find(d => d._1._2 == e.button && d._2) match {
        case Some((region, _)) =>
          _dragged += region -> false
          onStopDragging(region._1, e.pos, region._2)
        case _ =>
      }
    case e: MouseEvent.Move =>
      _dragged.find(_._2) match {
        case Some((region, _)) =>
          onDrag(region._1, e.pos, lastDragPositions(region), e.pos - lastDragPositions(region), region._2)
          lastDragPositions += region -> e.pos
        case _ =>
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

  def onStopDragging(region: String, mousePos: Point, button: MouseButton): Unit = ()

  def canDrag: Boolean

  def dragRegion: Rectangle = Rectangle(Point(0, 0), context.size)

  def controlRegions: Map[String, Rectangle] = Map("full" -> bounds)

  def onDrag(region: String, newPos: Point, lastPos: Point, delta: Point, button: MouseButton): Unit = {
    this.screen += delta
  }

  def dragged(region: String, button: MouseButton) = _dragged.get((region, button)) match {
    case Some(d) => d
    case _ => false
  }

  private var lastDragPositions = mutable.Map.empty[(String, MouseButton), Point]
  private var _dragged = mutable.Map.empty[(String, MouseButton), Boolean]
}
