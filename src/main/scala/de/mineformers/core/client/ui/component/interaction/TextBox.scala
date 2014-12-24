/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 MineFormers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.mineformers.core.client.ui.component.interaction

import java.text.DecimalFormat

import de.mineformers.core.client.shape2d.{Point, Size}
import de.mineformers.core.client.ui.component.{Focusable, TextComponent}
import de.mineformers.core.client.ui.proxy.Context
import de.mineformers.core.client.ui.util.ComponentEvent.ValueChanged
import de.mineformers.core.client.ui.util._
import de.mineformers.core.reaction.Publisher
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.opengl.GL11

/**
 * TextBox
 *
 * @author PaleoCrafter
 */
class TextBox(initText: String, initSize: Size, var font: Font = Font.DefaultLightShadow) extends TextComponent with Focusable {
  /**
   * Current text of this <code>UITextField</code>
   */
  private var sb: StringBuilder = new StringBuilder
  /**
   * Current cursor position.
   */
  private var cursorPosition: Int = 0
  /**
   * Current selection cursor position. If -1, no selection is active.
   */
  private var selectionPosition: Int = -1
  /**
   * Number of characters offset out of this <code>UITextField</code> when drawn.
   */
  private var charOffset: Int = 0
  /**
   * Cursor blink timer
   */
  private var startTimer: Long = 0L
  private var _formatter: (String, String) => String = (old, s) => s
  var canLoseFocus = true
  sb ++= formatter("", initText)
  this.size = initSize
  skin = new TextBoxSkin
  private var _textOff = Point(2, (size.height - font.height) / 2)

  override def init(channel: Publisher, context: Context): Unit = {
    super.init(channel, context)
  }

  override def textOff = _textOff

  def textOff_=(off: Point): Unit = _textOff = off

  /**
   * Clamps the position <i>pos</i> between 0 and text length
   *
   * @param pos
   * @return position clamped
   */
  private def clamp(pos: Int): Int = Math.max(0, Math.min(pos, sb.length))

  /**
   * Determines the cursor position for a given x coordinate
   *
   * @param x
   * @return position
   */
  private def cursorPositionFromX(x: Int): Int = {
    var pos: Int = 0
    var width: Int = 0
    for (i <- charOffset until sb.length) {
      val w: Int = font.charWidth(sb.charAt(i))
      if (width + (w.asInstanceOf[Float] / 2) > x) return pos
      width += w
      pos += 1
    }
    pos
  }

  /**
   * Adds text at current cursor position. If some text is selected, it's deleted first.
   */
  def addText(text: String): Unit = {
    if (selectionPosition != -1) deleteSelectedText()
    val old = this.text
    this.sb.insert(cursorPosition, text)
    val newText = this.text
    val cursor = cursorPos
    this.sb.clear()
    this.sb.append(formatter(old, newText))
    publish(ValueChanged(this, old, this.text))
    cursorPos = cursor + text.length
  }

  private def textWidth(start: Int, end: Int): Int = {
    if (end <= start) return 0
    font.width(sb.substring(clamp(start), clamp(end)))
  }

  /**
   * @return the text of this <code>UITextField</code>.
   */
  def text: String = sb.mkString

  /**
   * Sets the text of this <code>UITextField</code> and place the cursor at the end.
   */
  def text_=(text: String): Unit = {
    setText(text)
  }

  def setText(text: String, notify: Boolean = true) {
    val old = this.text
    this.sb.setLength(0)
    this.sb.append(formatter(this.text, text))
    if (notify)
      publish(ValueChanged(this, old, this.text))
    removeSelection()
    this.jumpToEnd()
  }

  def formatter = _formatter

  def formatter_=(formatter: (String, String) => String): Unit = {
    text = formatter(text, text)
    _formatter = formatter
  }

  /**
   * @return the current position of the cursor.
   */
  def cursorPos: Int = this.cursorPosition

  /**
   * Sets the position of the cursor to the provided index.
   *
   * @param position
   */
  def cursorPos_=(position: Int): Unit = {
    this.cursorPosition = clamp(position)
    while (textWidth(charOffset, cursorPosition) > width - 3)
      charOffset += 1
    if (charOffset > cursorPosition) charOffset = position
    startTimer = System.currentTimeMillis
  }

  /**
   * @return the text currently selected.
   */
  def selectedText: String = {
    if (selectionPosition == -1) return ""
    val start: Int = Math.min(selectionPosition, cursorPosition)
    val end: Int = Math.max(selectionPosition, cursorPosition)
    this.sb.substring(start, end)
  }

  /**
   * Deletes the text currently selected
   */
  def deleteSelectedText(): Unit = {
    val old = this.text
    val start: Int = Math.min(selectionPosition, cursorPosition)
    val end: Int = Math.max(selectionPosition, cursorPosition)
    this.sb.delete(start, end)
    val newText = this.text
    publish(ValueChanged(this, old, newText))
    removeSelection()
    cursorPos = start
  }

  /**
   * Clear the text selection
   */
  def removeSelection(): Unit = {
    selectionPosition = -1
  }

  /**
   * Deletes the selected text, otherwise deletes characters from either side of the cursor. params: delete num
   */
  def deleteFromCursor(amount: Int): Unit = {
    if (sb.length == 0) return
    if (selectionPosition == -1) selectionPosition = cursorPosition + amount
    deleteSelectedText()
  }

  /**
   * Deletes the specified number of words starting at the cursor position. Negative numbers will delete words left of the cursor.
   */
  def deleteWords(amount: Int): Unit = {
    this.deleteFromCursor(nextSpacePosition(amount < 0))
  }

  def nextSpacePosition(backwards: Boolean): Int = {
    var pos: Int = cursorPosition + (if (backwards) -1 else 1)
    if (pos < 0 || pos > sb.length) return 0
    if (sb.charAt(pos) == ' ')
      pos -= 1
    while (pos > 0 && pos < sb.length) {
      if (sb.charAt(pos) == ' ') return pos + 1 - cursorPosition
      pos += (if (backwards) -1 else 1)
    }
    pos - cursorPosition
  }

  /**
   * Moves the text cursor by a specified number of characters and clears the selection
   */
  def moveCursorBy(amount: Int): Unit = {
    if (GuiScreen.isShiftKeyDown) selection = cursorPosition
    else removeSelection()
    this.cursorPos = this.cursorPosition + amount
  }

  /**
   * sets the cursors position to the beginning
   */
  def jumpToBegining(): Unit = {
    if (GuiScreen.isShiftKeyDown) selection = cursorPosition
    else removeSelection()
    this.cursorPos = 0
  }

  /**
   * sets the cursors position to after the text
   */
  def jumpToEnd(): Unit = {
    if (GuiScreen.isShiftKeyDown) selection = cursorPosition
    else removeSelection()
    this.cursorPos = this.sb.length
  }

  def selection: Int = selectionPosition

  /**
   * Starts text selection. The selection anchor is set to current cursor position, and the cursor is moved to the new position.
   */
  def selection_=(pos: Int): Unit = {
    if (selectionPosition == -1) selectionPosition = cursorPosition
    cursorPos = pos
  }

  override def gainFocus(): Unit = {
  }

  override def loseFocus(): Unit = {
  }

  override def update(mousePos: Point): Unit = {
  }

  reactions += {
    case e: MouseEvent.Click =>
      if (hovered(e.pos))
        if (e.button == MouseButton.Left) {
          val pos = cursorPositionFromX(local(e.pos).x)
          if (GuiScreen.isShiftKeyDown)
            selection = pos
          else {
            removeSelection()
            cursorPos = pos
          }
        } else if (e.button == MouseButton.Right) {
          this.text = ""
          this.cursorPosition = 0
        }
    case e: MouseEvent.Drag =>
      if (focused && e.lastButton == MouseButton.Left) {
        val pos = cursorPositionFromX(local(e.pos).x)
        selection = pos
      }
    case KeyEvent.Type(char, code) =>
      if (focused && enabled) {
        import de.mineformers.core.client.ui.component.interaction.TextBox._
        import org.lwjgl.input.Keyboard._
        if (code == KEY_ESCAPE) {
          if (!canLoseFocus)
            context.close()
          else {
            this.noFocus()
          }
        } else {
          val ctrlDown = GuiScreen.isCtrlKeyDown
          def nativeActions(char: Char): Boolean = char match {
            case SelectAll =>
              this.cursorPos = 0
              this.selection = sb.length
              true
            case Copy =>
              GuiScreen.setClipboardString(selectedText)
              true
            case Paste =>
              addText(GuiScreen.getClipboardString)
              true
            case Cut =>
              GuiScreen.setClipboardString(selectedText)
              addText("")
              true
            case _ => false
          }
          if (!nativeActions(char)) {
            code match {
              case KEY_BACK =>
                if (ctrlDown)
                  this.deleteWords(-1)
                else
                  this.deleteFromCursor(-1)
              case KEY_DELETE =>
                if (ctrlDown)
                  deleteWords(1)
                else
                  deleteFromCursor(1)
              case KEY_HOME =>
                jumpToBegining()
              case KEY_END =>
                jumpToEnd()
              case KEY_LEFT =>
                if (ctrlDown)
                  moveCursorBy(nextSpacePosition(backwards = true))
                else
                  moveCursorBy(-1)
              case KEY_RIGHT =>
                if (ctrlDown)
                  moveCursorBy(nextSpacePosition(backwards = false))
                else
                  moveCursorBy(1)
              case _ =>
                if (ChatAllowedCharacters.isAllowedCharacter(char))
                  addText(char.toString)
            }
          }
        }
      }
  }
  var maxTextLength = 32

  class TextBoxSkin extends TextSkin {
    override def drawForeground(mousePos: Point): Unit = {
      if (text.length != 0) drawText()
      if (selectionPosition != -1 && selectionPosition != cursorPosition) drawSelectionBox()
      if (focused) drawCursor()
    }

    def drawText() {
      var end: Int = text.length
      while (textWidth(charOffset, end) > width - textOff.x * 2)
        end -= 1
      font.draw(text.substring(charOffset, end), screen.x + textOff.x + 1, screen.y + textOff.y + 1, 0, if (!enabled) 0x707070 else font.color)
    }

    def drawCursor() {
      val elapsedTime: Long = startTimer - System.currentTimeMillis
      if ((elapsedTime / 500) % 2 != 0) return
      val offset: Int = textWidth(charOffset, cursorPosition)
      utils.drawRectangle(0xD0D0D0, screen.x + offset + textOff.x, screen.y + textOff.y, 0, 1, 10)
    }

    def drawSelectionBox() {
      GL11.glEnable(GL11.GL_COLOR_LOGIC_OP)
      GL11.glLogicOp(GL11.GL_OR_REVERSE)
      var start: Int = Math.max(Math.min(cursorPosition, selectionPosition), charOffset)
      var width: Int = textWidth(start, Math.max(cursorPosition, selectionPosition))
      start = textWidth(charOffset, start)
      width = Math.min(component.width - start - textOff.x * 2, width)
      utils.drawRectangle(0x0000FF, screen.x + start + textOff.x, screen.y + textOff.y, 0, width, 10)
      GL11.glDisable(GL11.GL_COLOR_LOGIC_OP)
    }
  }

}

object TextBox {
  final val SelectAll: Char = 1
  final val Copy: Char = 3
  final val Paste: Char = 22
  final val Cut: Char = 24
  final val IntegerFormatter = (old: String, s: String) => s.replaceAll("[^0-9]", "")

  class NumberFormatter(format: DecimalFormat) extends ((String, String) => String) {
    override def apply(old: String, s: String): String = {
      try {
        val formatted = format.parse(s)
        if (formatted != null)
          format.format(formatted.doubleValue())
        else IntegerFormatter(old, s)
      } catch {
        case e: Exception =>
          IntegerFormatter(old, s)
      }
    }
  }

}