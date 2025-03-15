package com.mycompany.myapp.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class SimuladoSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("nota_mat", table, columnPrefix + "_nota_mat"));
        columns.add(Column.aliased("nota_port", table, columnPrefix + "_nota_port"));
        columns.add(Column.aliased("nota_lang", table, columnPrefix + "_nota_lang"));
        columns.add(Column.aliased("nota_hum", table, columnPrefix + "_nota_hum"));

        columns.add(Column.aliased("aluno_id", table, columnPrefix + "_aluno_id"));
        return columns;
    }
}
