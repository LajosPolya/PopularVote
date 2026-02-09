package com.github.lajospolya.popularVote.entity.attributeConverter

import com.github.lajospolya.popularVote.entity.Role
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

@WritingConverter
class RoleWritingConverter : Converter<Role, String> {
    override fun convert(source: Role): String = source.name.lowercase()
}

@ReadingConverter
class RoleReadingConverter : Converter<String, Role> {
    override fun convert(source: String): Role = Role.valueOf(source.uppercase())
}
