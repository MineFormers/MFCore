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
package de.mineformers.core.mod

import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import de.mineformers.core.util.ResourceUtils.Resource
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import org.apache.logging.log4j.{LogManager, Logger}

trait MFMod {
  val ModId: String
  val ModName: String
  val ResourcePath: String = ModId.toLowerCase
  lazy val CreativeTab: CreativeTabs = new MFCreativeTab(ModName, tabItem)
  val Log: Logger = LogManager.getLogger(ModId)

  def apply(resource: String) = Resource(ResourcePath, resource)

  def texture(tpe: String, name: String) = this("textures/" + tpe.replace(".", "/") + "/" + name + ".png")

  def icon(name: String) = ResourcePath + ":" + name

  def name(name: String) = ResourcePath + ":" + name

  def tabItem = new ItemStack(net.minecraft.init.Items.apple)

  @EventHandler
  def preInit(event: FMLPreInitializationEvent): Unit

  @EventHandler
  def init(event: FMLInitializationEvent): Unit

  @EventHandler
  def postInit(event: FMLPostInitializationEvent): Unit
}