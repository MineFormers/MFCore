package de.mineformers.core.util.collection

/**
 * HMap
 *
 * @author PaleoCrafter
 */
trait HMap[TypedKey[_]] {
  self =>
  def get[T](key: TypedKey[T]): Option[T]

  def put[T](key: TypedKey[T], value: T): HMap[TypedKey]

  def keySet: Set[TypedKey[_]]

  def toMap: Map[TypedKey[_], AnyRef]
}

object HMap {

  private[collection] class WrappedMap[TypedKey[_]](m: Map[TypedKey[_], AnyRef])
    extends HMap[TypedKey] {
    def get[T](key: TypedKey[T]) = m.get(key).asInstanceOf[Option[T]]

    def put[T](key: TypedKey[T], value: T) =
      new WrappedMap[TypedKey](m.updated(key, value.asInstanceOf[AnyRef]))

    def keySet = m.keySet

    def toMap = m
  }

  def empty[TypedKey[_]]: HMap[TypedKey] = new WrappedMap[TypedKey](Map())
}
