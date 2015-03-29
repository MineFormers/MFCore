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
package de.mineformers.core.util

import de.mineformers.core.MFCore
import de.mineformers.core.asm.util.Instruction.{MethodOp, TypeOp}
import de.mineformers.core.asm.util.{Instruction, SevenASMUtils}
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.Type
import org.objectweb.asm.tree._

/**
 * ASMUtil
 *
 * @author PaleoCrafter
 */
object ASMUtils {
  /**
   * Find a method for the given name
   * @param clazz the class to search the method in
   * @param name the name of the method to search
   * @return a [[MethodNode]] if found
   */
  def findMethod(clazz: ClassNode, name: String): MethodNode = {
    import scala.collection.JavaConversions._
    for (m <- clazz.methods)
      if (m.name == name)
        return m
    null
  }

  /**
   * Eases the search for a minecraft method
   * @param clazz the class to search the method in
   * @param mcpName the MCP name of the method
   * @param srgName the SRG name of the method
   * @return a [[MethodNode]] if found
   */
  def getMappedMethod(clazz: ClassNode, mcpName: String, srgName: String): MethodNode = {
    val m = findMethod(clazz, if (mcpEnv) mcpName else srgName)
    if (m == null)
      throw new NoSuchMethodException(mcpName + " in " + clazz.name)
    m
  }

  def box(typ: String, cast: Boolean = true): Option[Instruction[AbstractInsnNode]] = {
    val newType = typ match {
      case "Z" => "java/lang/Boolean"
      case "C" => "java/lang/Character"
      case "B" => "java/lang/Byte"
      case "S" => "java/lang/Short"
      case "I" => "java/lang/Integer"
      case "F" => "java/lang/Float"
      case "J" => "java/lang/Long"
      case "D" => "java/lang/Double"
      case _ => typ
    }
    if (newType != typ)
      if (cast) Some(TypeOp(CHECKCAST, newType)) else Some(MethodOp(INVOKESTATIC, newType, "valueOf", s"($typ)" + Type.getObjectType(newType)))
    else None
  }

  def unboxingNode(typ: String): Option[MethodOp] = {
    val (name, desc) = typ match {
      case "java/lang/Boolean" => ("booleanValue", "()Z")
      case "java/lang/Character" => ("charValue", "()C")
      case "java/lang/Byte" => ("byteValue", "()B")
      case "java/lang/Short" => ("shortValue", "()S")
      case "java/lang/Integer" => ("intValue", "()I")
      case "java/lang/Float" => ("floatValue", "()F")
      case "java/lang/Long" => ("longValue", "()J")
      case "java/lang/Double" => ("doubleValue", "()D")
      case _ => (null, null)
    }
    if (name != null) {
      Some(MethodOp(INVOKEVIRTUAL, typ, name, desc))
    } else
      None
  }

  def findListStart(find: Seq[AbstractInsnNode], in: InsnList): AbstractInsnNode = {
    import scala.collection.JavaConversions._
    val list = in.iterator().toList
    for (i <- 0 until list.length) {
      import scala.util.control.Breaks._
      breakable {
        val elem = list(i)
        if (!SevenASMUtils.matches(elem, find.head))
          break()
        if (i + find.size > in.size())
          return null
        val sub = list.slice(i, i + find.size)
        for (j <- 0 until sub.length) {
          if (!SevenASMUtils.matches(sub(j), find(j)))
            break()
        }
        return elem
      }
    }
    null
  }

  /**
   * @param mcpName a MCP mapped name
   * @param srgName the MCP name's SRG equivalent
   * @return either the MCP or the SRG name, depending on the obfuscation of the environment
   */
  def validateName(mcpName: String, srgName: String): String = if (mcpEnv) mcpName else srgName

  /**
   * @return true, if we're running in an MCP mapped environment
   */
  def mcpEnv = MFCore.McpEnvironment
}
