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
package de.mineformers.core.client.ui.view.container

import de.mineformers.core.client.ui.layout.StackLayout
import de.mineformers.core.client.ui.proxy.Context
import de.mineformers.core.client.ui.skin.drawable.{DrawableTexture, StaticTexture, Drawable}
import de.mineformers.core.client.ui.state.{BooleanProperty, Property, StringProperty, ViewState}
import de.mineformers.core.client.ui.util.SimpleShadow
import de.mineformers.core.client.ui.util.ViewEvent.ViewClicked
import de.mineformers.core.client.ui.view.View
import de.mineformers.core.client.ui.view.container.Panel.Padding
import de.mineformers.core.client.ui.view.container.Tab.Orientation
import de.mineformers.core.client.ui.view.container.Tab.Orientation.Orientation
import de.mineformers.core.client.ui.view.decoration.Label
import de.mineformers.core.client.util.Color
import de.mineformers.core.reaction.GlobalPublisher
import de.mineformers.core.util.math.shape2d.{Point, Size}

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
  private var channel: GlobalPublisher = _
  skin = new TabFrameSkin
  tabPanel.clip = false
  clip = false

  globalReactions += {
    case ViewClicked(c, pos, button) =>
      c match {
        case tab: Tab =>
          active = tab.key
        case _ =>
      }
  }

  override def init(channel: GlobalPublisher, context: Context): Unit = {
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
        tab.enabled = true
      }
      _active = key
      content = Seq(activeTab.panel)
      if (channel != null) {
        activeTab.panel.parent = this
        activeTab.panel.init(channel, context)
      }
      sizeUpdate = false
      activeTab.enabled = false
    }
  }

  def addTab(key: String, title: String, icon: Drawable, panel: Panel): Unit = {
    val tab = Tab(key, title, icon, panel, tabs.isEmpty)
    tab.enabled = true
    listenTo(tab)
    tabs += key -> tab
    tabPanel.add(tab)
    if (active == null)
      active = key
  }

  override def findView(mousePos: Point, predicate: (View) => Boolean): View = super.findView(mousePos, predicate) match {
    case null =>
      tabPanel.findView(mousePos, predicate)
    case v =>
      v
  }

  case class Tab(key: String, title: String, icon: Drawable, panel: Panel, first: Boolean = false) extends View {
    size = if (orientation.vertical) Size(32, 28) else Size(28, 32)
    tooltip = title

    import de.mineformers.core.client.ui.view.container.{Tab => TabO}

    override def defaultState(state: ViewState): Unit = {
      super.defaultState(state.set(TabO.FirstProperty, first)
        .set(TabO.OrientationProperty, orientation.name)
        .set(TabO.TypeProperty, "frame"))
    }

    override def update(mousePos: Point): Unit = ()

    override var skin: Skin = new TabSkin

    override def hovered(mousePosition: Point): Boolean = {
      screenBounds contains mousePosition
    }

    class TabSkin extends Skin {
      override protected def drawForeground(mousePos: Point): Unit = {
        icon match {
          case t: DrawableTexture =>
            t.size = t.textureSize
          case _ =>
            icon.size = size
        }
        icon.draw(mousePos, screen + Point(6 + (if (orientation.vertical) 2 else 0), 6 + (if (!orientation.vertical) 2 else 0)), zIndex + 1)
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

class TabbedPanel(orientation: Orientation = Orientation.Top) extends Panel {
  padding = Padding.None
  private val tabPanel = new Panel
  tabPanel.padding = Padding.None
  tabPanel.layout = new StackLayout(horizontal = !orientation.vertical)
  private val tabContainer = new Panel
  tabContainer.padding = Padding(1, 0, 1, 1)
  tabContainer.maxSize = Size(Integer.MAX_VALUE, Integer.MAX_VALUE)
  this.layout = new StackLayout(gap = 0, horizontal = orientation.vertical)
  tabPanel.size = Size(12, 12)
  tabPanel.maxSize = Size(if (!orientation.vertical) Integer.MAX_VALUE else 12, if (orientation.vertical) Integer.MAX_VALUE else 12)
  if (orientation == Orientation.Top || orientation == Orientation.Left)
    addViews(tabPanel, tabContainer)
  else
    addViews(tabContainer, tabPanel)
  private val tabs = mutable.LinkedHashMap.empty[String, Tab]
  private var _active: String = _
  private var channel: GlobalPublisher = _

  globalReactions += {
    case ViewClicked(c, pos, button) =>
      c match {
        case tab: Tab =>
          active = tab.key
        case _ =>
      }
  }

  override def init(channel: GlobalPublisher, context: Context): Unit = {
    super.init(channel, context)
    this.channel = channel
  }

  def activeTab = tabs.getOrElse(active, null)

  def active = _active

  def active_=(key: String): Unit = {
    if (tabs contains key) {
      val tab = activeTab
      if (tab != null && tab.panel != null) {
        tab.panel.deafTo(channel)
        tab.enabled = true
      }
      _active = key
      tabContainer.content = Seq(activeTab.panel)
      if (channel != null) {
        activeTab.panel.parent = this
        activeTab.panel.init(channel, context)
      }
      sizeUpdate = false
      layout.reset()
      activeTab.enabled = false
    }
  }

  def addTab(key: String, title: String, panel: Panel): Unit = {
    val tab = Tab(key, title, panel, tabs.isEmpty)
    tab.enabled = true
    listenTo(tab)
    tabs += key -> tab
    tabPanel.add(tab)
    if (active == null)
      active = key
  }

  case class Tab(key: String, title: String, panel: Panel, first: Boolean = false) extends Label(title) {
    shadow = SimpleShadow(0, 1, Color.black(0.3f))
    padding = Padding(2, 2, 2, 1)

    import de.mineformers.core.client.ui.view.container.{Tab => TabO}

    override def updateState(mousePos: Point): Unit = {
      super.updateState(mousePos)
      if (enabled && !state(Property.Hovered))
        shadow = SimpleShadow(0, 1, Color.black(0.2f))
      else
        shadow = SimpleShadow(0, 1, Color.black(0.3f))
    }

    override def defaultState(state: ViewState): Unit = super.defaultState(state.set(TabO.FirstProperty, first).set(TabO.OrientationProperty, orientation.name).set(TabO.TypeProperty, "panel"))
  }

}

object Tab {
  final val TypeProperty = new StringProperty("type", "frame", allowedValues = Seq("frame", "panel"))
  final val FirstProperty = new BooleanProperty("first")
  final val OrientationProperty = new StringProperty("orientation", "left", Orientation.values.map(_.toString).toSeq)

  object Orientation extends Enumeration {
    type Orientation = OrientationVal
    final val Left = Value("left", vertical = true)
    final val Right = Value("right", vertical = true)
    final val Top = Value("top", vertical = false)
    final val Bottom = Value("bottom", vertical = false)

    class OrientationVal(val name: String, val vertical: Boolean) extends Val(nextId, name) {
      override def toString(): String = name
    }

    protected final def Value(name: String, vertical: Boolean) = new OrientationVal(name, vertical)
  }

}
