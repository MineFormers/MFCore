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
package de.mineformers.core.util.math.shape2d

import de.mineformers.core.util.math.shape2d.Polygon.{Corner, Edge, Edges}

import scala.collection.mutable

/**
 * Polygon
 *
 * @author PaleoCrafter
 */
object Polygon {
  private val cache = new mutable.HashMap[Edges, Polygon]
  type Edges = Seq[Edge]

  /**
   * Create a new [[Polygon]] based on the given points
   * @param edges a sequence of the edges this polygon consists of
   * @return a [[Polygon]] instance, either a new one or one from the cache
   */
  def apply(edges: Edges): Polygon = cache.getOrElseUpdate(edges, new Polygon(edges))

  case class Corner(e1: Edge, e2: Edge) {
    def convex: Boolean = (e1.p1.x == e1.p2.x && e2.p2.y == e2.p1.y) || (e2.p2.x == e2.p1.x && e1.p1.y == e1.p2.y)

    def concave: Boolean = !convex
  }

  case class Edge(p1: Point, p2: Point) {
  }

}

class Polygon(val edges: Edges) extends Shape[Polygon] {
  def nEdges = edges.size

  def nPoints = nEdges

  lazy val points: Seq[Point] = edges.map(_.p1)
  lazy val corners: Seq[Corner] = edges.sliding(2).map({ case Seq(e1, e2) => Corner(e1, e2)}).toSeq :+ Corner(edges(0), edges.last)

  override def intersect(r: Rectangle): Option[Polygon] = None

  override def contains(p: Point): Boolean = {
    var result: Boolean = false
    edges.foreach { case Edge(last, current) =>
      if ((current.y > p.y) != (last.y > p.y) && (p.x < (last.x - current.x) * (p.y - current.y) / (last.y - current.y) + current.x)) {
        result = !result
      }
    }
    result
  }

  override def contains(r: Rectangle): Boolean = false

  override def bounds: Rectangle = _bounds

  override def translate(p: Point): Polygon = Polygon(edges.map(e => Edge(e.p1 + p, e.p2)))

  lazy val _bounds: Rectangle = {
    if (nEdges == 0)
      Rectangle(0, 0, 0, 0)
    else {
      var boundsMinX: Int = Integer.MAX_VALUE
      var boundsMinY: Int = Integer.MAX_VALUE
      var boundsMaxX: Int = Integer.MIN_VALUE
      var boundsMaxY: Int = Integer.MIN_VALUE
      for (e <- edges) {
        boundsMinX = Math.min(boundsMinX, e.p1.x)
        boundsMaxX = Math.max(boundsMaxX, e.p1.x)
        boundsMinY = Math.min(boundsMinY, e.p1.y)
        boundsMaxY = Math.max(boundsMaxY, e.p1.y)
      }
      Rectangle(boundsMinX, boundsMinY, boundsMaxX, boundsMaxY)
    }
  }
}
