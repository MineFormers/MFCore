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

import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import net.minecraft.client.resources.data.IMetadataSection
import java.util.jar.JarFile
import java.net.URLDecoder
import java.io.File

/**
 * ResourceUtils
 *
 * @author PaleoCrafter
 */
object ResourceUtils {

  implicit def resourceToResourceLocation(res: Resource):ResourceLocation = res.location

  implicit def locationToResource(res: ResourceLocation):Resource = Resource(res)

  def findAllInFolder(folder: Resource): List[Resource] = {
    val path = folder.toJarResource
    val url = classOf[Resource].getResource(path)
    var result = List.empty[Resource]
    if (url != null) {
      if (url.getProtocol == "file") {
        val file = new File(url.toURI)
        for (f <- file.list()) {
          var entry: String = f
          val checkSubdir: Int = entry.indexOf("/")
          if (checkSubdir >= 0) {
            entry = entry.substring(0, checkSubdir)
          }
          result = Resource(folder.domain, folder.path + entry) :: result
        }
      }
      if (url.getProtocol == "jar") {
        val jarPath: String = url.getPath.substring(5, url.getPath.indexOf("!"))
        val jar: JarFile = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))
        val entries = jar.entries
        while (entries.hasMoreElements) {
          val name: String = entries.nextElement.getName
          if (name.startsWith(path)) {
            var entry: String = name.substring(path.length)
            val checkSubdir: Int = entry.indexOf("/")
            if (checkSubdir >= 0) {
              entry = entry.substring(0, checkSubdir)
            }
            result = Resource(folder.domain, folder.path + entry) :: result
          }
        }
      }
    }
    result
  }

  object Resource {
    def apply(path: String) = new Resource(path)

    def apply(domain: String, path: String) = new Resource(domain, path)
  }

  case class Resource(location: ResourceLocation) {
    val domain = location.getResourceDomain
    val path = location.getResourcePath

    def this(domain: String, path: String) = this(new ResourceLocation(domain, path))

    def this(path: String) = this(new ResourceLocation(path))

    def toResource = Minecraft.getMinecraft.getResourceManager.getResource(location)

    def toInputStream = toResource.getInputStream

    def toJarResource: String = s"/assets/$domain/$path"

    def getMetadata(name: String): Option[IMetadataSection] = {
      if (toResource.hasMetadata) {
        val meta = toResource.getMetadata(name)
        if (meta != null) Some(meta) else None
      } else None
    }

    override def toString: String = domain + ":" + path
  }

}
