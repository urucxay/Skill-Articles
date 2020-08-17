package ru.skillbranch.skillarticles.data.local

import androidx.room.TypeConverter
import ru.skillbranch.skillarticles.data.repositories.MarkdownParser
import java.util.*

class DateConverter {
    @TypeConverter
    fun timestampToDate(long: Long) = Date(long)

    @TypeConverter
    fun dateToTimestamp(date: Date) = date.time
}

class MarkdownConverter {

    @TypeConverter
    fun toMarkdown(content: String?) = content?.let { MarkdownParser.parse(it) }
}