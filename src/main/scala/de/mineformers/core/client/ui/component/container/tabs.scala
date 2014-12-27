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
package de.mineformers.core.client.ui.component.container

import de.mineformers.core.client.shape2d.{Point, Size}
import de.mineformers.core.client.ui.component.Component
import de.mineformers.core.client.ui.component.container.Panel.Padding
import de.mineformers.core.client.ui.component.container.Tab.Orientation
import de.mineformers.core.client.ui.component.container.Tab.Orientation.Orientation
import de.mineformers.core.client.ui.layout.StackLayout
import de.mineformers.core.client.ui.proxy.Context
import de.mineformers.core.client.ui.skin.drawable.Drawable
import de.mineformers.core.client.ui.state.{BooleanProperty, ComponentState}
import de.mineformers.core.client.ui.util.ComponentEvent.ComponentClicked
import de.mineformers.core.client.ui.util.{MouseButton, MouseEvent}
import de.mineformers.core.reaction.Publisher

import scala.collection.mutable

/**
 * TabbedFrame
 *
 * @author PaleoCrafter
 */
class TabbedFrame(size0: Size, orientation: Orientation = Orientation.Top) extends Frame(size0) {
  private val tabPanel = new Panel
  tabPanel.padding = Padding.None
  tabPanel.layout = new StackLayout(horizontal = !orientation.vertical)
  private val tabs = mutable.LinkedHashMap.empty[String, Tab]
  private var _active: String = _
  private var channel: Publisher = _
  skin = new TabFrameSkin

  reactions += {
    case ComponentClicked(c, button) =>
      c match {
        case tab: Tab =>
          active = tab.key
        case _ =>
      }
  }

  override def init(channel: Publisher, context: Context): Unit = {
    super.init(channel, context)
    this.channel = channel
    tabPanel.init(channel, context)
    tabPanel.position = orientation match {
      case Orientation.Left => Point(-28, 0)
      case Orientation.Right => Point(width - 4, 0)
      case Orientation.Top => Point(0, -28)
      case Orientation.Bottom => Point(0, height - 4)
    }
    tabPanel.screen = screen + tabPanel.position
  }

  override def update(mousePos: Point): Unit = {
    super.update(mousePos)
    tabPanel.position = orientation match {
      case Orientation.Left => Point(-28, 0)
      case Orientation.Right => Point(width - 4, 0)
      case Orientation.Top => Point(0, -28)
      case Orientation.Bottom => Point(0, height - 4)
    }
    tabPanel.screen = screen + tabPanel.position
    tabPanel.update(mousePos)
  }

  def activeTab = tabs.getOrElse(active, null)

  def active = _active

  def active_=(key: String): Unit = {
    if (tabs contains key) {
      val tab = activeTab
      if (tab != null && tab.panel != null) {
        tab.panel.deafTo(channel)
        tab.enabled = false
      }
      _active = key
      content = Seq(tabs(key).panel)
      activeTab.enabled = true
    }
  }

  def addTab(key: String, title: String, icon: Drawable, panel: Panel): Unit = {
    val tab = Tab(key, title, icon, panel, tabs.isEmpty)
    tab.enabled = false
    listenTo(tab)
    tabs += key -> tab
    tabPanel.add(tab)
    if (active == null)
      active = key
  }

  case class Tab(key: String, title: String, icon: Drawable, panel: Panel, first: Boolean = false) extends Component {
    reactions += {
      case MouseEvent.Click(p, code) => if (hovered(p) && visible) context.publish(ComponentClicked(this, MouseButton(code)))
    }

    size = if (orientation.vertical) Size(32, 28) else Size(28, 32)
    tooltip = title
    identifier = orientation.name

    override def defaultState(state: ComponentState): Unit = state.set(de.mineformers.core.client.ui.component.container.Tab.FirstProperty, first)

    override def update(mousePos: Point): Unit = ()

    override var skin: Skin = new TabSkin

    class TabSkin extends Skin {
      override protected def drawForeground(mousePos: Point): Unit = {
        icon.draw(mousePos, screen + Point(6 + (if (orientation.vertical) 2 else 0), 6 + (if (!orientation.vertical) 2 else 0)), zIndex)
      }
    }

  }

  class TabFrameSkin extends PanelSkin {
    override def drawForeground(mousePos: Point): Unit = {
      super.drawForeground(mousePos)
      tabPanel.skin.draw(mousePos)
    }
  }

}

object Tab {

  final val FirstProperty = new BooleanProperty("first")

  object Orientation extends Enumeration {
    type Orientation = OrientationVal
    final val Left = Value("tabLeft", vertical = true)
    final val Right = Value("tabRight", vertical = true)
    final val Top = Value("tabTop", vertical = false)
    final val Bottom = Value("tabBottom", vertical = false)

    class OrientationVal(val name: String, val vertical: Boolean) extends Val(nextId, name)

    protected final def Value(name: String, vertical: Boolean) = new OrientationVal(name, vertical)
  }

}
