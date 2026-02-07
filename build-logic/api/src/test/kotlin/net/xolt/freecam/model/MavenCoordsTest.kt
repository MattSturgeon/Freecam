package net.xolt.freecam.model

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class MavenCoordsTest {

    @Test
    fun `coordinate omits default extension and null classifier`() {
        val maven = MavenCoords(group = "g", name = "a", version = "1.0")
        maven.coordinate shouldBe "g:a:1.0"
    }

    @Test
    fun `coordinate includes classifier`() {
        val maven = MavenCoords(group = "g", name = "a", version = "1.0", classifier = "sources")
        maven.coordinate shouldBe "g:a:1.0:sources"
    }

    @Test
    fun `coordinate includes non-jar extension`() {
        val maven = MavenCoords(group = "g", name = "a", version = "1.0", extension = "zip")
        maven.coordinate shouldBe "g:a:1.0@zip"
    }

    @Test
    fun `coordinate includes classifier and non-jar extension`() {
        val maven = MavenCoords(group = "g", name = "a", version = "1.0", classifier = "sources", extension = "zip")
        maven.coordinate shouldBe "g:a:1.0:sources@zip"
    }
}

