package com.github.lajospolya.PopularVote.entity.attributeConverter

import com.github.lajospolya.PopularVote.entity.PoliticalSpectrum
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.PropertyValueConverter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.ValueConverter
import org.springframework.data.convert.WritingConverter


@ReadingConverter
class PoliticalSpectrumConverter: Converter<PoliticalSpectrum, String> {
    override fun convert(source: PoliticalSpectrum): String {
        return source.name
    }
}

@WritingConverter
class PoliticalSpectrumWritingConverter: Converter<String, PoliticalSpectrum> {
    override fun convert(source: String): PoliticalSpectrum {
        return PoliticalSpectrum.valueOf(source)
    }
}