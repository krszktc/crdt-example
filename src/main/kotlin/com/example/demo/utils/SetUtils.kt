package com.example.demo.utils

import com.example.demo.model.Vertex

fun getVertexState(name: String, valSet: Set<Vertex>, tombstone: Set<Vertex>): Pair<Vertex?, Vertex?> {
    val existing = valSet.filter { it.name == name }.maxByOrNull { it.timestamp }
    val removed = tombstone.filter { it.name == name }.maxByOrNull { it.timestamp }
    return Pair(existing, removed)
}

fun getAllActiveVertices(valSet: Set<Vertex>, tombstone: Set<Vertex>): List<Vertex> =
    valSet.asSequence()
        .map { it.name }.toSet()
        .map { getVertexState(it, valSet, tombstone) }
        .filter { it.isExist() }
        .mapNotNull { it.first }
        .toList()

fun Pair<Vertex?, Vertex?>.isExist(): Boolean =
    first != null && (second == null || second!!.timestamp.isBefore(first!!.timestamp))