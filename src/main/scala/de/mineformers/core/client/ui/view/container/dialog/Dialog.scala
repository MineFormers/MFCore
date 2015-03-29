package de.mineformers.core.client.ui.view.container.dialog

import de.mineformers.core.client.ui.layout.{FlowLayout, StackLayout}
import de.mineformers.core.client.ui.proxy.Context
import de.mineformers.core.client.ui.util.ViewEvent.ButtonPressed
import de.mineformers.core.client.ui.view.container.Panel.Padding
import de.mineformers.core.client.ui.view.container.dialog.Dialog.{Buttons, DialogButton, Result}
import de.mineformers.core.client.ui.view.container.{Frame, Panel}
import de.mineformers.core.client.ui.view.interaction.Button
import de.mineformers.core.util.math.shape2d.Size

/**
 * Dialog
 *
 * @author PaleoCrafter
 */
abstract class Dialog[R <: Result](title0: String, size0: Size, buttons0: Int) extends Frame(size0) {
  if (buttons0 == 0)
    throw new IllegalArgumentException("There must be at least one button")
  this.clip = false
  title = title0
  fixed = false
  zIndex = 999
  layout = new StackLayout
  val contentPanel = new Panel
  contentPanel.padding = Padding.None
  contentPanel.maxSize = Size.Max
  val buttonPanel = new Panel
  buttonPanel.padding = Padding.None
  buttonPanel.layout = new FlowLayout(right = true)
  buttonPanel.maxSize = Size.MaxWidth
  buttonPanel.clip = false
  val buttons = Buttons.createButtons(buttons0)
  buttonPanel.size = Size(buttons.foldLeft(0)(_ + _.width) + 2, buttons.head.height)
  listenTo(buttons: _*)
  buttonPanel.addViews(buttons: _*)
  globalReactions += {
    case ButtonPressed(b: DialogButton) =>
      onAction(b.tpe)
  }
  addViews(contentPanel, buttonPanel)
  var modal = false
  protected var callback: R => Unit = r => ()

  def onAction(button: Int): Unit

  def display(ctx: Context, callback: R => Unit = r => ()): Unit = {
    this.init(ctx, ctx)
    sizeUpdate = false
    ctx.focused = null
    ctx.addFrames((if (modal) Seq(new Dialog.DialogContainer(ctx), this) else Seq(this)): _*)
    this.callback = callback
  }
}

object Dialog {
  def simple(title: String, message: String, buttons: Int): Dialog[SimpleDialogResult] = new SimpleDialog(title, message, buttons)

  def confirm(title: String, message: String): Dialog[SimpleDialogResult] = new SimpleDialog(title, message, Buttons.YesNo)

  trait Result {
    def clickedButton: Int
  }

  class DialogButton(text: String, val tpe: Int) extends Button(text) {
    def isYes = tpe == Buttons.Yes

    def isNo = tpe == Buttons.No

    def isOkay = tpe == Buttons.Okay

    def isCancel = tpe == Buttons.Cancel
  }

  private class DialogContainer(ctx: Context) extends Frame(ctx.size) {
    this.init(ctx, ctx)
    hasBackground = false
  }

  object Buttons {
    final val Yes = 1
    final val No = 1 << 1
    final val Okay = 1 << 2
    final val Cancel = 1 << 3
    final val YesNo = Yes | No
    final val YesNoCancel = Yes | No | Cancel
    final val OkayCancel = Okay | Cancel

    def createButtonsFrom(buttons: Int*) = createButtons(from(buttons: _*))

    def from(buttons: Int*): Int = buttons.reduce(_ | _)

    def createButtons(buttons: Int): Seq[DialogButton] = {
      val builder = Seq.newBuilder[DialogButton]
      if (hasYes(buttons))
        builder += new DialogButton("Yes", Yes)
      if (hasNo(buttons))
        builder += new DialogButton("No", No)
      if (hasOkay(buttons))
        builder += new DialogButton("Okay", Okay)
      if (hasCancel(buttons))
        builder += new DialogButton("Cancel", Cancel)
      builder.result()
    }

    def hasYes(buttons: Int): Boolean = (buttons & Yes) == Yes

    def hasNo(buttons: Int): Boolean = (buttons & No) == No

    def hasOkay(buttons: Int): Boolean = (buttons & Okay) == Okay

    def hasCancel(buttons: Int): Boolean = (buttons & Cancel) == Cancel

    def has(buttons: Int, button: Int) = (buttons & button) == button
  }

}