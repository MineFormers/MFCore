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
import org.objectweb.asm.tree.{MethodNode, ClassNode}

/**
 * ASMUtil
 *
 * @author PaleoCrafter
 */
object ASMUtil {

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
