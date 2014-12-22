/*
 *
 *  * The MIT License (MIT)
 *  *
 *  * Copyright (c) 2014 MineFormers
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *
 */
/*
 *
 *  * The MIT License (MIT)
 *  *
 *  * Copyright (c) 2014 MineFormers
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *
 */
package de.mineformers.core.asm.util

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree._

import scala.collection.mutable
import scala.language.implicitConversions

/**
 * Hierarchy
 *
 * @author PaleoCrafter
 */
abstract sealed class Hierarchy[A] {
  def asm: A
}

object Hierarchy {

  case class Class(access: Int, name: String, signature: String, superName: String, interfaces: mutable.Buffer[String], fields: mutable.Buffer[Field], method: mutable.Buffer[Method]) extends Hierarchy[ClassNode] {
    override lazy val asm: ClassNode = {
      val node = new ClassNode()
      node.visit(Opcodes.ASM4, access, name, signature, superName, interfaces.toArray)
      node
    }
  }

  case class InnerClass(access: Int, name: String, outerName: String, innerName: String) extends Hierarchy[InnerClassNode] {
    override lazy val asm: InnerClassNode = new InnerClassNode(name, outerName, innerName, access)
  }

  case class Field(access: Int, name: String, desc: String, signature: String = null, value: Any = null) extends Hierarchy[FieldNode] {
    override lazy val asm: FieldNode = new FieldNode(access, name, desc, signature, value)
  }

  case class Method(access: Int, name: String, desc: String, signature: String = null, exceptions: Traversable[String] = null) extends Hierarchy[MethodNode] {
    val instructions = mutable.Buffer.empty[Instruction[AbstractInsnNode]]
    override lazy val asm: MethodNode = {
      val node = new MethodNode(access, name, desc, signature, if (exceptions != null) exceptions.toArray else null)
      node.instructions = Instruction.seq2insList(instructions)
      node
    }
  }

  object Conversions {
    def converter[A](n: A) = n match {
      case node: ClassNode => asm2class(node)
      case node: InnerClassNode => asm2innerClass(node)
      case node: FieldNode => asm2field(node)
      case node: MethodNode => asm2method(node)
    }

    implicit def asm2any[A](asm: A): Hierarchy[A] = converter(asm).asInstanceOf[Hierarchy[A]]

    implicit def asm2class(asm: ClassNode): Class = {
      import scala.collection.JavaConversions._
      Class(asm.access, asm.name, asm.signature, asm.superName, asm.interfaces.toBuffer, asm.fields map asm2field, asm.methods map asm2method)
    }

    implicit def asm2innerClass(asm: InnerClassNode): InnerClass = InnerClass(asm.access, asm.name, asm.outerName, asm.innerName)

    implicit def asm2field(asm: FieldNode): Field = Field(asm.access, asm.name, asm.desc, asm.signature, asm.value)

    implicit def asm2method(asm: MethodNode): Method = {
      import scala.collection.JavaConversions._
      Method(asm.access, asm.name, asm.desc, asm.signature, asm.exceptions)
    }

    implicit def field2asm(field: Field): FieldNode = field.asm

    implicit def method2asm(method: Method): MethodNode = method.asm
  }

}
