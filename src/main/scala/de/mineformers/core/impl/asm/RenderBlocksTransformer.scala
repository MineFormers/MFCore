package de.mineformers.core.impl.asm

import de.mineformers.core.asm.transformer.ClassTransformer
import org.objectweb.asm.tree._
import de.mineformers.core.util.ASMUtil
import org.objectweb.asm.{Type, Opcodes}
import scala.util.control.Breaks

/**
 * RenderBlocksTransformer
 *
 * @author PaleoCrafter
 */
class RenderBlocksTransformer extends ClassTransformer {
  final val MRenderByTypeMcp = "renderBlockByRenderType"
  final val MRenderByTypeSrg = "func_147805_b"

  final val FBlockAccessMcp = "blockAccess"
  final val FBlockAccessSrg = "field_147845_a"

  /**
   * @param className the name of the class
   * @return true, if the transformer needs to edit it
   */
  override def transforms(className: String): Boolean =
    className == "net.minecraft.client.renderer.RenderBlocks"

  /**
   * Transform the given class
   * @param clazz the class to transform
   * @return true, if the class was changed, otherwise false
   */
  override def transform(clazz: ClassNode): Boolean = {
    val m = ASMUtil.getMinecraftMethod(clazz, MRenderByTypeMcp, MRenderByTypeSrg)
    val insns = new InsnList()
    // Call the pre render hook, if it returns true, return the method
    val lbl = new LabelNode() // Label for if-statement
    insns.add(new VarInsnNode(Opcodes.ALOAD, 0)) // Pass 'this' as argument to onRenderByTypePre
    insns.add(new VarInsnNode(Opcodes.ALOAD, 0)) // Pass 'this' as argument to getfield
    insns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/renderer/RenderBlocks", ASMUtil.validateName(FBlockAccessMcp, FBlockAccessSrg), "Lnet/minecraft/world/IBlockAccess;")) // Pass 'blockAccess' as argument
    insns.add(new VarInsnNode(Opcodes.ILOAD, 2)) // Pass 'x' as argument to onRenderByTypePre
    insns.add(new VarInsnNode(Opcodes.ILOAD, 3)) // Pass 'y' as argument to onRenderByTypePre
    insns.add(new VarInsnNode(Opcodes.ILOAD, 4)) // Pass 'z' as argument to onRenderByTypePre
    insns.add(new VarInsnNode(Opcodes.ALOAD, 1)) // Pass 'block' as argument to onRenderByTypePre
    insns.add(new VarInsnNode(Opcodes.ILOAD, 5)) // Pass 'modelId' as argument to onRenderByTypePre
    val name = "onRenderByTypePre"
    val desc = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getObjectType(clazz.name), Type.getObjectType("net/minecraft/world/IBlockAccess"), Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE, Type.getObjectType("net/minecraft/block/Block"), Type.INT_TYPE) // Method descriptor for onRenderByTypePre
    insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/mineformers/core/impl/asm/ASMHooks", name, desc)) // Invoke onRenderByTypePre
    insns.add(new JumpInsnNode(Opcodes.IFEQ, lbl)) // Use the result as if jump
    insns.add(new InsnNode(Opcodes.ICONST_0)) // Pass '0' to return
    insns.add(new InsnNode(Opcodes.IRETURN)) // Return an integer (boolean)
    insns.add(lbl) // Add the label as jumping point if the result is false

    // Add instruction lists, pre before switch, post afterwards
    val loop = new Breaks
    loop.breakable {
      for (i <- 0 until m.instructions.size())
        if (m.instructions.get(i).isInstanceOf[TableSwitchInsnNode]) {
          m.instructions.insertBefore(m.instructions.get(5), insns)
          loop.break()
        }
    }

    true
  }
}
