package ru.skillbranch.skillarticles.data.repositories

import java.util.regex.Pattern

object MarkdownParser {

    private val LINE_SEPARATOR = System.getProperty("line.separator") ?: "\n"

    //group regex
    private const val UNORDERED_LIST_ITEM_GROUP = "(^[*+-] .+$)"
    private const val HEADER_GROUP = "(^#{1,6} .+?$)"
    private const val QUOTE_GROUP = "(^> .+?$)"
    private const val ITALIC_GROUP = "((?<!\\*)\\*[^*].*?[^*]\\*(?!\\*)|(?<!_)_[^_].*?[^_]_(?!_))"
    private const val BOLD_GROUP =
        "((?<!\\*)\\*{2}[^*].*?[^*]\\*{2}(?!\\*)|(?<!_)_{2}[^_].*?[^_]_{2}(?!_))"
    private const val STRIKE_GROUP = "((?<!~)~{2}[^~].*?[^~]~{2}(?!~))"
    private const val RULE_GROUP = "(^[-_*]{3}$)"
    private const val INLINE_CODE_GROUP = "((?<!`)`[^`\\s].*?[^`\\s]?`(?!`))"
    private const val LINK_GROUP = "(\\[[^\\[\\]]*?]\\(.+?\\)|^\\[*?]\\(.*?\\))"
    private const val ORDERED_LIST_ITEM_GROUP = "(^\\d\\. .+$)"
    private const val BLOCK_CODE_GROUP = "(^`{3}[\\s\\S]+?`{3}$)"

    //result regex
    private const val MARKDOWN_GROUPS = "$UNORDERED_LIST_ITEM_GROUP|$HEADER_GROUP|$QUOTE_GROUP" +
            "|$ITALIC_GROUP|$BOLD_GROUP|$STRIKE_GROUP|$RULE_GROUP|$INLINE_CODE_GROUP|$LINK_GROUP" +
            "|$ORDERED_LIST_ITEM_GROUP|$BLOCK_CODE_GROUP"

    private val elementsPattern by lazy { Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE) }

    fun parse(string: String): MarkdownText {
        val elements = mutableListOf<Element>()
        elements.addAll(
            findElement(
                string
            )
        )
        return MarkdownText(
            elements
        )
    }

    fun clear(string: String?): String? {
        string ?: return null
        val stringBuilder = StringBuilder()
        parse(
            string
        ).elements
            .map {
                if (it.elements.isEmpty())
                    stringBuilder.append(it.text)
                else
                    stringBuilder.append(
                        clear(
                            it.text.toString()
                        )
                    )
            }
        return stringBuilder.toString()
    }

    private fun findElement(string: CharSequence): List<Element> {
        val parents = mutableListOf<Element>()
        val matcher = elementsPattern.matcher(string)
        var lastStartIndex = 0

        loop@ while (matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()

            //if something was found then everything before is TEXT
            if (lastStartIndex < startIndex) {
                parents.add(
                    Element.Text(
                        string.subSequence(lastStartIndex, startIndex)
                    )
                )
            }

            //found text
            var text: CharSequence
            //groups range for iterating by groups
            val groups = 1..11
            var group = -1

            for (gr in groups) {
                if (matcher.group(gr) != null) {
                    group = gr
                    break
                }
            }

            when (group) {
                -1 -> break@loop

                //UNORDERED_LIST
                1 -> {
                    //text without "* "
                    text = string.subSequence(startIndex.plus(2), endIndex)
                    //find inner elements
                    val subelements =
                        findElement(
                            text
                        )
                    val element =
                        Element.UnorderedListItem(
                            text,
                            subelements
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //HEADERS
                2 -> {
                    val regex = "#{1,6}".toRegex().find(string.subSequence(startIndex, endIndex))
                    val level = regex!!.value.length

                    //text without "# - ###### "
                    text = string.subSequence(startIndex.plus(level.inc()), endIndex)

                    val element =
                        Element.Header(
                            level,
                            text
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //QUOTE
                3 -> {
                    //text without "> "
                    text = string.subSequence(startIndex.plus(2), endIndex)
                    val subelements =
                        findElement(
                            text
                        )
                    val element =
                        Element.Quote(
                            text,
                            subelements
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //ITALIC
                4 -> {
                    //text without "_ or * {} _ or *"
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val subelements =
                        findElement(
                            text
                        )
                    val element =
                        Element.Italic(
                            text,
                            subelements
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //BOLD
                5 -> {
                    //text without "__ or ** {} __ or **"
                    text = string.subSequence(startIndex.plus(2), endIndex.minus(2))
                    val subelements =
                        findElement(
                            text
                        )
                    val element =
                        Element.Bold(
                            text,
                            subelements
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //STRIKE
                6 -> {
                    //text without "~~{}~~
                    text = string.subSequence(startIndex.plus(2), endIndex.minus(2))
                    val subelements =
                        findElement(
                            text
                        )
                    val element =
                        Element.Strike(
                            text,
                            subelements
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //RULE
                7 -> {
                    //text without *** or ___ or ---
                    val element =
                        Element.Rule()
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //INLINE CODE
                8 -> {
                    //text without " `{}` "
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val subelements =
                        findElement(
                            text
                        )
                    val element =
                        Element.InlineCode(
                            text,
                            subelements
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //LINK CODE
                9 -> {
                    //full text
                    text = string.subSequence(startIndex, endIndex)
                    val (title: String, link: String) = "\\[(.*)]\\((.*)\\)".toRegex()
                        .find(text)!!.destructured
                    val element =
                        Element.Link(
                            link,
                            title
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //ORDERED LIST
                10 -> {
                    val regex = "\\d+\\.".toRegex().find(string.subSequence(startIndex, endIndex))
                    val order = regex!!.value
                    text = string.subSequence(startIndex.plus(3), endIndex)
                    val subelements =
                        findElement(
                            text
                        )
                    val element =
                        Element.OrderedListItem(
                            order,
                            text,
                            subelements
                        )
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //BLOCK CODE
                11 -> {
                    text = string.subSequence(startIndex.plus(3), endIndex.minus(3))
                    val lines = text.lines()
                    var line: String
                    var element: Element
                    var type: Element.BlockCode.Type

                    for (index in 0..lines.lastIndex) {
                        line = lines[index] + if (index != lines.lastIndex) "\n" else ""
                        type = if (lines.size == 1) {
                            Element.BlockCode.Type.SINGLE
                        } else {
                            when (index) {
                                0 -> Element.BlockCode.Type.START
                                lines.lastIndex -> Element.BlockCode.Type.END
                                else -> Element.BlockCode.Type.MIDDLE
                            }
                        }
                        element =
                            Element.BlockCode(
                                type,
                                line
                            )
                        parents.add(element)
                    }

                    lastStartIndex = endIndex
                }

            }
        }

        if (lastStartIndex < string.length) {
            val text = string.subSequence(lastStartIndex, string.length)
            parents.add(
                Element.Text(
                    text
                )
            )
        }

        return parents
    }

}

data class MarkdownText(val elements: List<Element>)

sealed class Element {
    abstract val text: CharSequence
    abstract val elements: List<Element>

    data class Text(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class UnorderedListItem(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Header(
        val level: Int = 1,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Quote(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Italic(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Bold(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Strike(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Rule(
        override val text: CharSequence = " ",
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class InlineCode(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Link(
        val link: String,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class OrderedListItem(
        val order: String,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class BlockCode(
        val type: Type = Type.MIDDLE,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element() {
        enum class Type { START, END, MIDDLE, SINGLE }
    }

}