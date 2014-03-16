package de.mineformers.core.asm.transformer

import net.minecraft.launchwrapper.IClassTransformer
import collection.mutable
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.{ClassReader, ClassWriter}
import org.objectweb.asm.ClassWriter._

trait CachedClassTransformer extends IClassTransformer {
  protected val transformers = mutable.ArrayBuffer[ClassTransformer]()

  init()

  def init(): Unit

  def register(transformer: ClassTransformer): Unit = transformers += transformer

  override def transform(name: String, transformedName: String, bytes: Array[Byte]): Array[Byte] = {
    var transformed: Boolean = false
    var clazz: ClassNode = null
    for (transformer <- transformers) {
      if (transformer.transforms(transformedName)) {
        if (clazz == null) {
          val cr = new ClassReader(bytes)
          clazz = new ClassNode()
          cr.accept(clazz, 0)
        }
        transformed |= transformer.transform(clazz)
      }
    }
    if (transformed) {
      val cw = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS)
      clazz.accept(cw)
      return cw.toByteArray
    }
    bytes
  }
}
