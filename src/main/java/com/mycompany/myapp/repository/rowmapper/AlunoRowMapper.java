package com.mycompany.myapp.repository.rowmapper;

import com.mycompany.myapp.domain.Aluno;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Aluno}, with proper type conversions.
 */
@Service
public class AlunoRowMapper implements BiFunction<Row, String, Aluno> {

    private final ColumnConverter converter;

    public AlunoRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Aluno} stored in the database.
     */
    @Override
    public Aluno apply(Row row, String prefix) {
        Aluno entity = new Aluno();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setNome(converter.fromRow(row, prefix + "_nome", String.class));
        return entity;
    }
}
