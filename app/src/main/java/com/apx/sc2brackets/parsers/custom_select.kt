package com.apx.sc2brackets.parsers

import org.jsoup.nodes.Element
import java.lang.Exception

class ElementNotFoundException(selector: String) : Exception("Element \"$selector\" expected, but not found")

@Throws(ElementNotFoundException::class)
fun Element.selectNotEmpty(selector: String) = select(selector).ifEmpty {
    throw ElementNotFoundException(selector)
}!!