package com.example.demo.model

import com.example.demo.utils.getAllActiveVertices
import com.example.demo.utils.getVertexState
import com.example.demo.utils.isExist
import java.time.LocalDateTime


class Vertex(val name: String, val timestamp: LocalDateTime) {
    private val values = mutableSetOf<VertexValue>()
    private val edges = mutableSetOf<Vertex>()
    private val edgesDel = mutableSetOf<Vertex>()

    fun setValue(value: VertexValue) {
        val allAreOlder = getValue()?.timestamp?.isBefore(value.timestamp)
        if (allAreOlder == null || allAreOlder) {
            values.add(value)
            setEdgeRelatedValues(value)
        }
    }

    fun hasEdge(name: String): Boolean =
        getVertexState(name, edges, edgesDel).isExist()

    fun getValue(): VertexValue? =
        values.maxByOrNull { it.timestamp }

    fun addEdge(vertex: Vertex) =
        edges.add(vertex)

    fun removeEdge(vertex: Vertex) =
        edgesDel.add(vertex)

    fun getAllEdges(): List<Vertex> =
        getAllActiveVertices(edges, edgesDel)

    fun replicate(): Vertex {
        val newVertex = Vertex(name, LocalDateTime.now())
        getValue()?.let { newVertex.setValue(VertexValue(it.value, it.timestamp)) }
        return newVertex
    }

    fun getRoutes(destinationName: String, currentRoute: List<String>): List<List<String>> {
        val nextRoute = currentRoute.toMutableList().plus(name)
        if(name == destinationName) {
            return listOf(nextRoute)
        }
        return getAllActiveVertices(edges, edgesDel)
            .filter { !nextRoute.contains(it.name) }
            .map { it.getRoutes(destinationName, nextRoute) }
            .fold(mutableListOf()) { base, element ->
                base.addAll(element)
                base
            }
    }

    private fun setEdgeRelatedValues(value: VertexValue) =
        getAllEdges().forEach { it.setValue(value) }

}
