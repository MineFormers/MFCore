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

package de.mineformers.core.impl.asm

import de.mineformers.core.asm.transformer.ClassTransformer
import de.mineformers.core.asm.util.Hierarchy
import de.mineformers.core.asm.util.Instruction
import Instruction._
import Hierarchy.{Field, Method}
import Hierarchy.Conversions._
import org.objectweb.asm.tree._
import de.mineformers.core.asm.util.ClassInfo
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.Type
import de.mineformers.core.network.Message

/**
 * MessageTransformer
 *
 * @author PaleoCrafter
 */
class MessageTransformer extends ClassTransformer {
  /**
   * Transform the given [[ClassNode]]
   * @param clazz the class to transform
   * @param info additional information about the class to transform
   * @return true, if the class was changed, otherwise false
   */
  override def transform(clazz: ClassNode, info: ClassInfo): Boolean = {
    import scala.collection.JavaConversions._
    if (messInfo.isAssignableFrom(info)) {
      for (i <- 0 until clazz.fields.length) {
        val field = clazz.fields.get(i)
        if ((field.access & ACC_FINAL) != 0) {
          clazz.fields.set(i, Field(((field.access & (~ACC_FINAL)) & (~ACC_PRIVATE)) | ACC_PUBLIC, field.name, field.desc, field.signature, field.value))
        }
      }
      var foundDefault = false
      for (method <- clazz.methods) {
        if (method.name == "<init>" && method.desc == "()V")
          foundDefault = true
      }
      if (!foundDefault) {
        val method = Method(ACC_PUBLIC, "<init>", "()V")
        method.instructions.add(Var(ALOAD, 0))
        method.instructions.add(MethodOp(INVOKESPECIAL, info.superName(), "<init>", "()V"))
        method.instructions.add(Simple(RETURN))
        clazz.methods.add(method)
      }
      true
    } else
      false
  }

  val equalsDesc = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getObjectType("java/lang/Object"))
  val messInfo = ClassInfo.of(classOf[Message])

  /**
   * Determine whether the transformer needs to edit the given class.
   *
   * @param className the name of the class
   * @return true, if the transformer needs to edit it
   */
  override def transforms(className: String): Boolean = className.endsWith("Message")
}
