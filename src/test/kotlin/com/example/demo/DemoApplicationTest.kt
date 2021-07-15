package com.example.demo

import com.example.demo.model.Graph
import com.example.demo.model.VertexValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class DemoApplicationTest {

    @Test
    fun `Should create graph with not repeated edges`() {
        // GIVEN
        val graph = Graph("A", 5)
        graph.addVertex("A", "B")
        graph.addVertex("A", "C")
        graph.addVertex("B", "D")
        graph.addVertex("B", "E")
        graph.addVertex("B", "E")
        graph.addVertex("B", "E")
        // WHEN
        val verticesNames = graph.getAllVertices().map { it.name }
        // THEN
        assertThat(verticesNames.size).isEqualTo(5)
        assertThat(verticesNames).containsAll(listOf("A", "B", "C", "D", "E"))
    }

    @Test
    fun `shouldn't create vertex if parent doesn't exist`() {
        // GIVEN
        val graph = Graph("A", 5)
        graph.addVertex("A", "B")
        graph.addVertex("C", "D")
        // WHEN
        val verticesNames = graph.getAllVertices().map { it.name }
        // THEN
        assertThat(verticesNames.size).isEqualTo(2)
        assertThat(verticesNames).containsAll(listOf("A", "B"))
    }

    @Test
    fun `should successfully remove vertex`() {
        // GIVEN
        val graph = Graph("A", 5)
        graph.addVertex("A", "B")
        graph.addVertex("B", "C")
        graph.removeVertex("C")
        // WHEN
        val verticesNames = graph.getAllVertices().map { it.name }
        // THEN
        assertThat(verticesNames.size).isEqualTo(2)
        assertThat(verticesNames).containsAll(listOf("A", "B"))
    }

    @Test
    fun `should check is vertex exist`() {
        // GIVEN
        val graph = Graph("A", 5)
        graph.addVertex("A", "B")
        graph.addVertex("A", "C")
        // THEN
        assertThat(graph.isVertexExist("C")).isTrue
        assertThat(graph.isVertexExist("D")).isFalse
    }

    @Test
    fun `should propagate vertices value change`() {
        // GIVEN
        val nextValue = VertexValue(5, LocalDateTime.now().plusSeconds(1))
        val graph = Graph("A", 2)
        graph.addVertex("A", "B")
        graph.addVertex("A", "C")
        graph.addVertex("C", "D")
        // WHEN
        graph.getVertex("C")?.setValue(nextValue)
        val values = graph.getAllVertices().map { it.getValue()?.value }
        // THEN
        values.forEach { assertThat(it).isEqualTo(nextValue.value) }
    }

    @Test
    fun `should propagate newest value in case of concurrent change`() {
        // GIVEN
        val earlierUpdate = VertexValue(5, LocalDateTime.now().plusSeconds(1))
        val laterUpdate = VertexValue(8, LocalDateTime.now().plusSeconds(2))
        val graph = Graph("A", 2)
        graph.addVertex("A", "B")
        graph.addVertex("A", "C")
        graph.addVertex("C", "D")
        // WHEN
        graph.getVertex("C")?.setValue(laterUpdate)
        graph.getVertex("D")?.setValue(earlierUpdate)
        val values = graph.getAllVertices().map { it.getValue()?.value }
        // THEN
        values.forEach { assertThat(it).isEqualTo(laterUpdate.value) }
    }

    @Test
    fun `should find all vertices routes`() {
        // GIVEN
        val graph = Graph("A", 2)
        graph.addVertex("A", "B")
        graph.addVertex("A", "C")
        graph.addVertex("B", "E")
        // graph.addVertex("C", "E") <--- concurrently created "E" can has parent "B" or "C" and can't be added like that
        graph.addVertex("C", "D")
        graph.addVertex("E", "G")
        graph.addVertex("E", "F")
        graph.addEdge("C", "E")
        // WHEN
        val routes1 = graph.findPatch("A", "F")
        val routes2 = graph.findPatch("A", "X")
        // THEN
        assertThat(routes1.size).isEqualTo(2)
        assertThat(routes2).isEmpty()
        assertThat(routes1[0]).isEqualTo("A --> B --> E --> F")
        assertThat(routes1[1]).isEqualTo("A --> C --> E --> F")
    }

    @Test
    fun `should successfully switch edge`() {
        // GIVEN
        val graph = Graph("A", 5)
        graph.addVertex("A", "B")
        graph.addVertex("A", "C")
        graph.addVertex("B", "D")
        graph.addEdge("C", "D")
        graph.removeEdge("B", "D")
        // WHEN
        val routes = graph.findPatch("A", "D")
        // THEN
        assertThat(routes.size).isEqualTo(1)
        assertThat(routes[0]).isEqualTo("A --> C --> D")
        assertThat(graph.hasEdge("B", "D")).isFalse
        assertThat(graph.hasEdge("D", "B")).isFalse
        assertThat(graph.hasEdge("C", "D")).isTrue
        assertThat(graph.hasEdge("D", "C")).isTrue
    }

    @Test
    fun `should replicate graph`() {
        // GIVEN
        val graphA = Graph("A", 5)
        graphA.addVertex("A", "B")
        graphA.addVertex("A", "C")
        graphA.addVertex("B", "D")
        graphA.addVertex("B", "E")
        val graphB = graphA.replicate()
        graphB.getVertex("C")?.setValue(VertexValue(7, LocalDateTime.now().plusNanos(10)))
        // WHEN
        val graphANames = graphA.getAllVertices().map { it.name }
        val graphAValues = graphA.getAllVertices().map { it.getValue()?.value }
        val graphBNames = graphB.getAllVertices().map { it.name }
        val graphBValues = graphB.getAllVertices().map { it.getValue()?.value }
        // THEN
        assertThat(graphANames).containsAll(listOf("A", "B", "C", "D", "E"))
        assertThat(graphBNames).containsAll(listOf("A", "B", "C", "D", "E"))
        graphAValues.forEach { assertThat(it).isEqualTo(5) }
        graphBValues.forEach { assertThat(it).isEqualTo(7) }
    }

}
