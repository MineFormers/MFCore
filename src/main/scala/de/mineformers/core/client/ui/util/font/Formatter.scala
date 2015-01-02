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

object Formatter {
  def getFormatted(c: Char): Array[Float] = {
    (c match {
      case '0' =>
        Array(0, 0, 0, 0, 255)
      case '1' =>
        Array(0, 0, 170, 255)
      case '2' =>
        Array(0, 170, 0, 255)
      case '3' =>
        Array(0, 170, 170, 255)
      case '4' =>
        Array(170, 0, 0, 255)
      case '5' =>
        Array(170, 0, 170, 255)
      case '6' =>
        Array(255, 170, 0, 255)
      case '7' =>
        Array(170, 170, 170, 255)
      case '8' =>
        Array(85, 85, 85, 255)
      case '9' =>
        Array(85, 85, 255, 255)
      case 'a' =>
        Array(85, 255, 85, 255)
      case 'b' =>
        Array(85, 255, 255, 255)
      case 'c' =>
        Array(255, 85, 85, 255)
      case 'd' =>
        Array(85, 255, 255, 255)
      case 'e' =>
        Array(255, 255, 85, 255)
      case 'f' =>
        Array(255, 255, 255, 255)
      case _ =>
        Array(255, 255, 255, 255)
    }) map (_.toFloat / 255F)
  }
}