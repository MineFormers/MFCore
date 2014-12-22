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
package de.mineformers.core.tileentity

import java.lang.reflect.Field

import cpw.mods.fml.relauncher.SideOnly
import de.mineformers.core.MFCore
import de.mineformers.core.network.Message.Serializer
import de.mineformers.core.network.{Message, TileDescriptionMessage}
import io.netty.buffer.ByteBuf
import net.minecraft.network.Packet

import scala.collection.mutable

/**
 * TileDescription
 *
 * @author PaleoCrafter
 */
final class TileDescription(val parentClass: Class[Describable]) {
  val fields = Describable.collectFields(parentClass)
  val values = mutable.Map.empty[Field, Any]

  def this() = this(null)

  def write(buf: ByteBuf): Unit = {
    for (f <- fields) {
      Describable.getSerializer(f) match {
        case Some(s) =>
          s.serialize(values(f), buf)
        case _ =>
      }
    }
  }

  def read(buf: ByteBuf): Unit = {
    for (f <- fields) {
      Describable.getSerializer(f) match {
        case Some(s) =>
          values += f -> s.deserialize(buf)
        case _ =>
      }
    }
  }

  def readParent(parent: Describable): Unit = {
    for (f <- fields) {
      values += f -> f.get(parent)
    }
  }

  def writeParent(parent: Describable): Unit = {
    for (f <- fields) {
      f.set(parent, values(f))
    }
  }
}

/**
 * Describable
 *
 * @author PaleoCrafter
 */
trait Describable {
  this: MFTile =>
  final override def getDescriptionPacket: Packet = {
    if (description == null)
      return null

    MFCore.net.getPacketFrom(new TileDescriptionMessage(xCoord, yCoord, zCoord, description))
  }

  def description: TileDescription = {
    val desc = new TileDescription(this.getClass.asInstanceOf[Class[Describable]])
    desc.readParent(this)
    desc
  }

  def onDescription(): Unit
}

object Describable {
  private val classFieldMappings = mutable.Map.empty[Class[_ <: Describable], Seq[Field]]
  private val fieldSerializerMappings = mutable.Map.empty[Field, Serializer[Any]]

  def collectFields[T <: Describable](clazz: Class[T]): Seq[Field] = classFieldMappings.getOrElseUpdate(clazz, {
    clazz.getDeclaredFields.sortBy(_.getName).foldLeft(Seq.empty[Field]) {
      (acc: Seq[Field], f: Field) =>
        if (f.getAnnotation(classOf[Describing]) != null && f.getAnnotation(classOf[SideOnly]) == null) {
          f.setAccessible(true)
          Message.getSerializer(f.getType) match {
            case Some(s) =>
              fieldSerializerMappings += f -> s.asInstanceOf[Serializer[Any]]
              acc ++ Seq(f)
            case _ => acc
          }
        }
        else acc
    }
  })

  def getSerializer(f: Field) = fieldSerializerMappings.get(f)
}
