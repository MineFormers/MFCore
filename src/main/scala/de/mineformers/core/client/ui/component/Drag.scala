package de.mineformers.core.client.ui.component

import de.mineformers.core.util.math.shape2d.{Point, Rectangle}
import de.mineformers.core.client.ui.util.{MouseButton, MouseEvent}

import scala.collection.mutable

/**
 * Draggable
 *
 * @author PaleoCrafter
 */
trait Drag extends View {
  reactions += {
    case e: MouseEvent.Click if e.button == MouseButton.Left =>
      val localPos = local(e.pos)
      if (hovered(e.pos)) {
        controlRegions.find(_._2.contains(localPos)) match {
          case Some((region, _)) =>
            lastDragPositions += region -> e.pos
            _dragged += region -> true
          case _ =>
        }
      }
  }

  globalReactions += {
    case e: MouseEvent.Release if e.button == MouseButton.Left =>
      _dragged.find(_._2) match {
        case Some((region, _)) =>
          _dragged += region -> false
          onStopDragging(region, e.pos)
        case _ =>
      }
    case e: MouseEvent.Move =>
      _dragged.find(_._2) match {
        case Some((region, _)) =>
          onDrag(region, e.pos, lastDragPositions(region), e.pos - lastDragPositions(region))
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

  def onStopDragging(region: String, mousePos: Point): Unit = ()

  def canDrag: Boolean

  def dragRegion: Rectangle = Rectangle(Point(0, 0), context.size)

  def controlRegions: Map[String, Rectangle] = Map("full" -> bounds)

  def onDrag(region: String, newPos: Point, lastPos: Point, delta: Point): Unit = {
    this.screen += delta
  }

  def dragged(region: String) = _dragged.get(region) match {
    case Some(d) => d
    case _ => false
  }

  private var lastDragPositions = mutable.Map.empty[String, Point]
  private var _dragged = mutable.Map.empty[String, Boolean]
}
