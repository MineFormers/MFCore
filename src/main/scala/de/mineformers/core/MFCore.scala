package de.mineformers.core

import cpw.mods.fml.relauncher.IFMLLoadingPlugin
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.{TransformerExclusions, MCVersion}
import de.mineformers.core.asm.transformer.MFCoreAccessTransformer
import de.mineformers.core.impl.asm.CoreClassTransformer

@MCVersion("1.7.2")
@IFMLLoadingPlugin.SortingIndex(1001)
@TransformerExclusions(Array(
  "de.mineformers.core.asm.transformer.",
  "de.mineformers.core.impl.asm.",
  "scala."
))
class MFCore extends IFMLLoadingPlugin {
  override def getAccessTransformerClass: String = classOf[MFCoreAccessTransformer].getName

  override def injectData(data: java.util.Map[String, AnyRef]): Unit = {
    MFCore.McpEnvironment = !data.get("runtimeDeobfuscationEnabled").asInstanceOf[Boolean]
  }

  override def getSetupClass: String = null

  override def getModContainerClass: String = classOf[MFCoreContainer].getName

  override def getASMTransformerClass: Array[String] = Array(classOf[CoreClassTransformer].getName)
}

object MFCore {
  var McpEnvironment = false
}
