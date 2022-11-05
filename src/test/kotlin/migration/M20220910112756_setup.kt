package migration

import org.ktapi.db.Database
import org.ktapi.db.KotlinMigration

class M20220910112756_setup : KotlinMigration() {
    override fun migrate() {
        Database.execute("insert into something (name, enabled) values ('FirstValue', true)")
        Database.execute("insert into something_else (name, something_id) values ('FirstElse', 1)")
    }

    override fun getChecksum() = 520560888
}