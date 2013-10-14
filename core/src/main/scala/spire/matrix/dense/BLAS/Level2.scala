/**
 * An implementation of the infamous Basic Linear Algebra System, level 2
 *
 * BLAS level 2 is concerned with those basic operations
 * that references O(n^2) elements and that performs O(n^2) flops,
 * where n is the size of the problem. Those operations are:
 *
 *   - product of a matrix and a vector
 *   - rank-1 updates
 *
 * The BLAS function declarations are altered as follow:
 *
 *   - we remove only the leading character (S, D, Z) indicating the type
 *     of elements, therefore keeping the next 2 characters indicating the type
 *     of matrices (GE for general, SY for symmetric, TR for triangular, ...)
 *   - we do not pass either the matrix and vector sizes, or vector stride,
 *     or matrix leading dimension as arguments as they are encapsulated
 *     in the matrix and vector arguments
 *   - the dummy CHARACTER*1 parameters such as TRANx are coded with
 *     enumerations instead
 *
 */
package spire.matrix.BLAS.level2

import spire.syntax.cfor._

import spire.matrix.dense.MatrixLike
import spire.matrix.dense.VectorLike
import spire.matrix.Transposition
import Transposition._

trait Interface {

  /**
   * This performs the following operation involving the matrix A,
   * the vectors x and y and the scalars $\alpha$ and $\beta$
   * \[
   *     y := \alpha op(A) x + beta y
   * \]
   * where $op(A)=A$ or $op(A)=A^T$ depending on the value
   * of the argument `trans`.
   */
   def gemv(trans: Transposition.Value,
            alpha: Double, a: MatrixLike,
            x: VectorLike, beta: Double, y: VectorLike): Unit

   /**
    * This perform the rank-1 update
    * \[
    *     A := alpha x y^T + A
    * \]
    * where A is a matrix, x and y are vectors and $\alpha$ is a scalar.
    */
   def ger(alpha: Double, x: VectorLike, y: VectorLike, a: MatrixLike): Unit
}

trait Naive extends Interface {

   def gemv(trans: Transposition.Value,
            alpha: Double, a: MatrixLike,
            x: VectorLike, beta: Double, y: VectorLike): Unit = {
    if (trans == NoTranspose)
      require(y.length == a.dimensions._1 && a.dimensions._2 == x.length)
    else
      require(y.length == a.dimensions._2 && a.dimensions._1 == x.length)

    // y := beta y
    if(beta == 0)
      cforRange(0 until y.length) { i => y(i) = 0 }
    else if(beta != 1)
      cforRange(0 until y.length) { i => y(i) *= beta }

    // y += alpha op(A) x
    if(alpha != 0) {
      val (m, n) = a.dimensions
      if(trans == NoTranspose) {
        cforRange(0 until n) { j =>
          if (x(j) != 0) {
            val t = alpha * x(j)
            cforRange(0 until m) { i => y(i) += t * a(i,j) }
          }
        }
      } else {
        cforRange(0 until n) { j =>
          var t = 0.0
          cforRange(0 until m) { i => t += a(i, j) * x(i) }
          y(j) += alpha * t
        }
      }
    }
   }

  def ger(alpha: Double, x: VectorLike, y: VectorLike, a: MatrixLike): Unit = {
    require((x.length, y.length) == a.dimensions)

    cforRange(0 until y.length) { j =>
      if (y(j) != 0) {
        val t = alpha * y(j)
        cforRange(0 until x.length) { i => a(i, j) += x(i) * t }
      }
    }
  }
}