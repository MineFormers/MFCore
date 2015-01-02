package de.mineformers.core.util.world

/**
 * Coord3
 *
 * @author PaleoCrafter
 */
abstract class Coord3[@specialized(Double, Int) N](implicit val numeric: Numeric[N]) {
  def x: N

  def y: N

  def z: N
}

trait Coord3Factory[N, C <: Coord3[N]] {
  def apply(x: N, y: N, z: N): C
}