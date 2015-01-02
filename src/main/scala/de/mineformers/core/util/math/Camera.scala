package de.mineformers.core.util.math

import de.mineformers.core.util.math.shape2d.Rectangle

/**
 * Camera
 *
 * @author PaleoCrafter
 */
class Camera(var viewport: Rectangle, private var _projectionMatrix: Matrix4, private var _viewMatrix: Matrix4) {
  private var viewInverse: Matrix4 = _
  private var projectionInverse: Matrix4 = _
  private var viewTranspose: Matrix4 = _
  private var projectionTranspose: Matrix4 = _

  def isValid = viewMatrix != null && projectionMatrix != null && viewport != null

  def eyePoint: Vector3 = {
    val ivm = invertedViewMatrix
    if (ivm == null) {
      return null
    }
    val ipm = invertedProjectionMatrix
    if (ipm == null) {
      return null
    }
    ivm * ipm

    ivm.translation
  }

  def projectionMatrix = _projectionMatrix

  def projectionMatrix_=(m: Matrix4): Unit = {
    _projectionMatrix = m
    if (m != null) {
      projectionTranspose = m.transpose
      projectionInverse = m.inverse
    }
  }

  def invertedProjectionMatrix: Matrix4 = {
    if (projectionMatrix != null) {
      if (projectionInverse == null) {
        projectionInverse = projectionMatrix.inverse
      }
      projectionInverse
    } else {
      null
    }
  }

  def transposedProjectionMatrix: Matrix4 = {
    if (projectionMatrix != null) {
      if (projectionTranspose == null) {
        projectionTranspose = projectionMatrix.transpose
      }
      projectionTranspose
    } else {
      null
    }
  }

  def viewMatrix = _viewMatrix

  def viewMatrix_=(m: Matrix4): Unit = {
    _viewMatrix = m
    if (m != null) {
      projectionTranspose = m.transpose
      projectionInverse = m.inverse
    }
  }

  def invertedViewMatrix: Matrix4 = {
    if (viewMatrix != null) {
      if (viewInverse == null) {
        viewInverse = viewMatrix.inverse
      }
      viewInverse
    } else {
      null
    }
  }

  def transposedViewMatrix: Matrix4 = {
    if (viewMatrix != null) {
      if (viewTranspose == null) {
        viewTranspose = viewMatrix.transpose
      }
      viewTranspose
    } else {
      null
    }
  }
}
