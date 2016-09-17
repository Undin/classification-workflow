package com.warrior.classification.workflow.baseline.json;

import org.hibernate.dialect.PostgreSQL95Dialect;

import java.sql.Types;

/**
 * Created by warrior on 17/09/16.
 */
public class JsonbPostgreSQLDialect extends PostgreSQL95Dialect {

    public JsonbPostgreSQLDialect() {
        super();
        this.registerColumnType(Types.JAVA_OBJECT, "jsonb");
    }
}
