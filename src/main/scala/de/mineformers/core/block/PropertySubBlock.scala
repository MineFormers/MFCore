package de.mineformers.core.block

import java.util

import net.minecraft.block.properties.PropertyHelper

import scala.collection.JavaConverters._

/**
 * PropertySubBlock
 *
 * @author PaleoCrafter
 */
class PropertySubBlock(name: String, subBlocks: Seq[SubBlock]) extends PropertyHelper(name, classOf[SubBlock]) {
  override def getAllowedValues: util.Collection[_] = subBlocks.asJava

  override def getName(value: Comparable[_]): String = value match {
    case s: SubBlock => s.name
  }
}
