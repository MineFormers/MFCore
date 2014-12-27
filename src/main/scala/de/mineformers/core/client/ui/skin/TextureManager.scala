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
package de.mineformers.core.client.ui.skin

import com.google.common.collect.ImmutableMap
import com.google.gson.{JsonArray, JsonObject}
import de.mineformers.core.client.shape2d.{Point, Rectangle, Size}
import de.mineformers.core.client.ui.component.Component
import de.mineformers.core.client.ui.skin.drawable.{DrawableTexture, DynamicTexture, StaticTexture}
import de.mineformers.core.util.ResourceUtils.Resource
import de.mineformers.core.util.renderer.GuiUtils
import net.minecraft.client.resources.data.IMetadataSection

import scala.collection.immutable.HashMap
import scala.collection.mutable

/**
 * TextureManager
 *
 * @author PaleoCrafter
 */
object TextureManager {
  private var resources = HashMap.empty[String, Resource]
  private val textures = new mutable.HashMap[String, List[TextureMapping]]
  private var deserializers = HashMap[String, DrawableDeserializer]("dynamic" -> new DynamicTexture.Deserializer(), "static" -> new StaticTexture.Deserializer())

  def apply(comp: Component): Option[DrawableTexture] = {
    if (!textures.contains(comp.identifier))
      apply(comp.identifier)
    else {
      val mappings = textures(comp.identifier)
      mappings.find(_.selector.matches(comp)).map(_.texture)
    }
  }

  def apply(id: String): Option[DrawableTexture] = {
    if (!textures.contains(id)) {
      if (resources.contains(id)) {
        set(id, load(resources(id)).head)
      }
    }
    textures.get(id).map(_.head.texture)
  }

  def apply(id: String, properties: Map[String, Any]): Option[DrawableTexture] = {
    if (!textures.contains(id))
      apply(id)
    else {
      val mappings = textures(id)
      mappings.find(_.selector.matches(properties.map(p => (p._1, p._2.toString)))).map(_.texture)
    }
  }

  def exists(identifier: String) = textures.contains(identifier)

  def loadWithTarget(resource: Resource): Unit = {
    val metaOption = resource.getMetadata("gui")
    if (metaOption.isDefined) {
      import scala.collection.JavaConversions._
      val meta = metaOption.get.asInstanceOf[GuiMetadataSection]
      if (meta.map != null && !meta.map.isEmpty) {
        for (entry <- meta.map.entrySet()) {
          val drawable = entry.getValue
          if (drawable != null) {
            drawable.texture = resource
            drawable.textureSize = GuiUtils.imageSize(resource)
            drawable.init()
            set(entry.getKey, drawable)
          }
        }
      }
    }
  }

  def load(resource: Resource): Seq[DrawableTexture] = {
    val metaOption = resource.getMetadata("gui")
    if (metaOption.isDefined) {
      import scala.collection.JavaConversions._
      val meta = metaOption.get.asInstanceOf[GuiMetadataSection]
      if (meta.map != null && !meta.map.isEmpty) {
        var seq = Seq.empty[DrawableTexture]
        for (entry <- meta.map.entrySet()) {
          val drawable = entry.getValue
          if (drawable != null) {
            drawable.texture = resource
            drawable.textureSize = GuiUtils.imageSize(resource)
            drawable.init()
            seq +:= drawable
          }
        }
        return seq
      }
    }
    Seq(null)
  }

  def reset(): Unit = {
    textures.clear()
  }

  def addResource(id: String, resource: Resource): Unit = {
    resources += id -> resource
  }

  def addDeserializer(id: String, deserializer: DrawableDeserializer): Unit = {
    deserializers += id -> deserializer
  }

  def getDeserializer(id: String): DrawableDeserializer = deserializers(id)

  def set(id: String, texture: DrawableTexture): Unit = {
    val selector = SkinSelector(id)
    if (!textures.contains(selector.id))
      textures.put(selector.id, List())
    textures.put(selector.id, (TextureMapping(selector, texture) :: textures(selector.id)).sortBy(_.selector.propertyCount).reverse)
  }

  private case class TextureMapping(selector: SkinSelector, texture: DrawableTexture)

}

trait DrawableDeserializer {
  def deserialize(typ: String, json: JsonObject): DrawableTexture

  def getRectangleFromObject(o: JsonObject): Rectangle = Rectangle(getPointFromArray(o.getAsJsonArray("start")), getSizeFromArray(o.getAsJsonArray("size")))

  def getUVsFromObject(o: JsonObject): Rectangle = if (o.has("start")) getRectangleFromObject(o) else getFullUV(o)

  def getFullUV(o: JsonObject): Rectangle = Rectangle(getPointFromArray(o.getAsJsonArray("min")), getPointFromArray(o.getAsJsonArray("max")))

  def getSizeFromArray(array: JsonArray): Size = Size(array.get(0).getAsInt, array.get(1).getAsInt)

  def getPointFromArray(array: JsonArray): Point = Point(array.get(0).getAsInt, array.get(1).getAsInt)
}

class GuiMetadataSection(val map: ImmutableMap[String, DrawableTexture]) extends IMetadataSection {
  def this(target: String, texture: DrawableTexture) = this(ImmutableMap.of(target, texture))
}