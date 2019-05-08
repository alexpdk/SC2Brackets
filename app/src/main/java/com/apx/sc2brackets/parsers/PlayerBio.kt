package com.apx.sc2brackets.parsers

import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.lang.Exception

/**Represents parsed html page from liquipedia with player bio fullIntro*/
class PlayerBio private constructor(override val document: Document) : ParsedDocument {

    val birth get() = info["Birth:"]

    val briefIntro get() = fullIntro[0]

    val earnings get() = info["Total Earnings:"]

    fun freeMemory() {
        document.empty()
    }

    /**List of paragraphs containing player introduction on the page*/
    val fullIntro: List<String> by lazy {
        val infoboxIndex = infobox.elementSiblingIndex()
        val introParagraphs = Elements(infobox.siblingElements()
            //get all paragraphs after infobox and before table of contents - fullIntro
            .drop(infoboxIndex)
            .takeWhile {
                it.tagName() == "p"
            }
            // if element descender is italic tag, skip this paragraph, it contains some service info
            .filterNot {
                it.children().isNotEmpty() && it.child(0).tagName() == "i"
            })
        // remove references in superscript
        introParagraphs.select("sup").remove()
        // obtain text content, drop possible empty paragraphs
        introParagraphs.map { it.text() }.filter { it.isNotEmpty() }
    }

    private val infobox by lazy {
        document.select(contentBlock.infoboxSelector).ifEmpty {
            throw ElementNotFoundException(contentBlock.infoboxSelector)
        }[0]
    }

    private val info by lazy {
        val map = HashMap<String, String>()
        infobox.child(0).children().forEach {
            if (it.children().size >= 2) {
                if (it.child(0).className() == infoboxBlock.attributeNameClass) {
                    //remove invisible blocks that can appear
                    val del = it.select("[style=display:none]")
                    del.remove()
                    map.set(
                        it.child(0).text().trim(),
                        it.child(1).text().trim()
                    )
                }
            }
        }
        map
    }

    val name
        get(): String {
            val header = document.select(nameHeaderSelector).ifEmpty {
                throw ElementNotFoundException(nameHeaderSelector)
            }
            return header.text()!!
        }

    val photoURI
        get(): String {
            val image = document.select(infoboxBlock.imageSelector).ifEmpty {
                throw ElementNotFoundException(infoboxBlock.imageSelector)
            }[0]
            return if (image.attr("srcset").isNotEmpty()) {
                //srcset is space-separated string, which looks like "<Url1> 1.5x <Url2> 2x ..."
                val lastUrl = image.attr("srcset").split(" ").filterNot { it.last() == 'x' }.last()
                // transform to absolute url
                val absolutePrefix = image.absUrl("src").let {
                    it.removeSuffix(it.commonSuffixWith(image.attr("src")))
                }
                absolutePrefix + lastUrl
            } else {
                image.absUrl("src")
            }
        }

    val premierTournaments by lazy {
        document.select(contentBlock.premierTournamentsHeader).parents().parents().parents().parents()
            .select("tbody tr")
    }

    /**Return name transcription of korean players or just name*/
    val realName
        get() = info["Romanized Name:"] ?: info["Name:"]

    val team get() = info["Team:"]

    val wcsCircuit get() = info["WCS Circuit rank:"]

    val wcsKorea get() = info["WCS Korea rank:"]

    companion object : Parser<PlayerBio>() {
        override fun getInstance(doc: Document) = PlayerBio(doc)

        const val nameHeaderSelector = "h1#firstHeading"
        /**Class that contains all th player data (except the name)*/
        private const val contentBlockSelector = "div.mw-content-text"

        /**Block defined by contentBlockSelector*/
        private val contentBlock = object {
            /**Class of the side panel with photo and briefIntro data*/
            val infoboxSelector = "div.fo-nttax-infobox-wrapper.infobox-wol"

            val premierTournamentsHeader = "table thead a[title=Premier Tournaments]"
            /**ID of content table between shot player fullIntro and rest of his bio */
//            val tableOfContentsId = "toc"
        }
        private val infoboxBlock = object {
            val attributeNameClass = "infobox-cell-2 infobox-description"
            val imageSelector = "div.infobox-image a.image img"
        }
    }
}