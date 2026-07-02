package com.androiddrop.animation.engine

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Matriz 4x4 para transformaciones 3D (traslación, rotación, escala, perspectiva).
 *
 * POR QUÉ matriz 4x4 propia vs library externa: Las transformaciones que necesita
 * AndroidDrop son álgebra lineal básica (traslación de la esfera, rotación del
 * anillo del portal, perspectiva para proyección). Una implementación propia
 * evita añadir dependencias como Rajawali o libGDX para operaciones que son
 * ~100 líneas de código. La matriz se almacena en column-major order para
 * compatibilidad directa con OpenGL ES.
 *
 * POR QUÉ column-major: OpenGL ES espera matrices en orden column-major
 * (16 floats: col0, col1, col2, col3). Almacenar así evita transpuestas
 * innecesarias al enviar uniforms a shaders.
 *
 * @property m Array de 16 floats en orden column-major.
 */
class Matrix4(
    val m: FloatArray = FloatArray(16)
) {
    companion object {
        /**
         * Crea una matriz identidad 4x4.
         *
         * [ 1  0  0  0 ]
         * [ 0  1  0  0 ]
         * [ 0  0  1  0 ]
         * [ 0  0  0  1 ]
         */
        fun identity(): Matrix4 {
            val mat = FloatArray(16)
            mat[0] = 1f
            mat[5] = 1f
            mat[10] = 1f
            mat[15] = 1f
            return Matrix4(mat)
        }

        /**
         * Matriz de proyección ortográfica.
         *
         * @param left   Plano izquierdo del viewport.
         * @param right  Plano derecho del viewport.
         * @param bottom Plano inferior del viewport.
         * @param top    Plano superior del viewport.
         * @param near   Plano cercano (near clipping plane).
         * @param far    Plano lejano (far clipping plane).
         */
        fun orthographic(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Matrix4 {
            val mat = FloatArray(16)
            mat[0] = 2f / (right - left)
            mat[5] = 2f / (top - bottom)
            mat[10] = -2f / (far - near)
            mat[12] = -(right + left) / (right - left)
            mat[13] = -(top + bottom) / (top - bottom)
            mat[14] = -(far + near) / (far - near)
            mat[15] = 1f
            return Matrix4(mat)
        }

        /**
         * Matriz de proyección en perspectiva.
         *
         * Crea una matriz de perspectiva simétrica (frustum) con [fov] en
         * grados, [aspect] ratio (ancho/alto), y planos [near] y [far].
         *
         * @param fov    Campo de visión vertical en grados.
         * @param aspect Relación de aspecto (width / height).
         * @param near   Plano cercano (debe ser > 0).
         * @param far    Plano lejano.
         */
        fun perspective(fov: Float, aspect: Float, near: Float, far: Float): Matrix4 {
            val f = 1f / tan(fov * 0.5f * (Math.PI.toFloat() / 180f))
            val rangeInv = 1f / (near - far)

            val mat = FloatArray(16)
            mat[0] = f / aspect
            mat[5] = f
            mat[10] = (near + far) * rangeInv
            mat[11] = -1f
            mat[14] = 2f * near * far * rangeInv
            return Matrix4(mat)
        }
    }

    /**
     * Aplica una traslación a la matriz.
     *
     * Equivale a: this * T(tx, ty, tz)
     *
     * @param x Traslación en el eje X.
     * @param y Traslación en el eje Y.
     * @param z Traslación en el eje Z.
     * @return this para encadenamiento.
     */
    fun translate(x: Float, y: Float, z: Float): Matrix4 {
        m[12] += m[0] * x + m[4] * y + m[8] * z
        m[13] += m[1] * x + m[5] * y + m[9] * z
        m[14] += m[2] * x + m[6] * y + m[10] * z
        m[15] += m[3] * x + m[7] * y + m[11] * z
        return this
    }

    /**
     * Aplica una rotación alrededor de un eje.
     *
     * @param angle Ángulo de rotación en grados.
     * @param axisX Componente X del eje de rotación (debe estar normalizado).
     * @param axisY Componente Y del eje de rotación.
     * @param axisZ Componente Z del eje de rotación.
     * @return this para encadenamiento.
     */
    fun rotate(angle: Float, axisX: Float, axisY: Float, axisZ: Float): Matrix4 {
        val radians = angle * (Math.PI.toFloat() / 180f)
        val c = cos(radians)
        val s = sin(radians)
        val t = 1f - c

        val rot = FloatArray(16)
        rot[0] = t * axisX * axisX + c
        rot[1] = t * axisX * axisY + s * axisZ
        rot[2] = t * axisX * axisZ - s * axisY
        rot[4] = t * axisX * axisY - s * axisZ
        rot[5] = t * axisY * axisY + c
        rot[6] = t * axisY * axisZ + s * axisX
        rot[8] = t * axisX * axisZ + s * axisY
        rot[9] = t * axisY * axisZ - s * axisX
        rot[10] = t * axisZ * axisZ + c
        rot[15] = 1f

        multiplyInternal(rot)
        return this
    }

    /**
     * Aplica una escala uniforme o no uniforme.
     *
     * @param x Factor de escala en X.
     * @param y Factor de escala en Y.
     * @param z Factor de escala en Z.
     * @return this para encadenamiento.
     */
    fun scale(x: Float, y: Float, z: Float): Matrix4 {
        for (i in 0 until 4) {
            m[i * 4] *= x
            m[i * 4 + 1] *= y
            m[i * 4 + 2] *= z
        }
        return this
    }

    /**
     * Multiplica esta matriz por otra (this = this * other).
     *
     * @param other Matriz a multiplicar por la derecha.
     * @return Nueva matriz resultado de la multiplicación.
     */
    fun multiply(other: Matrix4): Matrix4 {
        val result = FloatArray(16)
        multiplyMatrices(m, other.m, result)
        return Matrix4(result)
    }

    /**
     * Calcula la inversa de esta matriz.
     *
     * Usa el algoritmo de cofactores (matriz adjunta / determinante).
     * Lanza excepción si la matriz es singular (determinante = 0).
     *
     * @return Nueva matriz inversa.
     * @throws ArithmeticException si la matriz no es invertible.
     */
    fun inverse(): Matrix4 {
        val inv = FloatArray(16)
        val det = computeInverse(m, inv)

        if (det == 0f) {
            throw ArithmeticException("Matriz singular: no se puede calcular la inversa")
        }

        return Matrix4(inv)
    }

    /**
     * Multiplica dos matrices 4x4 en orden column-major.
     */
    private fun multiplyMatrices(a: FloatArray, b: FloatArray, result: FloatArray) {
        for (col in 0 until 4) {
            for (row in 0 until 4) {
                var sum = 0f
                for (k in 0 until 4) {
                    sum += a[k * 4 + row] * b[col * 4 + k]
                }
                result[col * 4 + row] = sum
            }
        }
    }

    /**
     * Calcula la matriz inversa usando el método de cofactores.
     *
     * @return Determinante de la matriz. Si es 0, la matriz es singular.
     */
    private fun computeInverse(mat: FloatArray, inv: FloatArray): Float {
        inv[0] = mat[5] * mat[10] * mat[15] -
                mat[5] * mat[11] * mat[14] -
                mat[9] * mat[6] * mat[15] +
                mat[9] * mat[7] * mat[14] +
                mat[13] * mat[6] * mat[11] -
                mat[13] * mat[7] * mat[10]

        inv[4] = -mat[4] * mat[10] * mat[15] +
                mat[4] * mat[11] * mat[14] +
                mat[8] * mat[6] * mat[15] -
                mat[8] * mat[7] * mat[14] -
                mat[12] * mat[6] * mat[11] +
                mat[12] * mat[7] * mat[10]

        inv[8] = mat[4] * mat[9] * mat[15] -
                mat[4] * mat[11] * mat[13] -
                mat[8] * mat[5] * mat[15] +
                mat[8] * mat[7] * mat[13] +
                mat[12] * mat[5] * mat[11] -
                mat[12] * mat[7] * mat[9]

        inv[12] = -mat[4] * mat[9] * mat[14] +
                mat[4] * mat[10] * mat[13] +
                mat[8] * mat[5] * mat[14] -
                mat[8] * mat[6] * mat[13] -
                mat[12] * mat[5] * mat[10] +
                mat[12] * mat[6] * mat[9]

        inv[1] = -mat[1] * mat[10] * mat[15] +
                mat[1] * mat[11] * mat[14] +
                mat[9] * mat[2] * mat[15] -
                mat[9] * mat[3] * mat[14] -
                mat[13] * mat[2] * mat[11] +
                mat[13] * mat[3] * mat[10]

        inv[5] = mat[0] * mat[10] * mat[15] -
                mat[0] * mat[11] * mat[14] -
                mat[8] * mat[2] * mat[15] +
                mat[8] * mat[3] * mat[14] +
                mat[12] * mat[2] * mat[11] -
                mat[12] * mat[3] * mat[10]

        inv[9] = -mat[0] * mat[9] * mat[15] +
                mat[0] * mat[11] * mat[13] +
                mat[8] * mat[1] * mat[15] -
                mat[8] * mat[3] * mat[13] -
                mat[12] * mat[1] * mat[11] +
                mat[12] * mat[3] * mat[9]

        inv[13] = mat[0] * mat[9] * mat[14] -
                mat[0] * mat[10] * mat[13] -
                mat[8] * mat[1] * mat[14] +
                mat[8] * mat[2] * mat[13] +
                mat[12] * mat[1] * mat[10] -
                mat[12] * mat[2] * mat[9]

        inv[2] = mat[1] * mat[6] * mat[15] -
                mat[1] * mat[7] * mat[14] -
                mat[5] * mat[2] * mat[15] +
                mat[5] * mat[3] * mat[14] +
                mat[13] * mat[2] * mat[7] -
                mat[13] * mat[3] * mat[6]

        inv[6] = -mat[0] * mat[6] * mat[15] +
                mat[0] * mat[7] * mat[14] +
                mat[4] * mat[2] * mat[15] -
                mat[4] * mat[3] * mat[14] -
                mat[12] * mat[2] * mat[7] +
                mat[12] * mat[3] * mat[6]

        inv[10] = mat[0] * mat[5] * mat[15] -
                mat[0] * mat[7] * mat[13] -
                mat[4] * mat[1] * mat[15] +
                mat[4] * mat[3] * mat[13] +
                mat[12] * mat[1] * mat[7] -
                mat[12] * mat[3] * mat[5]

        inv[14] = -mat[0] * mat[5] * mat[14] +
                mat[0] * mat[6] * mat[13] +
                mat[4] * mat[1] * mat[14] -
                mat[4] * mat[2] * mat[13] -
                mat[12] * mat[1] * mat[6] +
                mat[12] * mat[2] * mat[5]

        inv[3] = -mat[1] * mat[6] * mat[11] +
                mat[1] * mat[7] * mat[10] +
                mat[5] * mat[2] * mat[11] -
                mat[5] * mat[3] * mat[10] -
                mat[9] * mat[2] * mat[7] +
                mat[9] * mat[3] * mat[6]

        inv[7] = mat[0] * mat[6] * mat[11] -
                mat[0] * mat[7] * mat[10] -
                mat[4] * mat[2] * mat[11] +
                mat[4] * mat[3] * mat[10] +
                mat[8] * mat[2] * mat[7] -
                mat[8] * mat[3] * mat[6]

        inv[11] = -mat[0] * mat[5] * mat[11] +
                mat[0] * mat[7] * mat[9] +
                mat[4] * mat[1] * mat[11] -
                mat[4] * mat[3] * mat[9] -
                mat[8] * mat[1] * mat[7] +
                mat[8] * mat[3] * mat[5]

        inv[15] = mat[0] * mat[5] * mat[10] -
                mat[0] * mat[6] * mat[9] -
                mat[4] * mat[1] * mat[10] +
                mat[4] * mat[2] * mat[9] +
                mat[8] * mat[1] * mat[6] -
                mat[8] * mat[2] * mat[5]

        var det = mat[0] * inv[0] + mat[1] * inv[4] + mat[2] * inv[8] + mat[3] * inv[12]

        if (det != 0f) {
            val invDet = 1f / det
            for (i in inv.indices) {
                inv[i] *= invDet
            }
        }

        return det
    }

    /**
     * Convierte la matriz a un FloatArray para enviar a shaders de OpenGL ES.
     *
     * @return FloatArray de 16 elementos en orden column-major.
     */
    fun toFloatArray(): FloatArray = m.copyOf()

    override fun toString(): String {
        val sb = StringBuilder("Matrix4[\n")
        for (row in 0 until 4) {
            sb.append("  [")
            for (col in 0 until 4) {
                sb.append(String.format("%8.4f", m[col * 4 + row]))
                if (col < 3) sb.append(", ")
            }
            sb.append("]\n")
        }
        sb.append("]")
        return sb.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Matrix4) return false
        return m.contentEquals(other.m)
    }

    override fun hashCode(): Int = m.contentHashCode()

    /** Multiplicación interna que modifica this.m in-place. */
    private fun multiplyInternal(other: FloatArray) {
        val result = FloatArray(16)
        multiplyMatrices(m, other, result)
        result.copyInto(m)
    }
}
