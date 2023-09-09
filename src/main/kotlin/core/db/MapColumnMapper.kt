package core.db

import com.fasterxml.jackson.module.kotlin.readValue
import core.config.JacksonModule
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

class MapColumnMapper : ColumnMapper<Map<String, String>> {
    @Throws(SQLException::class)
    override fun map(r: ResultSet, columnNumber: Int, ctx: StatementContext?): Map<String, String> {
        return JacksonModule.mapper.readValue(r.getString(columnNumber))
    }
}
