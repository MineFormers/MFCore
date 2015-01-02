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

import java.io.File

import com.google.common.collect.ImmutableList
import com.google.common.eventbus.{EventBus, Subscribe}
import de.mineformers.core.block.TestBlock
import de.mineformers.core.client.renderer.world.{RenderWorld, MFWorldRenderer}
import de.mineformers.core.client.ui.skin.{GuiMetadataSection, GuiMetadataSectionDeserializer, TextureLoader}
import de.mineformers.core.client.util.RenderUtils
import de.mineformers.core.item.TestItem
import de.mineformers.core.network.{MFNetworkWrapper, TileDescriptionMessage}
import de.mineformers.core.registry.{SharedBlockRegistry, SharedItemRegistry}
import de.mineformers.core.tileentity.Describable
import de.mineformers.core.util.renderer.GuiUtils
import de.mineformers.core.util.world.{BlockCuboid, BlockPos}
import de.mineformers.core.util.world.snapshot.WorldSnapshot
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.SimpleReloadableResourceManager
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.profiler.Profiler
import net.minecraft.server.MinecraftServer
import net.minecraft.world.World
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.client.{FMLFileResourcePack, FMLFolderResourcePack}
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.{FMLCommonHandler, DummyModContainer, LoadController, ModMetadata}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import net.minecraftforge.fml.server.FMLServerHandler
import org.lwjgl.opengl.GL11

/**
 * MFCoreContainer
 *
 * @author PaleoCrafter
 */
class MFCoreContainer extends DummyModContainer(new ModMetadata) {
  class CameraEntity(world: World) extends Entity(world) {
    override def entityInit(): Unit = ()

    override def writeEntityToNBT(tagCompound: NBTTagCompound): Unit = ()

    override def readEntityFromNBT(tagCompound: NBTTagCompound): Unit = ()
  }

  var snapshot: WorldSnapshot = _
  var cam: CameraEntity = _
  val meta = getMetadata
  meta.modId = "mfcore"
  meta.name = "MFCore"
  meta.authorList = ImmutableList.of("PaleoCrafter")
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
    SharedBlockRegistry.add("test", new TestBlock)
    SharedItemRegistry.add("testItem", new TestItem)
    Proxy.preInit(event)
  }

  @SubscribeEvent
  def onJoin(e: EntityJoinWorldEvent): Unit = {
    if(e.entity.isInstanceOf[EntityPlayer] && !e.world.isRemote) {
      snapshot = WorldSnapshot.fromArea(e.world, new BlockCuboid(BlockPos(0, 0, 0), BlockPos(3, 3, 3)))
      snapshot.addWorldAccess(new RenderWorld(snapshot))
    }
  }

  @SubscribeEvent
  def onRenderWorldLast(e: RenderWorldLastEvent): Unit = {
    GL11.glPushMatrix()
    GL11.glTranslatef(0, 8, 0)
    val player = RenderUtils.mc.thePlayer
    cam.setLocationAndAngles(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch)
    cam.lastTickPosX = player.lastTickPosX
    cam.lastTickPosY = player.lastTickPosY
    cam.lastTickPosZ = player.lastTickPosZ
//    MFWorldRenderer.renderWorld(cam, snapshot)
    GL11.glPopMatrix()
  }

  @Subscribe
  def init(event: FMLInitializationEvent): Unit = {
    Proxy.init(event)
    snapshot = new WorldSnapshot(16, 16, 16, true)
    snapshot.setBlockState(BlockPos(8, 8, 8), Blocks.stone.getDefaultState)
    cam = new CameraEntity(snapshot)
    snapshot.addWorldAccess(new RenderWorld(snapshot))
    MinecraftForge.EVENT_BUS.register(this)
  }

  override def getSource: File = MFCore.CoreModLocation

  override def getCustomResourcePackClass: Class[_] =
    if (MFCore.CoreModLocation.isDirectory) classOf[FMLFolderResourcePack] else classOf[FMLFileResourcePack]
}

object Proxy {
  var profiler: Profiler = _

  def preInit(event: FMLPreInitializationEvent): Unit = {
    if (event.getSide.isClient)
      clientPreInit()
    else if(event.getSide.isServer)
      serverPreInit()
  }

  def init(event: FMLInitializationEvent): Unit = {
    if (event.getSide.isClient)
      clientInit()
  }

  @SideOnly(Side.CLIENT)
  def clientPreInit(): Unit = {
    profiler = Minecraft.getMinecraft.mcProfiler
    MFCore.net.addHandler({
      case (TileDescriptionMessage(pos, desc), ctx) =>
        val tile = Minecraft.getMinecraft.theWorld.getTileEntity(pos)
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

  def serverPreInit(): Unit = {
    profiler = FMLServerHandler.instance().getServer.theProfiler
  }

  @SideOnly(Side.CLIENT)
  def clientInit(): Unit = {
    SharedBlockRegistry.registerRenderers()
    SharedItemRegistry.registerRenderers()
  }
}