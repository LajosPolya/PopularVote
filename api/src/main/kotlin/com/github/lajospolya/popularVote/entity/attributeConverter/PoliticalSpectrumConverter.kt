package com.github.lajospolya.popularVote.entity.attributeConverter

import com.github.lajospolya.popularVote.entity.PoliticalSpectrum
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

@WritingConverter
class PoliticalSpectrumWritingConverter : Converter<PoliticalSpectrum, String> {
    override fun convert(source: PoliticalSpectrum): String = source.name.lowercase()
}

@ReadingConverter
class PoliticalSpectrumReadingConverter : Converter<String, PoliticalSpectrum> {
    override fun convert(source: String): PoliticalSpectrum = PoliticalSpectrum.valueOf(source.uppercase())
}
