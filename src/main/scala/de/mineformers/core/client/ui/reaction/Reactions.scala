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

package de.mineformers.core.client.ui.reaction

import scala.collection.mutable.{ListBuffer, Buffer}

object Reactions {

  class Impl extends Reactions {
    private val parts: Buffer[Reaction] = new ListBuffer[Reaction]

    def isDefinedAt(e: Event) = parts.exists(_ isDefinedAt e)

    def +=(r: Reaction): this.type = {
      parts += r; this
    }

    def -=(r: Reaction): this.type = {
      parts -= r; this
    }

    def apply(e: Event) {
      for (p <- parts) if (p isDefinedAt e) p(e)
    }
  }

  type Reaction = PartialFunction[Event, Unit]

  /**
   * A Reaction implementing this trait is strongly referenced in the reaction list
   */
  trait StronglyReferenced

  class Wrapper(listener: Any)(r: Reaction) extends Reaction with StronglyReferenced with Proxy {
    def self = listener

    def isDefinedAt(e: Event) = r.isDefinedAt(e)

    def apply(e: Event) {
      r(e)
    }
  }

}

/**
 * Used by reactors to let clients register custom event reactions.
 */
abstract class Reactions extends Reactions.Reaction {
  /**
   * Add a reaction.
   */
  def +=(r: Reactions.Reaction): this.type

  /**
   * Remove the given reaction.
   */
  def -=(r: Reactions.Reaction): this.type
}