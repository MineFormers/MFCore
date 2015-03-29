package de.mineformers.core.client.ui.view.container

import de.mineformers.core.client.ui.view.View
import de.mineformers.core.client.ui.view.container.Frame.Anchor
import de.mineformers.core.client.ui.view.interaction.CheckBox
import de.mineformers.core.util.math.shape2d.{Point, Size}
import de.mineformers.core.util.renderer.GuiUtils

/**
 * DebugWindow
 *
 * @author PaleoCrafter
 */
class DebugWindow extends Frame(Size(200, 50)) {
  title = "Debug"
  fixed = false
  resizable = false
  anchor = Anchor.BottomRight
  skin = new DebugWindowSkin
  val showOverlay = new CheckBox("Show view bounds on hover")

  add(showOverlay)

  override def update(mousePos: Point): Unit = {
    super.update(mousePos)
    hoveredView = context.findHoveredView(mousePos)
  }

  var hoveredView: View = _

  class DebugWindowSkin extends FrameSkin {
    override def drawForeground(mousePos: Point): Unit = {
      super.drawForeground(mousePos)
      if (hoveredView != null && showOverlay.checked) {
        GuiUtils.drawRectangle(0x0099FF, 0.2F, hoveredView.screen.x, hoveredView.screen.y, 1000, hoveredView.width, hoveredView.height)
        hoveredView match {
          case p: Panel =>
            GuiUtils.drawRectangle(0xFF0000, 0.2F, hoveredView.screen.x, hoveredView.screen.y, 1000, p.padding.left, hoveredView.height)
            GuiUtils.drawRectangle(0xFF0000, 0.2F, hoveredView.screen.x + hoveredView.width - p.padding.right, hoveredView.screen.y, 1000, p.padding.right, hoveredView.height)
            GuiUtils.drawRectangle(0xFF0000, 0.2F, hoveredView.screen.x + p.padding.left, hoveredView.screen.y, 1000, hoveredView.width - p.padding.left - p.padding.right, p.padding.top)
            GuiUtils.drawRectangle(0xFF0000, 0.2F, hoveredView.screen.x + p.padding.left, hoveredView.screen.y + hoveredView.height - p.padding.bottom, 1000, hoveredView.width - p.padding.left - p.padding.right, p.padding.bottom)
          case _ =>
        }
      }
    }
  }

}
