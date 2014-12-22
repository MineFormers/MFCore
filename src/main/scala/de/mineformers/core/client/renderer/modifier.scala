package de.mineformers.core.client.renderer

import de.mineformers.core.client.util.Color
import org.lwjgl.opengl.GL11._

class Modifier(val apply: RenderContext => Unit, val unapply: () => Unit, val bit: Int)

class Blending(src: Int = GL_SRC_ALPHA, dest: Int = GL_ONE_MINUS_SRC_ALPHA) extends Modifier(
  c => {
    glEnable(GL_BLEND)
    glBlendFunc(src, dest)
  }, () => {
    glDisable(GL_BLEND)
  }, GL_COLOR_BUFFER_BIT)

class Coloring(color: RenderContext => Color) extends Modifier(
  context => {
    val c = color(context)
    glColor4f(c.r, c.g, c.b, c.a)
  }, () => {
    glColor4f(1, 1, 1, 1)
  }, GL_CURRENT_BIT) {
  def this(color: => Color) = this(c => color)
}

class GLToggle(properties: List[Int], bit: Int) extends Modifier(
  c => {
    properties foreach {
      p =>
        if (p > 0) glEnable(p) else glDisable(-p)
    }
  }, () => {
    properties foreach {
      p =>
        if (p < 0) glEnable(-p) else glDisable(p)
    }
  }, bit)