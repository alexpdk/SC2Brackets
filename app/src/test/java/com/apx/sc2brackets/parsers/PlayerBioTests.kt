package com.apx.sc2brackets.parsers

import java.io.File

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS) //create object of this class and therefore parse html pages only once
class PlayerBioTests {

    data class PlayerTest(val name: String, val pageName: String, val baseURI: String = defaultBaseURI(name)) {
        lateinit var bio: PlayerBio

        var photoURI: String? = null

        fun freeMemory() {
            bio.freeMemory()
        }

        fun parsePage() {
            bio = PlayerBio.parseFile(
                File(this.javaClass.getResource(pageName)?.toURI()),
                baseURI
            )
        }

        companion object {
            fun defaultBaseURI(name: String) = "https://liquipedia.net/starcraft2/$name"
        }
    }

    private val players = object {
        val Innovation = PlayerTest(
            name = "INnoVation", pageName = "/INnoVation.html"
        ).apply {
            photoURI = "https://liquipedia.net/commons/images/f/f9/OG_INno.png"
        }
        val Special = PlayerTest(
            name = "SpeCial", pageName = "/SpeCial.html"
        ).apply {
            photoURI = "https://liquipedia.net/commons/images/8/84/MajOr_WCS_S1_2015.jpg"
        }
        val Scarlett = PlayerTest(
            name = "Scarlett", pageName = "/Scarlett.html"
        ).apply {
            photoURI = "https://liquipedia.net/commons/images/4/43/Scarlett_Archon_Toronto.jpg"
        }
    }

    @BeforeAll
    fun `Parse documents`() {
        with(players) {
            Innovation.parsePage()
            Special.parsePage()
            Scarlett.parsePage()
        }
    }

    @AfterAll
    fun `Free memory`() {
        with(players) {
            Innovation.freeMemory()
            Special.freeMemory()
            Scarlett.freeMemory()
        }
    }

    @Test
    fun `Test player names parsing`() {
        with(players) {
            assertThat(Innovation.bio.name, equalTo(Innovation.name))
            assertThat(Special.bio.name, equalTo(Special.name))

            /*Some JVM 1.6 problems here
            //remove header with name
            val bio = PlayerBio.modifiedCopy(Innovation.bio) {
                select(PlayerBio.nameHeaderSelector).remove()
            }
            assertThrows<ElementNotFoundException> {  }*/
        }
    }
    @Test
    fun `Test player photo URI parsing`() {
        with(players) {
            //used src attribute
            assertThat(Innovation.bio.photoURI, equalTo(Innovation.photoURI))
            //parsed srcset attribute
            assertThat(Special.bio.photoURI, equalTo(Special.photoURI))
            //parsed srcset with multiple url
            assertThat(Scarlett.bio.photoURI, equalTo(Scarlett.photoURI))
        }
    }

    @Test
    fun `Test player intro parsing`() {
        with(players) {
            assertThat(Special.bio.fullIntro.size, equalTo(2))
            assertThat(Special.bio.briefIntro, equalTo(
                """
                    Juan Carlos "SpeCial" Tena Lopez, also known as MajOr,
                    is a Mexican Terran player who is currently playing for Ocean Gaming.
                """.trimIndent().replace('\n', ' ')
            ))
            assertThat(Special.bio.fullIntro[1], equalTo(
                """
                    SpeCial is known for his aggressive style of play, even during the mid and late-game,
                    used in tandem with strong mechanics and multi-tasking. He usually prefers to use as much Bio and
                    infantry as he can, taking full advantage of the mobility, using multi-pronged drops, attacks and
                    pokes to beat his opponents into submission while he macros and multi-tasks more effectively.
                    His early-game aggression is usually centered around the 2 Rax.
                """.trimIndent().replace('\n', ' ')
            ))

            assertThat(Innovation.bio.fullIntro.size, equalTo(1))
            assertThat(Innovation.bio.briefIntro, equalTo(
                """
                    Lee "INnoVation" Shin Hyung, also known as "Bogus", is a StarCraft progamer from South Korea
                    currently playing for Team Reciprocity.
                """.trimIndent().replace('\n', ' ')
            ))
        }
    }

    @Test
    fun `Test player info block parsing`(){
        with(players) {
            assertThat(Special.bio.realName, equalTo("Juan Carlos Tena Lopez"))
            assertThat(Special.bio.birth, equalTo("May 20, 1993 (age 25)"))
            assertThat(Special.bio.team, equalTo("Ocean Gaming"))
            assertThat(Special.bio.wcsKorea, equalTo("#32 (200 points)"))
            assertThat(Special.bio.wcsCircuit, equalTo("#18 (385 points)"))

            assertThat(Innovation.bio.realName, equalTo("Lee Shin-hyung"))
            assertThat(Innovation.bio.birth, equalTo("July 25, 1993 (age 25)"))
            assertThat(Innovation.bio.team, equalTo("Team Reciprocity"))
            assertThat(Innovation.bio.wcsKorea, equalTo("#19 (950 points)"))
            assertThat(Innovation.bio.wcsCircuit, nullValue())
        }
    }
}