package de.mineformers.core.client.ui.state

import de.mineformers.core.util.collection.{MutableHMap, HMap}

/**
 * ComponentState
 *
 * @author PaleoCrafter
 */
trait ComponentState {
  def propertyNames: Set[String]

  def apply[T](property: Property[T]): T

  def get[T](property: Property[T]): Option[T]

  def byName(name: String): Option[Any]

  def set[T](property: Property[T], value: T): ComponentState

  def cycle[T](property: Property[T]): ComponentState

  def properties: HMap[Property]

  override def toString: String = "{" + propertyNames.map(s => s"$s: ${byName(s).orNull}").mkString(",") + "}"
}

object ComponentState {

  private class Impl extends ComponentState {
    override def propertyNames: Set[String] = data.keySet map {
      _.name
    }

    override def set[T](property: Property[T], value: T): ComponentState = {
      if ((propertyNames.contains(property.name) && data.keySet.contains(property)) || !propertyNames.contains(property.name)) {
        if (property.allowedValues == null || property.allowedValues.contains(value))
          data.put(property, value)
        else
          throw new IllegalArgumentException("Value " + property.nameFrom(value) + " isn't allowed for property " + property.name + ".")
      }
      else
        throw new IllegalArgumentException("Property " + property.name + " already exists in this state with another type.")
      this
    }

    override def properties: HMap[Property] = data.immutable

    override def apply[T](property: Property[T]): T = get(property) match {
      case Some(v) => v
      case _ => null.asInstanceOf[T]
    }

    override def get[T](property: Property[T]): Option[T] = properties.get(property)

    override def byName(name: String): Option[Any] = data.keySet.find(_.name == name) match {
      case Some(p) => get(p)
      case None => None
    }

    override def cycle[T](property: Property[T]): ComponentState = {
      val currentValue = this(property)
      if (property.allowedValues != null) {
        val index = property.allowedValues.indexOf(currentValue)
        if (index == property.allowedValues.length - 1)
          set(property, property.allowedValues.head)
        else
          set(property, property.allowedValues(index + 1))
      } else
        throw new IllegalArgumentException("Property " + property.name + " can't be cycled, it doesn't have a predefined set of values.")
      this
    }

    private val data = MutableHMap.empty[Property]
  }

  def create(properties: Property[_]*): ComponentState = {
    val impl = new Impl
    properties.foreach(p => impl.set(p, p.defaultValue))
    impl
  }
}