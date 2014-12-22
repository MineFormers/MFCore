package de.mineformers.core.client.renderer.shape

import de.mineformers.core.client.renderer.RenderParams

/**
 * Sphere
 *
 * @author PaleoCrafter
 */
class Sphere(radius: Double, accuracy: Int, filled: Boolean = true) extends Shape(List(new Circle(radius, accuracy, filled)), RenderParams.start().face().build())
