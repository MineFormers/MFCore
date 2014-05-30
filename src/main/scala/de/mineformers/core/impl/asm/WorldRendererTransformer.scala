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
import org.objectweb.asm.{Type, Opcodes}
import de.mineformers.core.util.ASMUtil
import scala.util.control.Breaks._

/**
 * WorldRendererTransformer
 *
 * @author PaleoCrafter
 */
class WorldRendererTransformer extends ClassTransformer {
  final val MUpdateRendererMcp = "updateRenderer"
  final val MUpdateRendererSrg = "func_147892_a"

  final val FBlockAccessMcp = "blockAccess"
  final val FBlockAccessSrg = "field_147845_a"

  /**
   * @param className the name of the class
   * @return true, if the transformer needs to edit it
   */
  override def transforms(className: String): Boolean = className == "net.minecraft.client.renderer.WorldRenderer"

  /**
   * Transform the given class
   * @param clazz the class to transform
   * @return true, if the class was changed, otherwise false
   */
  override def transform(clazz: ClassNode): Boolean = {
    val m = ASMUtil.getMappedMethod(clazz, MUpdateRendererMcp, MUpdateRendererSrg)
    val insnsPre = new InsnList()
    // Call the pre render hook, if it returns true, return the method
    val lblTrue = new LabelNode() // Label for if-statement
    val lblFalse = new LabelNode()
    insnsPre.add(new VarInsnNode(Opcodes.ALOAD, 16)) // Pass 'renderer' as argument to onRenderByTypePost
    insnsPre.add(new VarInsnNode(Opcodes.ALOAD, 16)) // Pass 'renderer' as argument to getfield
    insnsPre.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/renderer/RenderBlocks", ASMUtil.validateName(FBlockAccessMcp, FBlockAccessSrg), "Lnet/minecraft/world/IBlockAccess;")) // Pass 'blockAccess' as argument to onRenderByTypePost
    insnsPre.add(new VarInsnNode(Opcodes.ILOAD, 23)) // Pass 'x' as argument to onRenderByTypePost
    insnsPre.add(new VarInsnNode(Opcodes.ILOAD, 21)) // Pass 'y' as argument to onRenderByTypePost
    insnsPre.add(new VarInsnNode(Opcodes.ILOAD, 22)) // Pass 'z' as argument to onRenderByTypePost
    insnsPre.add(new VarInsnNode(Opcodes.ALOAD, 24)) // Pass 'block' as argument to onRenderByTypePost
    insnsPre.add(new VarInsnNode(Opcodes.ILOAD, 17)) // Pass 'renderPass' as argument to onRenderByTypePost
    val name = "onRenderByTypePre"
    val desc = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getObjectType("net/minecraft/client/renderer/RenderBlocks"), Type.getObjectType("net/minecraft/world/IBlockAccess"), Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE, Type.getObjectType("net/minecraft/block/Block"), Type.INT_TYPE) // Method descriptor for onRenderByTypePre
    insnsPre.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/mineformers/core/impl/asm/ASMHooks", name, desc)) // Invoke onRenderByTypePre
    insnsPre.add(new JumpInsnNode(Opcodes.IFEQ, lblTrue)) // Use the result as if jump
    insnsPre.add(new InsnNode(Opcodes.ICONST_1))
    insnsPre.add(new VarInsnNode(Opcodes.ISTORE, 19))
    insnsPre.add(new JumpInsnNode(Opcodes.GOTO, lblFalse))
    insnsPre.add(new InsnNode(Opcodes.ICONST_1))
    insnsPre.add(new VarInsnNode(Opcodes.ISTORE, 18))
    insnsPre.add(lblTrue)

    val insnsPost = new InsnList()
    // Call the post render hook
    insnsPost.add(new VarInsnNode(Opcodes.ALOAD, 16)) // Pass 'renderer' as argument to onRenderByTypePost
    insnsPost.add(new VarInsnNode(Opcodes.ALOAD, 16)) // Pass 'renderer' as argument to getfield
    insnsPost.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/renderer/RenderBlocks", ASMUtil.validateName(FBlockAccessMcp, FBlockAccessSrg), "Lnet/minecraft/world/IBlockAccess;")) // Pass 'blockAccess' as argument to onRenderByTypePost
    insnsPost.add(new VarInsnNode(Opcodes.ILOAD, 23)) // Pass 'x' as argument to onRenderByTypePost
    insnsPost.add(new VarInsnNode(Opcodes.ILOAD, 21)) // Pass 'y' as argument to onRenderByTypePost
    insnsPost.add(new VarInsnNode(Opcodes.ILOAD, 22)) // Pass 'z' as argument to onRenderByTypePost
    insnsPost.add(new VarInsnNode(Opcodes.ALOAD, 24)) // Pass 'block' as argument to onRenderByTypePost
    insnsPost.add(new VarInsnNode(Opcodes.ILOAD, 17)) // Pass 'renderPass' as argument to onRenderByTypePost
    val namePost = "onRenderByTypePost"
    val descPost = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType("net/minecraft/client/renderer/RenderBlocks"), Type.getObjectType("net/minecraft/world/IBlockAccess"), Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE, Type.getObjectType("net/minecraft/block/Block"), Type.INT_TYPE) // Method descriptor for onRenderByTypePost
    insnsPost.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/mineformers/core/impl/asm/ASMHooks", namePost, descPost)) // Invoke onRenderByTypePost
    var visitedPre = false
    breakable(for (i <- 0 until m.instructions.size()) {
      m.instructions.get(i) match {
        case v: VarInsnNode => if (v.`var` == 24) {
          if (!visitedPre && v.getOpcode == Opcodes.ALOAD && v.getNext.getOpcode == Opcodes.INVOKEVIRTUAL &&
            v.getNext.getNext.getOpcode == Opcodes.ISTORE) {
            visitedPre = true
            m.instructions.insertBefore(v, insnsPre)
          }
        }
        case i: IincInsnNode => if (i.getOpcode == Opcodes.IINC) {
          if (i.`var` == 23 && i.getNext.getOpcode == Opcodes.GOTO) {
            m.instructions.insertBefore(i.getPrevious.getPrevious.getPrevious, insnsPost)
            m.instructions.insertBefore(i, lblFalse)
            break()
          }
        }
        case _ =>
      }
    })
    true
  }
}
