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

package de.mineformers.core.network

import cpw.mods.fml.common.network.simpleimpl.SimpleIndexedCodec
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.common.network.{FMLOutboundHandler, NetworkRegistry}
import de.mineformers.core.network.Message.NetReaction
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import net.minecraft.network.{Packet, INetHandler}
import org.apache.logging.log4j.Level
import de.mineformers.core.util.Log
import net.minecraft.entity.player.EntityPlayerMP

import scala.reflect.ClassTag

/**
 * MFCodec
 *
 * @author PaleoCrafter
 */
class MFNetworkWrapper(channelName: String) {
  def register[M <: Message]()(implicit ev: ClassTag[M]): Unit = {
    packetCodec.addDiscriminator(lastDiscriminator, ev.runtimeClass.asInstanceOf[Class[M]])
    lastDiscriminator = (lastDiscriminator + 1).toByte
  }

  def register[M <: Message](discriminator: Byte)(implicit ev: ClassTag[M]): Unit = {
    packetCodec.addDiscriminator(discriminator, ev.runtimeClass.asInstanceOf[Class[M]])
    if (lastDiscriminator < discriminator)
      lastDiscriminator = discriminator
  }

  def addHandler(reaction: NetReaction, side: Side): Unit = {
    val channel = channels.get(side)
    val handler = new PartialFunctionChannelHandler(reaction, side)
    channel.pipeline.addAfter(channel.findChannelHandlerNameForType(classOf[SimpleIndexedCodec]), reaction.toString(), handler)
  }

  class PartialFunctionChannelHandler(reaction: NetReaction, side: Side) extends SimpleChannelInboundHandler[Message](classOf[Message]) {
    override def channelRead0(ctx: ChannelHandlerContext, msg: Message): Unit = {
      val iNetHandler: INetHandler = ctx.attr(NetworkRegistry.NET_HANDLER).get
      val context: Message.Context = new Message.Context(iNetHandler, side)
      val result = reaction.applyOrElse((msg, context), null)
      if (result != null) {
        ctx.writeAndFlush(result).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
      }
    }

    override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
      Log.log(Level.ERROR, "FunctionChannelHandler exception", cause)
      super.exceptionCaught(ctx, cause)
    }
  }

  /**
   * Construct a minecraft packet from the supplied message. Can be used where minecraft packets are required, such as
   * [[net.minecraft.tileentity.TileEntity.]].
   *
   * @param message The message to translate into packet form
   * @return A minecraft { @link Packet} suitable for use in minecraft APIs
   */
  def getPacketFrom(message: Message): Packet = channels.get(Side.SERVER).generatePacketFrom(message)

  /**
   * Send this message to everyone.
   * The reaction for this message type should be on the CLIENT side.
   *
   * @param message The message to send
   */
  def sendToAll(message: Message) {
    channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL)
    channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
  }

  /**
   * Send this message to the specified player.
   * The reaction for this message type should be on the CLIENT side.
   *
   * @param message The message to send
   * @param player The player to send it to
   */
  def sendTo(message: Message, player: EntityPlayerMP) {
    channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER)
    channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player)
    channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
  }

  /**
   * Send this message to everyone within a certain range of a point.
   * The reaction for this message type should be on the CLIENT side.
   *
   * @param message The message to send
   * @param point The { @link TargetPoint} around which to send
   */
  def sendToAllAround(message: Message, point: NetworkRegistry.TargetPoint) {
    channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT)
    channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point)
    channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
  }

  /**
   * Send this message to everyone within the supplied dimension.
   * The reaction for this message type should be on the CLIENT side.
   *
   * @param message The message to send
   * @param dimensionId The dimension id to target
   */
  def sendToDimension(message: Message, dimensionId: Int) {
    channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DIMENSION)
    channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(Integer.valueOf(dimensionId))
    channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
  }

  /**
   * Send this message to the server.
   * The reaction for this message type should be on the SERVER side.
   *
   * @param message The message to send
   */
  def sendToServer(message: Message) {
    channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER)
    channels.get(Side.CLIENT).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
  }

  private val packetCodec = new SimpleIndexedCodec
  private val channels = NetworkRegistry.INSTANCE.newChannel(channelName, packetCodec)
  private var lastDiscriminator: Byte = 0
}
