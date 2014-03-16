package de.mineformers.core.asm.transformer

import org.objectweb.asm.tree.ClassNode

trait ClassTransformer {

  def transform(clazz: ClassNode): Boolean

  def transforms(className: String): Boolean

}
