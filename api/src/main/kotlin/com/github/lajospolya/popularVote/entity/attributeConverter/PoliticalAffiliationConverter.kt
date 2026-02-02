package com.github.lajospolya.popularVote.entity.attributeConverter

import com.github.lajospolya.popularVote.entity.PoliticalAffiliation
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

@WritingConverter
class PoliticalAffiliationWritingConverter : Converter<PoliticalAffiliation, String> {
    override fun convert(source: PoliticalAffiliation): String = source.name.lowercase()
}

@ReadingConverter
class PoliticalAffiliationReadingConverter : Converter<String, PoliticalAffiliation> {
    override fun convert(source: String): PoliticalAffiliation = PoliticalAffiliation.valueOf(source.uppercase())
}
