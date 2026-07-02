package com.androiddrop.animation.engine

/**
 * Nodo base del árbol de renderizado.
 *
 * POR QUÉ un árbol de nodos vs renderizado plano: El árbol permite construir
 * jerarquías de transformación donde los hijos heredan la transformación del
 * padre. Esto es esencial para el portal (anillo exterior que contiene el
 * vórtice, que a su vez contiene partículas) y para la esfera (que puede
 * tener sub-elementos como aros de energía orbitando).
 *
 * Cada [RenderNode] tiene su propia transformación (matriz 4x4), alpha y
 * visibilidad. La transformación se compone con la del padre durante el
 * renderizado.
 *
 * @property transform Matriz de transformación local del nodo (4x4).
 * @property alpha Opacidad del nodo (0f = transparente, 1f = opaco).
 * @property visible Control de visibilidad. Si es false, el nodo y sus
 *                   hijos no se renderizan.
 */
abstract class RenderNode {

    /** Matriz de transformación local (modelo). Por defecto identidad. */
    var transform: Matrix4 = Matrix4.identity()

    /** Opacidad del nodo. Afecta al blending final. */
    var alpha: Float = 1f

    /** Visibilidad del nodo y sus descendientes. */
    var visible: Boolean = true

    /** Lista de nodos hijos en el árbol de renderizado. */
    private val children: MutableList<RenderNode> = mutableListOf()

    /**
     * Actualiza la lógica del nodo (físicas, animaciones, etc.).
     *
     * @param deltaTime Tiempo transcurrido desde el último frame en segundos.
     */
    abstract fun update(deltaTime: Float)

    /**
     * Renderiza el nodo en el contexto gráfico actual.
     *
     * La implementación debe dibujar la geometría del nodo usando su
     * [transform] y [alpha] actuales.
     */
    abstract fun render()

    /**
     * Actualiza este nodo y todos sus descendientes en orden jerárquico.
     *
     * @param deltaTime Tiempo transcurrido desde el último frame en segundos.
     */
    fun updateTree(deltaTime: Float) {
        if (!visible) return
        update(deltaTime)
        for (child in children) {
            child.updateTree(deltaTime)
        }
    }

    /**
     * Renderiza este nodo y todos sus descendientes en orden jerárquico.
     *
     * Los hijos se renderizan después del padre (pintor: lo último dibujado
     * aparece encima).
     */
    fun renderTree() {
        if (!visible) return
        render()
        for (child in children) {
            child.renderTree()
        }
    }

    /**
     * Añade un hijo al árbol de renderizado.
     *
     * @param node Nodo hijo a añadir.
     */
    fun addChild(node: RenderNode) {
        children.add(node)
    }

    /**
     * Elimina un hijo del árbol de renderizado.
     *
     * @param node Nodo hijo a eliminar.
     * @return true si el nodo fue encontrado y eliminado.
     */
    fun removeChild(node: RenderNode): Boolean = children.remove(node)

    /**
     * Obtiene la lista actual de hijos.
     */
    fun getChildren(): List<RenderNode> = children.toList()
}
