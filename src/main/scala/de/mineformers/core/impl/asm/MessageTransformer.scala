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
import org.objectweb.asm.tree._
import de.mineformers.core.asm.util.ClassInfo
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.Type
import de.mineformers.core.util.ASMUtils
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
          clazz.fields.set(i, new FieldNode(field.access & (~ACC_FINAL), field.name, field.desc, field.signature, field.value))
        }
      }
      var foundDefault = false
      var foundSet = false
      var foundGet = false
      for (method <- clazz.methods) {
        if (method.name == "<init>" && method.desc == "()V")
          foundDefault = true
        if (method.name == "set" && method.desc == setDesc)
          foundSet = true
        if (method.name == "get" && method.desc == getDesc)
          foundGet = true
      }
      if (!foundDefault) {
        val method = new MethodNode(ACC_PUBLIC, "<init>", "()V", null, null)
        method.instructions.add(new VarInsnNode(ALOAD, 0))
        method.instructions.add(new MethodInsnNode(INVOKESPECIAL, info.superName(), "<init>", "()V"))
        method.instructions.add(new InsnNode(RETURN))
        clazz.methods.add(method)
      }
      if (!foundSet) {
        val method = new MethodNode(ACC_PROTECTED, "set", setDesc, null, null)
        val insns = method.instructions
        val defaultLabel = new LabelNode()
        val actualKeys = clazz.fields.map(f => f.name ->(f, f.name.hashCode)).toMap
        val keys = clazz.fields.map(_.name.hashCode).toArray.distinct
        val labels = Array.fill(keys.length)(new LabelNode())
        insns.add(new VarInsnNode(ALOAD, 1)) // load 'field'
        insns.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I")) // load String.hashCode() for switching
        insns.add(new LookupSwitchInsnNode(defaultLabel, keys, labels)) // do switch
        for ((key, label) <- (keys, labels).zipped) {
          insns.add(label) // Add label
          for ((field, (fieldNode, code)) <- actualKeys if code == key) {
            insns.add(new VarInsnNode(ALOAD, 1)) // load 'field'
            insns.add(new LdcInsnNode(field))
            insns.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "equals", equalsDesc))
            insns.add(new JumpInsnNode(IFEQ, defaultLabel))
            insns.add(new VarInsnNode(ALOAD, 0)) // load 'this'
            insns.add(new VarInsnNode(ALOAD, 2)) // load 'value'
            insns.add(castFrom(fieldNode))
            insns.add(new FieldInsnNode(PUTFIELD, clazz.name, field, fieldNode.desc))
            insns.add(new InsnNode(RETURN))
          }
        }
        insns.add(defaultLabel)
        insns.add(new VarInsnNode(ALOAD, 0))
        insns.add(new VarInsnNode(ALOAD, 1))
        insns.add(new VarInsnNode(ALOAD, 2))
        insns.add(new MethodInsnNode(INVOKESPECIAL, info.superName(), "set", setDesc))
        insns.add(new InsnNode(RETURN))
        clazz.methods.add(method)
      }
      if (!foundGet) {
        val method = new MethodNode(ACC_PROTECTED, "get", getDesc, null, null)
        val insns = method.instructions
        val defaultLabel = new LabelNode()
        val actualKeys = clazz.fields.map(f => f.name ->(f, f.name.hashCode)).toMap
        val keys = clazz.fields.map(_.name.hashCode).toArray.distinct
        val labels = Array.fill(keys.length)(new LabelNode())
        insns.add(new VarInsnNode(ALOAD, 1)) // load 'field'
        insns.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I")) // load String.hashCode() for switching
        insns.add(new LookupSwitchInsnNode(defaultLabel, keys, labels)) // do switch
        for ((key, label) <- (keys, labels).zipped) {
          insns.add(label) // Add label
          for ((field, (fieldNode, code)) <- actualKeys if code == key) {
            insns.add(new VarInsnNode(ALOAD, 1)) // load 'field'
            insns.add(new LdcInsnNode(field))
            insns.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "equals", equalsDesc))
            insns.add(new JumpInsnNode(IFEQ, defaultLabel))
            insns.add(new VarInsnNode(ALOAD, 0)) // load 'this'
            insns.add(new FieldInsnNode(GETFIELD, clazz.name, field, fieldNode.desc))
            insns.add(castTo(fieldNode))
            insns.add(new InsnNode(ARETURN))
          }
        }
        insns.add(defaultLabel)
        insns.add(new VarInsnNode(ALOAD, 0))
        insns.add(new VarInsnNode(ALOAD, 1))
        insns.add(new MethodInsnNode(INVOKESPECIAL, info.superName(), "get", getDesc))
        insns.add(new InsnNode(ARETURN))
        clazz.methods.add(method)
      }
      true
    } else
      false
  }

  val equalsDesc = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getObjectType("java/lang/Object"))
  val setDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType("java/lang/String"), Type.getObjectType("java/lang/Object"))
  val getDesc = Type.getMethodDescriptor(Type.getObjectType("java/lang/Object"), Type.getObjectType("java/lang/String"))
  val messInfo = ClassInfo.of(classOf[Message])

  def castFrom(field: FieldNode): InsnList = {
    val insns = new InsnList
    val box = ASMUtils.box(field.desc).asInstanceOf[TypeInsnNode]
    if (box != null) {
      insns.add(box)
      insns.add(ASMUtils.unboxingNode(box.desc))
    } else {
      insns.add(new TypeInsnNode(CHECKCAST, field.desc.replace(";", "").substring(1)))
    }
    insns
  }

  def castTo(field: FieldNode): InsnList = {
    val insns = new InsnList
    val box = ASMUtils.box(field.desc, cast = false)
    if (box != null) {
      insns.add(box)
    }
    insns
  }

  /**
   * Determine whether the transformer needs to edit the given class.
   *
   * @param className the name of the class
   * @return true, if the transformer needs to edit it
   */
  override def transforms(className: String): Boolean = className.endsWith("Message")
}
