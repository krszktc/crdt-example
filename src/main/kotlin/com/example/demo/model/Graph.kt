package com.example.demo.model

import com.example.demo.utils.getAllActiveVertices
import com.example.demo.utils.getVertexState
import com.example.demo.utils.isExist
import java.time.LocalDateTime

class Graph(private var vertices: MutableSet<Vertex>) {
    private val verticesDel = mutableSetOf<Vertex>()

    constructor(initPointName: String, initPointValue: Int): this(mutableSetOf()) {
        val timestamp = LocalDateTime.now()
        val initValue = VertexValue(initPointValue, timestamp)
        val initVertex = Vertex(initPointName, timestamp).apply { setValue(initValue) }
        vertices.add(initVertex)
    }

    fun addEdge(source: String, destination: String) {
        val sourceVertex = getVertex(source)
        val destinationVertex =  getVertex(destination)
        if (sourceVertex != null && destinationVertex != null) {
            sourceVertex.addEdge(destinationVertex)
            destinationVertex.addEdge(sourceVertex)
        }
    }

    fun removeEdge(source: String, destination: String) {
        val sourceVertex = getVertex(source)
        val destinationVertex =  getVertex(destination)
        if (sourceVertex != null && destinationVertex != null) {
            sourceVertex.removeEdge(destinationVertex)
            destinationVertex.removeEdge(sourceVertex)
        }
    }

    fun hasEdge(source: String, destination: String): Boolean {
        val sourceVertex = getVertex(source)
        val destinationVertex =  getVertex(destination)
        return sourceVertex != null
                && destinationVertex != null
                && sourceVertex.hasEdge(destination)
                && destinationVertex.hasEdge(source)
    }

    fun isVertexExist(name: String): Boolean =
        getVertexState(name, vertices, verticesDel).isExist()

    fun addVertex(parentName: String, name: String) {
        val parentState = getVertexState(parentName, vertices, verticesDel)
        if (parentState.isExist()) {
            val newVertex = Vertex(name, LocalDateTime.now())
            parentState.first?.let {
                it.addEdge(newVertex)
                newVertex.addEdge(it)
                newVertex.setValue(it.getValue()!!)
            }
            vertices.add(newVertex)
        }
    }

    fun removeVertex(name: String) {
        val vertexState = getVertexState(name, vertices, verticesDel)
        if (vertexState.isExist()) {
            val newVertex = Vertex(name, LocalDateTime.now())
            verticesDel.add(newVertex)
            vertexState.first?.getAllEdges()?.forEach { it.removeEdge(newVertex) }
        }
    }

    fun getVertex(name: String): Vertex? =
        getAllActiveVertices(vertices, verticesDel).find { it.name == name }

    fun getAllVertices(): List<Vertex> =
        getAllActiveVertices(vertices, verticesDel)

    fun findPatch(source: String, destination: String): List<String> {
        val sourceState = getVertexState(source, vertices, verticesDel)
        val destinationState = getVertexState(destination, vertices, verticesDel)
        if (sourceState.isExist() && destinationState.isExist()) {
            return route(destination, sourceState.first!!)
        }
        return emptyList()
    }

    fun replicate(): Graph {
        val currentVertices = getAllVertices()
        val newVertices = currentVertices.map { it.replicate() }.toMutableSet()
        currentVertices.forEach { vertex ->
            val newVertex = newVertices.find { it.name == vertex.name }
            vertex.getAllEdges()
                .forEach { edge ->
                    val reference = newVertices.find { it.name == edge.name }
                    newVertex?.addEdge(reference!!)
                }
        }
        return Graph(newVertices)
    }

    private fun route(destination: String, root: Vertex): List<String> {
        val allRoutes: List<List<String>> = root.getAllEdges()
            .map { it.getRoutes(destination, listOf(root.name)) }
            .fold(mutableListOf()) { base, element ->
                base.addAll(element)
                base
            }
        return allRoutes.map { it.joinToString(separator = " --> ") }
    }

}