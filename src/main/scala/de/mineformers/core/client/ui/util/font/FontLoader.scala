package de.mineformers.core.client.ui.util.font

/*
 * TrueTyper: Open Source TTF implementation for Minecraft.
 * Copyright (C) 2013 - Mr_okushama, Modified by PaleoCrafter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.{Font => JFont}

import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation

object FontLoader {
  def loadSystemFont(name: String, defSize: Float, antialias: Boolean): TrueTypeFont =
    loadSystemFont(name, defSize, antialias, JFont.TRUETYPE_FONT)

  def loadSystemFont(name: String, defSize: Float, antialias: Boolean, `type`: Int): TrueTypeFont = {
    var font: JFont = null
    var out: TrueTypeFont = null
    try {
      font = new JFont(name, `type`, defSize.asInstanceOf[Int])
      font = font.deriveFont(defSize)
      out = new TrueTypeFont(font, antialias)
    }
    catch {
      case e: Exception =>
        e.printStackTrace()
    }
    out
  }

  def createFont(res: ResourceLocation, defSize: Float, antialias: Boolean): TrueTypeFont = createFont(res, defSize, antialias, JFont.TRUETYPE_FONT)

  def createFont(res: ResourceLocation, defSize: Float, antialias: Boolean, `type`: Int): TrueTypeFont = {
    try {
      val font = JFont.createFont(`type`, Minecraft.getMinecraft.getResourceManager.getResource(res).getInputStream) deriveFont defSize
      new TrueTypeFont(font, antialias)
    }
    catch {
      case e: Exception =>
        e.printStackTrace()
        null
    }
  }
}