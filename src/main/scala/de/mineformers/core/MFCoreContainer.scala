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
package de.mineformers.core

import cpw.mods.fml.common.{LoadController, ModMetadata, DummyModContainer}
import com.google.common.collect.ImmutableList
import com.google.common.eventbus.{Subscribe, EventBus}
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPreInitializationEvent}
import java.io.File
import cpw.mods.fml.client.{FMLFileResourcePack, FMLFolderResourcePack}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import de.mineformers.core.network.{TileDescriptionMessage, MFNetworkWrapper}
import de.mineformers.core.registry.SharedBlockRegistry
import de.mineformers.core.tileentity.{TileDescription, Describable}
import net.minecraft.client.Minecraft
import de.mineformers.core.client.ui.skin.{TextureLoader, GuiMetadataSectionDeserializer, GuiMetadataSection}
import de.mineformers.core.util.renderer.GuiUtils
import net.minecraft.client.resources.SimpleReloadableResourceManager

/**
 * MFCoreContainer
 *
 * @author PaleoCrafter
 */
class MFCoreContainer extends DummyModContainer(new ModMetadata) {
  val meta = getMetadata
  meta.modId = "mfcore"
  meta.name = "MFCore"
  meta.authorList = ImmutableList.of("PaleoCrafter")
  meta.version = "0.3.0"
  meta.autogenerated = false

  /**
   * Register the container to the EventBus to allow the use of lifecycle events
   * @param bus the EventBus to register to
   * @param controller ???
   * @return true, if a new element was registered
   */
  override def registerBus(bus: EventBus, controller: LoadController): Boolean = {
    bus.register(this)
    true
  }

  /**
   * Fired on pre init
   *
   * @param event the pre init event
   */
  @Subscribe
  def preInit(event: FMLPreInitializationEvent): Unit = {
    MFCore.net = new MFNetworkWrapper("MFCore")
    MFCore.net.register[TileDescriptionMessage]()
    Proxy.preInit(event)
    Proxy.getClass.getDeclaredMethods
  }

  @Subscribe
  def init(event: FMLInitializationEvent): Unit = {
    Proxy.init(event)
  }

  override def getSource: File = MFCore.CoreModLocation

  override def getCustomResourcePackClass: Class[_] =
    if (MFCore.CoreModLocation.isDirectory) classOf[FMLFolderResourcePack] else classOf[FMLFileResourcePack]
}

object Proxy {
  def preInit(event: FMLPreInitializationEvent): Unit = {
    if (event.getSide.isClient)
      clientPreInit()
  }

  def init(event: FMLInitializationEvent): Unit = {
    if (event.getSide.isClient)
      clientInit()
  }

  @SideOnly(Side.CLIENT)
  def clientPreInit(): Unit = {
    MFCore.net.addHandler({
      case (TileDescriptionMessage(x, y, z, desc), ctx) =>
        val tile = Minecraft.getMinecraft.theWorld.getTileEntity(x, y, z)
        tile match {
          case d: Describable =>
            desc.writeParent(d)
            d.onDescription()
          case _ =>
        }
        null
    }, Side.CLIENT)
    Minecraft.getMinecraft.getResourcePackRepository.rprMetadataSerializer.registerMetadataSectionType(new GuiMetadataSectionDeserializer(), classOf[GuiMetadataSection])
    GuiUtils.init()
    Minecraft.getMinecraft.getResourceManager.asInstanceOf[SimpleReloadableResourceManager].registerReloadListener(new TextureLoader)
  }

  @SideOnly(Side.CLIENT)
  def clientInit(): Unit = {
    SharedBlockRegistry.registerRenderers()
  }
}