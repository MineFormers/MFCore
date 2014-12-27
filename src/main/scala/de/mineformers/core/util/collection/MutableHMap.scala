package de.mineformers.core.util.collection

import scala.collection.mutable

/**
 * HMap
 *
 * @author PaleoCrafter
 */
trait MutableHMap[TypedKey[_]] {
  self =>
  def get[T](key: TypedKey[T]): Option[T]

  def put[T](key: TypedKey[T], value: T): Unit

  def immutable: HMap[TypedKey]

  def keySet: Set[TypedKey[_]]
}

object MutableHMap {

  private class WrappedMap[TypedKey[_]](m: mutable.Map[TypedKey[_], AnyRef])
    extends MutableHMap[TypedKey] {
    def get[T](key: TypedKey[T]) = m.get(key).asInstanceOf[Option[T]]

    def put[T](key: TypedKey[T], value: T) = m.put(key, value.asInstanceOf[AnyRef])

    def keySet = m.keySet.toSet

    override def immutable: HMap[TypedKey] = new HMap.WrappedMap[TypedKey](m.toMap)
  }

  def empty[TypedKey[_]]: MutableHMap[TypedKey] = new WrappedMap[TypedKey](mutable.Map())
}
