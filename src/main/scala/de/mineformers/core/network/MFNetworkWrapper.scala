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
import cpw.mods.fml.common.network.{FMLOutboundHandler, FMLEmbeddedChannel, NetworkRegistry}
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import net.minecraft.network.{Packet, INetHandler}
import org.apache.logging.log4j.Level
import de.mineformers.core.util.Log
import net.minecraft.entity.player.EntityPlayerMP

/**
 * MFCodec
 *
 * @author PaleoCrafter
 */
class MFNetworkWrapper(channelName: String) {
  def register[M <: Message, R <: Message](message: Class[M], reaction: (M, Message.Context) => R, discriminator: Byte, side: Side) {
    packetCodec.addDiscriminator(discriminator, message)
    val channel: FMLEmbeddedChannel = channels.get(side)
    val `type`: String = channel.findChannelHandlerNameForType(classOf[SimpleIndexedCodec])
    if (side == Side.SERVER) {
      addServerHandlerAfter(channel, `type`, reaction, message)
    } else {
      addClientHandlerAfter(channel, `type`, reaction, message)
    }
  }

  private def addServerHandlerAfter[M <: Message, R <: Message](channel: FMLEmbeddedChannel, `type`: String, reaction: (M, Message.Context) => R, message: Class[M]) {
    val handler = new FunctionChannelHandler[M, R](message, reaction, Side.SERVER)
    channel.pipeline.addAfter(`type`, message.getName, handler)
  }

  private def addClientHandlerAfter[M <: Message, R <: Message](channel: FMLEmbeddedChannel, `type`: String, reaction: (M, Message.Context) => R, message: Class[M]) {
    val handler = new FunctionChannelHandler[M, R](message, reaction, Side.CLIENT)
    channel.pipeline.addAfter(`type`, message.getName, handler)
  }

  class FunctionChannelHandler[M <: Message, R <: Message](request: Class[M], reaction: (M, Message.Context) => R, side: Side) extends SimpleChannelInboundHandler[M](request) {
    override def channelRead0(ctx: ChannelHandlerContext, msg: M): Unit = {
      val iNetHandler: INetHandler = ctx.attr(NetworkRegistry.NET_HANDLER).get
      val context: Message.Context = new Message.Context(iNetHandler, side)
      val result: R = reaction(msg, context)
      if (result != null) {
        ctx.writeAndFlush(result).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
      }
    }

    override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
      Log().log(Level.ERROR, "FunctionChannelHandler exception", cause)
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
}
