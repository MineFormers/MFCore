package de.mineformers.core.client.ui.view.interaction

import de.mineformers.core.util.math.shape2d.{Point, Rectangle, Size}
import de.mineformers.core.client.ui.view.View
import de.mineformers.core.client.ui.view.decoration.Label
import de.mineformers.core.client.ui.state.{BooleanProperty, ViewState, Property}
import de.mineformers.core.client.ui.util.ViewEvent.{ViewClicked, ValueChanged}

/**
 * CheckBox
 *
 * @author PaleoCrafter
 */
class CheckBox(text: String) extends View {
  private val slaveLabel = new Label(text)

  size = Size(14 + slaveLabel.width + 3, 14)

  globalReactions += {
    case e: ViewClicked if e.v eq this =>
      if (e.mousePos.x < 14) {
        checked = !checked
        publish(ValueChanged(this, !checked, checked))
      }
  }

  override def updateState(mousePos: Point): Unit = {
    state.set(Property.Hovered, boxHovered(mousePos))
  }

  def boxHovered(mousePosition: Point): Boolean = {
    if (parent != null && parent.clip) (Rectangle(screen, 14, 14) contains mousePosition) && parent.hovered(mousePosition) && (parent.screenPaddingBounds contains mousePosition) else Rectangle(screen, 14, 14) contains mousePosition
  }

  def checked = state(CheckBox.PropertyChecked)

  def checked_=(checked: Boolean) = state.set(CheckBox.PropertyChecked, checked)

  override def defaultState(state: ViewState): Unit = super.defaultState(state.set(CheckBox.PropertyChecked, false))

  override def update(mousePos: Point): Unit = {
    slaveLabel.screen = screen + Point(17, (14 - slaveLabel.height) / 2 + 1)
  }

  override var skin: Skin = new CheckBoxSkin

  class CheckBoxSkin extends Skin {
    stretchStatic = false

    override protected def drawForeground(mousePos: Point): Unit = {
      slaveLabel.skin.draw(mousePos)
    }
  }

}

object CheckBox {
  final val PropertyChecked = new BooleanProperty("checked")
}
