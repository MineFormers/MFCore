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
package de.mineformers.core

import java.io.File
import java.net.URISyntaxException

import com.google.common.base.Throwables
import cpw.mods.fml.relauncher.IFMLLoadingPlugin
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.{MCVersion, TransformerExclusions}
import de.mineformers.core.impl.asm.CoreClassTransformer
import de.mineformers.core.network.MFNetworkWrapper
import de.mineformers.core.util.Log

/**
 * MFCore
 *
 * @author PaleoCrafter
 */
@MCVersion("1.7.10")
@IFMLLoadingPlugin.SortingIndex(1001)
@TransformerExclusions(Array("de.mineformers.core.asm.", "de.mineformers.core.impl.asm.", "scala."))
class MFCore extends IFMLLoadingPlugin {
  override def getAccessTransformerClass: String = null

  /**
   * Get data from the running Minecraft instance.
   * Used to determine the environment type.
   *
   * @param data a [[java.util.Map]] representing the data
   */
  override def injectData(data: java.util.Map[String, AnyRef]): Unit = {
    MFCore.McpEnvironment = !data.get("runtimeDeobfuscationEnabled").asInstanceOf[Boolean]
    MFCore.CoreModLocation = data.get("coremodLocation").asInstanceOf[File]
    if (MFCore.CoreModLocation == null) {
      try {
        MFCore.CoreModLocation = new File(getClass.getProtectionDomain.getCodeSource.getLocation.toURI)
      } catch {
        case e: URISyntaxException =>
          Log.error("Failed to acquire source location for MFCore!")
          throw Throwables.propagate(e)
      }
    }
  }

  /**
   * Get the setup class for this coremod
   * @return the fully qualified class name of the setup class
   */
  override def getSetupClass: String = null

  /**
   * @return the fully qualified class name of the mod container class
   */
  override def getModContainerClass: String = classOf[MFCoreContainer].getName

  /**
   * @return an array of transformer class names
   */
  override def getASMTransformerClass: Array[String] = Array(classOf[CoreClassTransformer].getName)
}

/**
 * MFCore
 *
 * @author PaleoCrafter
 */
object MFCore {
  var net: MFNetworkWrapper = null
  var McpEnvironment = false
  var CoreModLocation: File = null
}
