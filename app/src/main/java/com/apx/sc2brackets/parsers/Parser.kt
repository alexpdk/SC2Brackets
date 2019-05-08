package com.apx.sc2brackets.parsers

import org.jetbrains.annotations.TestOnly
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File

interface ParsedDocument{
    val document: Document
}

abstract class Parser<T: ParsedDocument> {
    protected abstract fun getInstance(doc: Document): T

    /**Used in unit tests to create malformed documents and check exception raising */
    @TestOnly
    fun modifiedCopy(src: T, modification: Document.() -> Unit): T {
        val copy = getInstance(src.document.clone())
        modification(copy.document)
        return copy
    }

    fun parseFile(input: File, baseUri: String): T {
        return getInstance(Jsoup.parse(input, "UTF-8", baseUri))
    }

    fun parseHTMLContent(html: String, baseUri: String): T {
        //baseUri allows to properly modify all Uri in the document
        //bug - baseUri not appended when parsing String, although appended when parsing file
        return getInstance(Jsoup.parse(html, baseUri))
    }
}