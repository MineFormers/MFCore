package de.mineformers.core.util

import de.mineformers.core.MFCore
import org.objectweb.asm.tree.{MethodNode, ClassNode}

object ASMUtil {

  def findMethod(clazz: ClassNode, name: String): MethodNode = {
    import scala.collection.JavaConversions._
    for (m <- clazz.methods)
      if (m.name == name)
        return m
    null
  }

  def getMinecraftMethod(clazz: ClassNode, mcpName: String, srgName: String): MethodNode = {
    val m = findMethod(clazz, if (mcpEnv) mcpName else srgName)
    if (m == null)
      throw new NoSuchMethodException(mcpName + " in " + clazz.name)
    m
  }

  def validateName(mcpName: String, srgName: String): String = if (mcpEnv) mcpName else srgName

  def mcpEnv = MFCore.McpEnvironment

}
