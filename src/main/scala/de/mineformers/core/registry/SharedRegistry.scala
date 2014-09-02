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
package de.mineformers.core.registry

import de.mineformers.core.util.DictEntry
import net.minecraftforge.oredict.OreDictionary

import scala.collection.{GenSet, mutable}

/**
 * SharedRegistry
 * @tparam K the key type
 * @tparam V the value type
 */
trait SharedRegistry[K, V] extends mutable.Iterable[(K, V)] {

  private val mappings: mutable.Map[K, V] = mutable.HashMap[K, V]()

  /**
   * @param key the key of the entry to get
   * @return the entry for the given key, null if it doesn't exist
   */
  def get(key: K): V = if (mappings contains key) mappings(key) else null.asInstanceOf[V]

  /**
   * For ease of use
   * @param key the key of the entry to get
   * @return same as [[get( k e y )]]
   */
  def apply(key: K): Option[V] = {
    val v = get(key)
    if (v == null)
      None
    else
      Some(v)
  }

  /**
   * Add a mapping
   * @param key the key for the mapping
   * @param value the mapping's value
   */
  def add(key: K, value: V): Unit = {
    mappings(key) = value
    put(key, value)
    value match {
      case o: DictEntry[_] =>
        o.dictionaryEntries foreach {
          e =>
            val name = e._1
            e._2 foreach {
              OreDictionary.registerOre(name, _)
            }
        }
      case _ =>
    }
  }

  /**
   * Perform the actual registration
   * @param key the mapping's key
   * @param value the mapping's value
   */
  protected def put(key: K, value: V): Unit

  /**
   * Filters the underlying mappings with the given condition
   * @param p the condition to use
   * @return an immutable Map containing ONLY the filtered values
   */
  def filter(p: (K, V) => Boolean): Map[K, V] = mappings.filter((kv: (K, V)) => p(kv._1, kv._2)).toMap

  /**
   * Filters the underlying mappings with the given condition
   * Returns the inverted map
   * @param p the condition to use
   * @return an immutable Map containing all values BUT the filtered ones
   */
  def filterNot(p: (K, V) => Boolean): Map[K, V] = mappings.filterNot((kv: (K, V)) => p(kv._1, kv._2)).toMap

  /**
   * Filters the underlying mappings for the given condition
   * @param p the condition to check the keys against
   * @return an immutable Map containing ONLY the filtered values
   */
  def filterKeys(p: K => Boolean): Map[K, V] = mappings.filterKeys(p).toMap

  /**
   * @return an iterator for all the mappings
   */
  override def iterator: Iterator[(K, V)] = mappings.iterator

  /**
   * @return a set of keys
   */
  def keySet: GenSet[K] = mappings.keySet

  /**
   * @return an iterator for all the keys
   */
  def keys: Iterable[K] = mappings.keys

  /**
   * @return an iterator for all the values
   */
  def values: Iterable[V] = mappings.values

}
