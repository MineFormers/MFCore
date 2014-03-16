package de.mineformers.core.registry

import scala.collection.{GenSet, mutable}

trait SharedRegistry[K, V] extends mutable.Iterable[(K, V)] {

  private val mappings: mutable.Map[K, V] = mutable.HashMap[K, V]()

  def get(key: K): V = if (mappings contains key) mappings(key) else null.asInstanceOf[V]

  def apply(key: K): V = get(key)

  def add(key: K, value: V): Unit = {
    mappings(key) = value
    put(key, value)
  }

  protected def put(key: K, value: V): Unit

  def filter(p: (K, V) => Boolean): Map[K, V] = mappings.filter((kv: (K, V)) => p(kv._1, kv._2)).toMap

  def filterNot(p: (K, V) => Boolean): Map[K, V] = mappings.filterNot((kv: (K, V)) => p(kv._1, kv._2)).toMap

  def filterKeys(p: K => Boolean): Map[K, V] = mappings.filterKeys(p).toMap

  override def iterator: Iterator[(K, V)] = mappings.iterator

  def keySet: GenSet[K] = mappings.keySet

  def keys: Iterable[K] = mappings.keys

  def values: Iterable[V] = mappings.values

}
