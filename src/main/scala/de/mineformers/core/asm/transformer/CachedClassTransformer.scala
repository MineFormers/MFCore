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
package de.mineformers.core.asm.transformer

import de.mineformers.core.asm.util.ClassInfo
import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.ClassWriter._
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.{ClassReader, ClassWriter}

import scala.collection.mutable

/**
 * CachedClassTransformer
 *
 * @author PaleoCrafter
 */
trait CachedClassTransformer extends IClassTransformer {
  protected val transformers = mutable.ArrayBuffer[ClassTransformer]()
  init()

  /**
   * Initialize this transformer wrapper, register the actual transformers here
   */
  def init(): Unit

  /**
   * Register a transformer to this wrapper
   * @param transformer the [[ClassTransformer]] to register
   */
  def register(transformer: ClassTransformer): Unit = transformers += transformer

  /**
   * Transform any class given.
   * @param name the untransformed name of the class
   * @param transformedName the name of the class after transformation
   * @param bytes the untransformed class' bytes
   * @return a (modified) byte array representing the class
   */
  override def transform(name: String, transformedName: String, bytes: Array[Byte]): Array[Byte] = {
    var transformed: Boolean = false
    var clazz: ClassNode = null
    var classInfo: ClassInfo = null
    for (transformer <- transformers) {
      if (transformer.transforms(transformedName)) {
        if (clazz == null) {
          val cr = new ClassReader(bytes)
          clazz = new ClassNode()
          cr.accept(clazz, 0)
          classInfo = ClassInfo.of(clazz)
        }
        transformed |= transformer.transform(clazz, classInfo)
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
