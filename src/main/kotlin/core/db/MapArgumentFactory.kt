package core.db

import core.config.JacksonModule
import org.jdbi.v3.core.argument.AbstractArgumentFactory
import org.jdbi.v3.core.argument.Argument
import org.jdbi.v3.core.config.ConfigRegistry
import org.jdbi.v3.core.statement.StatementContext
import java.sql.PreparedStatement
import java.sql.Types

class MapArgumentFactory : AbstractArgumentFactory<Map<String, String>>(Types.VARCHAR) {
    override fun build(value: Map<String, String>, config: ConfigRegistry): Argument {
        return Argument { position: Int, statement: PreparedStatement, _: StatementContext? ->
            statement.setString(position, JacksonModule.mapper.writeValueAsString(value))
        }
    }
}
