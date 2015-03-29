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
import de.mineformers.core.client.ui.view.View
import de.mineformers.core.client.ui.view.interaction.WorldSnapshotView
import de.mineformers.core.client.ui.skin.drawable.{DrawableTexture, DynamicTexture, StaticTexture}
import de.mineformers.core.util.ResourceUtils.Resource
import de.mineformers.core.util.math.shape2d.{Point, Rectangle, Size}
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
  private val textures = new mutable.HashMap[String, (Boolean, List[TextureMapping])]
  private var deserializers = HashMap[String, DrawableDeserializer]("dynamic" -> new DynamicTexture.Deserializer(), "static" -> new StaticTexture.Deserializer())

  def identifier(comp: Class[_]): String =
    comp.getSimpleName()(0).toLower + comp.getSimpleName.substring(1)

  def apply(comp: View): Option[DrawableTexture] = {
    var clazz: Class[_] = comp.getClass
    var result: Option[DrawableTexture] = None

    while (result.isEmpty && clazz != classOf[View]) {
      val mappings = textures.get(identifier(clazz))
      mappings match {
        case Some((sorted, maps)) =>
          maps.foreach(_.selector.initProperties(comp))
          if (sorted) {
            result = maps.find(_.selector.matches(comp)).map(_.texture)
          } else {
            sort(identifier(clazz), comp).find(_.selector.matches(comp)).map(_.texture)
          }
        case _ =>
      }
      clazz = clazz.getSuperclass
    }

    result.orElse(apply(identifier(comp.getClass)))
  }

  private def sort(id: String, comp: View): List[TextureMapping] = {
    val current = textures(id)._2
    val newMappings = current.sortBy(m => (m.selector.propertyCount, m.selector.priority(comp))).reverse
    textures.put(id, (true, newMappings))
    newMappings
  }

  def apply(id: String): Option[DrawableTexture] = {
    if (!textures.contains(id)) {
      if (resources.contains(id)) {
        set(id, load(resources(id)).head)
      }
    }
    textures.get(id).map(_._2.head.texture)
  }

  def apply(id: String, properties: Map[String, Any]): Option[DrawableTexture] = {
    if (!textures.contains(id))
      apply(id)
    else {
      val mappings = textures(id)
      mappings._2.find(_.selector.matches(properties.map(p => (p._1, p._2.toString)))).map(_.texture)
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
      textures.put(selector.id, (false, List()))
    val sorted = textures(selector.id)._1
    val current = textures(selector.id)._2
    if (!sorted)
      textures.put(selector.id, (sorted, (TextureMapping(selector, texture) :: current).sortBy(_.selector.propertyCount).reverse))
    else
      textures.put(selector.id, (false, (TextureMapping(selector, texture) :: current).sortBy(_.selector.propertyCount).reverse))
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

class GuiMetadataSection(val map: ImmutableMap[String, DrawableTexture], val data: ImmutableMap[String, String]) extends IMetadataSection {
  def this(target: String, texture: DrawableTexture, data: ImmutableMap[String, String]) = this(ImmutableMap.of(target, texture), data)
}