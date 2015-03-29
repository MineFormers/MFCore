package de.mineformers.core.client.ui.view.container.dialog

import de.mineformers.core.client.ui.view.decoration.Label
import de.mineformers.core.util.math.shape2d.Size

/**
 * SimpleDialog
 *
 * @author PaleoCrafter
 */
class SimpleDialog(title0: String, message: String, buttons0: Int) extends Dialog[SimpleDialogResult](title0, Size(0, 0), buttons0) {
  val messageLabel = new Label(message)
  contentPanel.add(messageLabel)
  size = Size(buttonPanel.contentSize().width + 10, 0)
  if(messageLabel.width > buttonPanel.width)
    size = Size(messageLabel.width, 0)

  override def onAction(button: Int): Unit = callback(new SimpleDialogResult(button))
}

class SimpleDialogResult(val clickedButton: Int) extends Dialog.Result
