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

import org.objectweb.asm.tree._
import org.objectweb.asm.{Label, Opcodes, tree}

import scala.language.implicitConversions

abstract sealed class Instruction[+A <: AbstractInsnNode](opcode: Int) {
  def asm: A
}

object Instruction {
  implicit def seq2insList[A <: Instruction[AbstractInsnNode]](seq: Seq[A]): InsnList = {
    val list = new InsnList
    val iterator = seq.iterator
    while (iterator.hasNext)
      list add iterator.next().asm
    list
  }

  implicit def insList2seq[A <: Instruction[_]](insnList: InsnList): Seq[A] = {
    val builder = Seq.newBuilder[A]
    val iterator = insnList.iterator
    import de.mineformers.core.asm.util.Instruction.Conversions.asm2any
    while (iterator.hasNext)
      builder += asm2any(iterator.next()).asInstanceOf[A]
    builder.result()
  }

  import org.objectweb.asm.Opcodes._

  val legalSimpleOpcodes = Seq(NOP, ACONST_NULL, ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5, LCONST_0, LCONST_1, FCONST_0, FCONST_1, FCONST_2, DCONST_0, DCONST_1, IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD, IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE, SASTORE, POP, POP2, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2, SWAP, IADD, LADD, FADD, DADD, ISUB, LSUB, FSUB, DSUB, IMUL, LMUL, FMUL, DMUL, IDIV, LDIV, FDIV, DDIV, IREM, LREM, FREM, DREM, INEG, LNEG, FNEG, DNEG, ISHL, LSHL, ISHR, LSHR, IUSHR, LUSHR, IAND, LAND, IOR, LOR, IXOR, LXOR, I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I, D2L, D2F, I2B, I2C, I2S, LCMP, FCMPL, FCMPG, DCMPL, DCMPG, IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN, ARRAYLENGTH, ATHROW, MONITORENTER, MONITOREXIT)
  val legalVarOpcodes = Seq(ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE, LSTORE, FSTORE, DSTORE, ASTORE, RET)
  val legalIntOpcodes = Seq(BIPUSH, SIPUSH, NEWARRAY)
  val legalFieldOpcodes = Seq(GETSTATIC, PUTSTATIC, GETFIELD, PUTFIELD)
  val legalMethodOpcodes = Seq(INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC, INVOKEINTERFACE)
  val legalJumpOpcodes = Seq(IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE, GOTO, JSR, IFNULL, IFNONNULL)
  val legalTypeOpcodes = Seq(NEW, ANEWARRAY, CHECKCAST, INSTANCEOF)

  case class Simple(opcode: Int) extends Instruction[InsnNode](opcode) {
    require(legalSimpleOpcodes contains opcode)
    override lazy val asm: InsnNode = new InsnNode(opcode)
  }

  object LabelOp {
    def apply(): LabelOp = new LabelOp()
  }

  case class LabelOp(label: Label) extends Instruction[LabelNode](-1) {
    def this() = this(null)

    override lazy val asm: LabelNode = new LabelNode(label)
  }

  case class Var(opcode: Int, variable: Int) extends Instruction[VarInsnNode](opcode) {
    require(legalVarOpcodes contains opcode)
    override lazy val asm: VarInsnNode = new VarInsnNode(opcode, variable)
  }

  case class IntOp(opcode: Int, operand: Int) extends Instruction[IntInsnNode](opcode) {
    require(legalIntOpcodes contains opcode)
    override lazy val asm: IntInsnNode = new IntInsnNode(opcode, operand)
  }

  case class FieldOp(opcode: Int, owner: String, name: String, desc: String) extends Instruction[FieldInsnNode](opcode) {
    require(legalFieldOpcodes contains opcode)
    override lazy val asm: FieldInsnNode = new FieldInsnNode(opcode, owner, name, desc)
  }

  case class MethodOp(opcode: Int, owner: String, name: String, desc: String) extends Instruction[MethodInsnNode](opcode) {
    require(legalMethodOpcodes contains opcode)
    override lazy val asm: MethodInsnNode = new MethodInsnNode(opcode, owner, name, desc)
  }

  case class Jump(opcode: Int, label: LabelOp) extends Instruction[JumpInsnNode](opcode) {
    require(legalJumpOpcodes contains opcode)
    override lazy val asm: JumpInsnNode = new JumpInsnNode(opcode, label.asm)
  }

  case class TypeOp(opcode: Int, desc: String) extends Instruction[TypeInsnNode](opcode) {
    require(legalTypeOpcodes contains opcode)
    override lazy val asm: TypeInsnNode = new TypeInsnNode(opcode, desc)
  }

  case class Ldc(cst: Any) extends Instruction[LdcInsnNode](Opcodes.LDC) {
    require(cst match { case _: String | _: Int | _: Float | _: Long => true
    case _ => false
    })
    override lazy val asm: LdcInsnNode = new LdcInsnNode(cst)
  }

  case class IInc(variable: Int, incr: Int) extends Instruction[IincInsnNode](IINC) {
    override lazy val asm: IincInsnNode = new IincInsnNode(variable, incr)
  }

  case class MultiArray(desc: String, dims: Int) extends Instruction[MultiANewArrayInsnNode](MULTIANEWARRAY) {
    override lazy val asm: MultiANewArrayInsnNode = new MultiANewArrayInsnNode(desc, dims)
  }

  case class TableSwitch(min: Int, max: Int, default: LabelOp, labels: LabelOp*) extends Instruction[TableSwitchInsnNode](TABLESWITCH) {
    private val asmLabels = labels map {
      _.asm
    }
    override lazy val asm: TableSwitchInsnNode = new TableSwitchInsnNode(min, max, default.asm, asmLabels: _*)
  }

  case class LookupSwitch(default: LabelOp, keys: Traversable[Int], labels: Traversable[LabelOp]) extends Instruction[LookupSwitchInsnNode](LOOKUPSWITCH) {
    private val asmLabels = labels map {
      _.asm
    }
    override lazy val asm: LookupSwitchInsnNode = new LookupSwitchInsnNode(default.asm, keys.toArray, asmLabels.toArray)
  }

  object Conversions {
    def converter[A <: AbstractInsnNode](n: A) = n match {
      case node: InsnNode => asm2simple(node)
      case node: LabelNode => asm2label(node)
      case node: VarInsnNode => asm2var(node)
      case node: IntInsnNode => asm2int(node)
      case node: FieldInsnNode => asm2field(node)
      case node: MethodInsnNode => asm2method(node)
      case node: JumpInsnNode => asm2jump(node)
      case node: TypeInsnNode => asm2type(node)
      case node: LdcInsnNode => asm2ldc(node)
      case node: IincInsnNode => asm2iinc(node)
      case node: MultiANewArrayInsnNode => asm2multiArray(node)
      case node: TableSwitchInsnNode => asm2tableSwitch(node)
      case node: LookupSwitchInsnNode => asm2lookupSwitch(node)
    }

    implicit def asm2any[A <: AbstractInsnNode](asm: A): Instruction[A] = converter(asm).asInstanceOf[Instruction[A]]

    implicit def asm2simple(asm: InsnNode): Simple = Simple(asm.getOpcode)

    implicit def asm2label(asm: LabelNode): LabelOp = LabelOp(asm.getLabel)

    implicit def asm2var(asm: VarInsnNode): Var = Var(asm.getOpcode, asm.`var`)

    implicit def asm2int(asm: IntInsnNode): IntOp = IntOp(asm.getOpcode, asm.operand)

    implicit def asm2field(asm: FieldInsnNode): FieldOp = FieldOp(asm.getOpcode, asm.owner, asm.name, asm.desc)

    implicit def asm2method(asm: MethodInsnNode): MethodOp = MethodOp(asm.getOpcode, asm.owner, asm.name, asm.desc)

    implicit def asm2jump(asm: JumpInsnNode): Jump = Jump(asm.getOpcode, asm.label)

    implicit def asm2type(asm: TypeInsnNode): TypeOp = TypeOp(asm.getOpcode, asm.desc)

    implicit def asm2ldc(asm: LdcInsnNode): Ldc = Ldc(asm.getOpcode, asm.cst)

    implicit def asm2iinc(asm: IincInsnNode): IInc = IInc(asm.`var`, asm.incr)

    implicit def asm2multiArray(asm: MultiANewArrayInsnNode): MultiArray = MultiArray(asm.desc, asm.dims)

    implicit def asm2tableSwitch(asm: TableSwitchInsnNode): TableSwitch = {
      import scala.collection.JavaConversions._
      TableSwitch(asm.min, asm.max, asm.dflt, asm.labels map asm2label: _*)
    }

    implicit def asm2lookupSwitch(asm: LookupSwitchInsnNode): LookupSwitch = {
      import scala.collection.JavaConversions._
      LookupSwitch(asm.dflt, asm.keys map {
        _.intValue()
      }, asm.labels map asm2label)
    }

    implicit def any2asm[A <: AbstractInsnNode, B <: Instruction[A]](instruction: B): A = instruction.asm
  }

}