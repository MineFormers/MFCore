package de.mineformers.core.impl.asm

import de.mineformers.core.asm.transformer.ClassTransformer
import org.objectweb.asm.tree._
import org.objectweb.asm.{Type, Opcodes}
import de.mineformers.core.util.ASMUtil
import scala.util.control.Breaks

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
    val m = ASMUtil.getMinecraftMethod(clazz, MUpdateRendererMcp, MUpdateRendererSrg)
    val insns = new InsnList()
    // Call the post render hook
    insns.add(new VarInsnNode(Opcodes.ALOAD, 16)) // Pass 'renderer' as argument to onRenderByTypePost
    insns.add(new VarInsnNode(Opcodes.ALOAD, 16)) // Pass 'renderer' as argument to getfield
    insns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/renderer/RenderBlocks", ASMUtil.validateName(FBlockAccessMcp, FBlockAccessSrg), "Lnet/minecraft/world/IBlockAccess;")) // Pass 'blockAccess' as argument to onRenderByTypePost
    insns.add(new VarInsnNode(Opcodes.ILOAD, 23)) // Pass 'x' as argument to onRenderByTypePost
    insns.add(new VarInsnNode(Opcodes.ILOAD, 21)) // Pass 'y' as argument to onRenderByTypePost
    insns.add(new VarInsnNode(Opcodes.ILOAD, 22)) // Pass 'z' as argument to onRenderByTypePost
    insns.add(new VarInsnNode(Opcodes.ALOAD, 24)) // Pass 'block' as argument to onRenderByTypePost
    val namePost = "onRenderByTypePost"
    val descPost = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType("net/minecraft/client/renderer/RenderBlocks"), Type.getObjectType("net/minecraft/world/IBlockAccess"), Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE, Type.getObjectType("net/minecraft/block/Block")) // Method descriptor for onRenderByTypePost
    insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/mineformers/core/impl/asm/ASMHooks", namePost, descPost)) // Invoke onRenderByTypePost

    val loop = new Breaks
    loop.breakable(for (i <- 0 until m.instructions.size()) {
      m.instructions.get(i) match {
        case v: VarInsnNode => if (v.`var` == 19) {
          if (v.getOpcode == Opcodes.ISTORE && v.getPrevious.getOpcode == Opcodes.IOR && v.getPrevious.getPrevious.getOpcode == Opcodes.INVOKEVIRTUAL) {
            m.instructions.insertBefore(v.getNext, insns)
            loop.break()
          }
        }
        case _ =>
      }
    })
    true
  }
}
