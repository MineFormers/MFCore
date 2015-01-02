package de.mineformers.core.client.ui.util

import de.mineformers.core.client.ui.util.font.Font
import de.mineformers.core.client.util.Color

trait Shadow {
  def draw(text: String, x: Int, y: Int, z: Int, font: Font, textColor: Color): Unit
}

object Shadow {
  def seq(shadows: Shadow*): Shadow = new SeqImpl(shadows)

  private class SeqImpl(shadows: Seq[Shadow]) extends Shadow {
    override def draw(text: String, x: Int, y: Int, z: Int, font: Font, textColor: Color): Unit = shadows.foreach(_.draw(text, x, y, z, font, textColor))
  }
}

case class SimpleShadow(hOff: Int, vOff: Int, color: Color) extends Shadow {
  override def draw(text: String, x: Int, y: Int, z: Int, font: Font, textColor: Color): Unit = {
    font.draw(text, x + hOff, y + vOff, z, if(color.a == 1) color.integer else textColor.lighten(color.a).integer)
  }
}