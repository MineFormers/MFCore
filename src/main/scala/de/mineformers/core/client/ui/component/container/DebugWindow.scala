package de.mineformers.core.client.ui.component.container

import de.mineformers.core.util.math.shape2d.{Point, Size}
import de.mineformers.core.client.ui.component.View
import de.mineformers.core.client.ui.component.container.Frame.Anchor
import de.mineformers.core.client.ui.component.interaction.CheckBox
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
  val showOverlay = new CheckBox("Show component bounds on hover")

  add(showOverlay)

  override def update(mousePos: Point): Unit = {
    super.update(mousePos)
    hoveredComponent = context.findHoveredComponent(mousePos)
  }

  var hoveredComponent: View = _

  class DebugWindowSkin extends FrameSkin {
    override def drawForeground(mousePos: Point): Unit = {
      super.drawForeground(mousePos)
      if(hoveredComponent != null && showOverlay.checked)
      {
        GuiUtils.drawRectangle(0x0099FF, 0.2F, hoveredComponent.screen.x, hoveredComponent.screen.y, 500, hoveredComponent.width, hoveredComponent.height)
      }
    }
  }
}
