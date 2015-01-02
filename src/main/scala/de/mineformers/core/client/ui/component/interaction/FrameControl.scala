package de.mineformers.core.client.ui.component.interaction

import de.mineformers.core.util.math.shape2d.Size
import de.mineformers.core.client.ui.util.ComponentEvent.ButtonPressed

/**
 * FrameControl
 *
 * @author PaleoCrafter
 */
class FrameControl(_name: String, action: () => Unit) extends Button("", Size(9, 9)) {
  name = _name
  globalReactions += {
    case ButtonPressed(btn) =>
      if(btn eq this)
        action()
  }
}