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

import cpw.mods.fml.common.network.simpleimpl.IMessage
import io.netty.buffer.ByteBuf
import net.minecraft.network.{NetHandlerPlayServer, INetHandler}
import cpw.mods.fml.common.network.ByteBufUtils
import scala.collection.immutable.HashMap
import cpw.mods.fml.relauncher.Side
import net.minecraft.client.network.NetHandlerPlayClient

/**
 * Message
 *
 * @author PaleoCrafter
 */
class Message extends IMessage {
  override def fromBytes(buf: ByteBuf): Unit = {
    for (field <- fields) {
      val serializer = serializers.get(field).getOrElse(null)
      if (serializer != null) {
        set(field, serializer.deserialize(buf))
      }
    }
  }

  override def toBytes(buf: ByteBuf): Unit = {
    for (field <- fields) {
      val serializer = serializers.get(field).getOrElse(null)
      if (serializer != null) {
        serializer.serialize(get(field), buf)
      }
    }
  }

  def set(field: String, value: Any): Unit = {
  }

  def get(field: String): Any = null

  def serializers = {
    if (_serializers == null) {
      val fields = this.getClass.getFields
      _serializers = HashMap.empty[String, Message.Serializer[Any]]
      for (field <- fields) {
        field.setAccessible(true)
        val serializer = Message.getSerializer(field.getType)
        _serializers += field.getName -> serializer.getOrElse(null).asInstanceOf[Message.Serializer[Any]]
      }
    }
    _serializers
  }

  val fields = this.getClass.getFields.map(_.getName)

  var _serializers: Map[String, Message.Serializer[Any]] = null
}

object Message {
  private var serializers = HashMap.empty[Class[_], Serializer[Any]]

  addSerializer(classOf[String], new Serializer[String] {
    override def serialize(target: String, buffer: ByteBuf): Unit = ByteBufUtils.writeUTF8String(buffer, target)

    override def deserialize(buffer: ByteBuf): String = ByteBufUtils.readUTF8String(buffer)
  })

  addSerializer(Integer.TYPE, new Serializer[Integer] {
    override def serialize(target: Integer, buffer: ByteBuf): Unit = buffer.writeInt(target)

    override def deserialize(buffer: ByteBuf): Integer = buffer.readInt()
  })

  def addSerializer[A](clazz: Class[A], serializer: Serializer[A]): Unit = {
    serializers += clazz -> serializer.asInstanceOf[Serializer[Any]]
  }

  def getSerializer[A](clazz: Class[A]): Option[Serializer[A]] = serializers.get(clazz).asInstanceOf[Option[Serializer[A]]]

  class Context(netHandler: INetHandler, val side: Side) {
    def serverHandler: NetHandlerPlayServer = netHandler.asInstanceOf[NetHandlerPlayServer]

    def clientHandler: NetHandlerPlayClient = netHandler.asInstanceOf[NetHandlerPlayClient]
  }

  trait Serializer[T] {
    def serialize(target: T, buffer: ByteBuf)

    def deserialize(buffer: ByteBuf): T
  }

}

case class TestMessage(i: Int, s: String) extends Message