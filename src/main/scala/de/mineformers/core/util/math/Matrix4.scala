package de.mineformers.core.util.math

/**
 * Matrix4
 *
 * @author PaleoCrafter
 */
class Matrix4(private val data: Array[Array[Double]]) {
  def this(data: Double*) = this(Array(
    Array(data(0), data(1), data(2), data(3)),
    Array(data(4), data(5), data(6), data(7)),
    Array(data(8), data(9), data(10), data(11)),
    Array(data(12), data(13), data(14), data(15))))

  def apply(x: Int, y: Int) = data(x)(y)

  def set(x: Int, y: Int, value: Double) = {
    val dataCopy = data.clone()
    dataCopy(x)(y) = value
    new Matrix4(dataCopy.flatten: _*)
  }

  def inverse = {
    val det = determinant
    if (det == 0) {
      throw new RuntimeException("Cannot invert matrix with a determinant of 0.")
    }
    val detInv = 1f / det

    // first row
    val t00 = determinant3x3(this(1, 1), this(2, 1), this(3, 1), this(1, 2), this(2, 2), this(3, 2), this(1, 3), this(2, 3), this(3, 3))
    val t01 = -determinant3x3(this(0, 1), this(2, 1), this(3, 1), this(0, 2), this(2, 2), this(3, 2), this(0, 3), this(2, 3), this(3, 3))
    val t02 = determinant3x3(this(0, 1), this(1, 1), this(3, 1), this(0, 2), this(1, 2), this(3, 2), this(0, 3), this(1, 3), this(3, 3))
    val t03 = -determinant3x3(this(0, 1), this(1, 1), this(2, 1), this(0, 2), this(1, 2), this(2, 2), this(0, 3), this(1, 3), this(2, 3))
    // second row
    val t10 = -determinant3x3(this(1, 0), this(2, 0), this(3, 0), this(1, 2), this(2, 2), this(3, 2), this(1, 3), this(2, 3), this(3, 3))
    val t11 = determinant3x3(this(0, 0), this(2, 0), this(3, 0), this(0, 2), this(2, 2), this(3, 2), this(0, 3), this(2, 3), this(3, 3))
    val t12 = -determinant3x3(this(0, 0), this(1, 0), this(3, 0), this(0, 2), this(1, 2), this(3, 2), this(0, 3), this(1, 3), this(3, 3))
    val t13 = determinant3x3(this(0, 0), this(1, 0), this(2, 0), this(0, 2), this(1, 2), this(2, 2), this(0, 3), this(1, 3), this(2, 3))
    // third row
    val t20 = determinant3x3(this(1, 0), this(2, 0), this(3, 0), this(1, 1), this(2, 1), this(3, 1), this(1, 3), this(2, 3), this(3, 3))
    val t21 = -determinant3x3(this(0, 0), this(2, 0), this(3, 0), this(0, 1), this(2, 1), this(3, 1), this(0, 3), this(2, 3), this(3, 3))
    val t22 = determinant3x3(this(0, 0), this(1, 0), this(3, 0), this(0, 1), this(1, 1), this(3, 1), this(0, 3), this(1, 3), this(3, 3))
    val t23 = -determinant3x3(this(0, 0), this(1, 0), this(2, 0), this(0, 1), this(1, 1), this(2, 1), this(0, 3), this(1, 3), this(2, 3))
    // fourth row
    val t30 = -determinant3x3(this(1, 0), this(2, 0), this(3, 0), this(1, 1), this(2, 1), this(3, 1), this(1, 2), this(2, 2), this(3, 2))
    val t31 = determinant3x3(this(0, 0), this(2, 0), this(3, 0), this(0, 1), this(2, 1), this(3, 1), this(0, 2), this(2, 2), this(3, 2))
    val t32 = -determinant3x3(this(0, 0), this(1, 0), this(3, 0), this(0, 1), this(1, 1), this(3, 1), this(0, 2), this(1, 2), this(3, 2))
    val t33 = determinant3x3(this(0, 0), this(1, 0), this(2, 0), this(0, 1), this(1, 1), this(2, 1), this(0, 2), this(1, 2), this(2, 2))

    new Matrix4(
      t00 * detInv, t01 * detInv, t02 * detInv, t03 * detInv,
      t10 * detInv, t11 * detInv, t12 * detInv, t13 * detInv,
      t20 * detInv, t21 * detInv, t22 * detInv, t23 * detInv,
      t30 * detInv, t31 * detInv, t32 * detInv, t33 * detInv)
  }

  def determinant = {
    var result =
      this(0, 0) * ((this(1, 1) * this(2, 2) * this(3, 3) + this(2, 1) * this(3, 2) * this(1, 3) + this(3, 1) * this(1, 2) * this(2, 3))
        - this(3, 1) * this(2, 2) * this(1, 3)
        - this(1, 1) * this(3, 2) * this(2, 3)
        - this(2, 1) * this(1, 2) * this(3, 3))
    result -= this(1, 0) * ((this(0, 1) * this(2, 2) * this(3, 3) + this(2, 1) * this(3, 2) * this(0, 3) + this(3, 1) * this(0, 2) * this(2, 3))
      - this(3, 1) * this(2, 2) * this(0, 3)
      - this(0, 1) * this(3, 2) * this(2, 3)
      - this(2, 1) * this(0, 2) * this(3, 3))
    result += this(2, 0) * ((this(0, 1) * this(1, 2) * this(3, 3) + this(1, 1) * this(3, 2) * this(0, 3) + this(3, 1) * this(0, 2) * this(1, 3))
      - this(3, 1) * this(1, 2) * this(0, 3)
      - this(0, 1) * this(3, 2) * this(1, 3)
      - this(1, 1) * this(0, 2) * this(3, 3))
    result -= this(3, 0) * ((this(0, 1) * this(1, 2) * this(2, 3) + this(1, 1) * this(2, 2) * this(0, 3) + this(2, 1) * this(0, 2) * this(1, 3))
      - this(2, 1) * this(1, 2) * this(0, 3)
      - this(0, 1) * this(2, 2) * this(1, 3)
      - this(1, 1) * this(0, 2) * this(2, 3))
    result
  }

  private def determinant3x3(e00: Double, e01: Double, e02: Double, e10: Double, e11: Double, e12: Double, e20: Double, e21: Double, e22: Double) = {
    e00 * (e11 * e22 - e12 * e21) + e01 * (e12 * e20 - e10 * e22) + e02 * (e10 * e21 - e11 * e20)
  }

  def *(m: Matrix4) = {
    val r00 = this(0, 0) * m(0, 0) + this(0, 1) * m(1, 0) + this(0, 2) * m(2, 0) + this(0, 3) * m(3, 0)
    val r01 = this(0, 0) * m(0, 1) + this(0, 1) * m(1, 1) + this(0, 2) * m(2, 1) + this(0, 3) * m(3, 1)
    val r02 = this(0, 0) * m(0, 2) + this(0, 1) * m(1, 2) + this(0, 2) * m(2, 2) + this(0, 3) * m(3, 2)
    val r03 = this(0, 0) * m(0, 3) + this(0, 1) * m(1, 3) + this(0, 2) * m(2, 3) + this(0, 3) * m(3, 3)

    val r10 = this(1, 0) * m(0, 0) + this(1, 1) * m(1, 0) + this(1, 2) * m(2, 0) + this(1, 3) * m(3, 0)
    val r11 = this(1, 0) * m(0, 1) + this(1, 1) * m(1, 1) + this(1, 2) * m(2, 1) + this(1, 3) * m(3, 1)
    val r12 = this(1, 0) * m(0, 2) + this(1, 1) * m(1, 2) + this(1, 2) * m(2, 2) + this(1, 3) * m(3, 2)
    val r13 = this(1, 0) * m(0, 3) + this(1, 1) * m(1, 3) + this(1, 2) * m(2, 3) + this(1, 3) * m(3, 3)

    val r20 = this(2, 0) * m(0, 0) + this(2, 1) * m(1, 0) + this(2, 2) * m(2, 0) + this(2, 3) * m(3, 0)
    val r21 = this(2, 0) * m(0, 1) + this(2, 1) * m(1, 1) + this(2, 2) * m(2, 1) + this(2, 3) * m(3, 1)
    val r22 = this(2, 0) * m(0, 2) + this(2, 1) * m(1, 2) + this(2, 2) * m(2, 2) + this(2, 3) * m(3, 2)
    val r23 = this(2, 0) * m(0, 3) + this(2, 1) * m(1, 3) + this(2, 2) * m(2, 3) + this(2, 3) * m(3, 3)

    val r30 = this(3, 0) * m(0, 0) + this(3, 1) * m(1, 0) + this(3, 2) * m(2, 0) + this(3, 3) * m(3, 0)
    val r31 = this(3, 0) * m(0, 1) + this(3, 1) * m(1, 1) + this(3, 2) * m(2, 1) + this(3, 3) * m(3, 1)
    val r32 = this(3, 0) * m(0, 2) + this(3, 1) * m(1, 2) + this(3, 2) * m(2, 2) + this(3, 3) * m(3, 2)
    val r33 = this(3, 0) * m(0, 3) + this(3, 1) * m(1, 3) + this(3, 2) * m(2, 3) + this(3, 3) * m(3, 3)

    new Matrix4(
      r00, r01, r02, r03,
      r10, r11, r12, r13,
      r20, r21, r22, r23,
      r30, r31, r32, r33)
  }

  def translation = Vector3(this(0, 3), this(1, 3), this(2, 3))

  def transpose = new Matrix4(
    this(0, 0), this(1, 0), this(2, 0), this(3, 0),
    this(0, 1), this(1, 1), this(2, 1), this(3, 1),
    this(0, 2), this(1, 2), this(2, 2), this(3, 2),
    this(0, 3), this(1, 3), this(2, 3), this(3, 3))

  def transformNormal(normal: Vector3) = {
    val x = this(0, 0) * normal.x + this(0, 1) * normal.y + this(0, 2) * normal.z
    val y = this(1, 0) * normal.x + this(1, 1) * normal.y + this(1, 2) * normal.z
    val z = this(2, 0) * normal.x + this(2, 1) * normal.y + this(2, 2) * normal.z
    Vector3(x, y, z)
  }

  def withTranslation(translation: Vector3) = set(0, 3, translation.x).set(1, 3, translation.y).set(2, 3, translation.z)
}

object Matrix4 {
  final val Zero = new Matrix4(
    0, 0, 0, 0,
    0, 0, 0, 0,
    0, 0, 0, 0,
    0, 0, 0, 0)
}