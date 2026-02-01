package com.github.lajospolya.popularVote.config

import com.github.lajospolya.popularVote.entity.attributeConverter.PoliticalSpectrumReadingConverter
import com.github.lajospolya.popularVote.entity.attributeConverter.PoliticalSpectrumWritingConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.MySqlDialect

@Configuration
class R2dbcConfig {
    @Bean
    fun r2dbcCustomConversions(): R2dbcCustomConversions {
        val converters =
            listOf(
                PoliticalSpectrumWritingConverter(),
                PoliticalSpectrumReadingConverter(),
            )
        return R2dbcCustomConversions.of(MySqlDialect.INSTANCE, converters)
    }
}
