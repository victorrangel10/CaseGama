package com.mycompany.myapp.repository.rowmapper;

import com.mycompany.myapp.domain.Meta;
import com.mycompany.myapp.domain.enumeration.AreaDoEnem;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Meta}, with proper type conversions.
 */
@Service
public class MetaRowMapper implements BiFunction<Row, String, Meta> {

    private final ColumnConverter converter;

    public MetaRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Meta} stored in the database.
     */
    @Override
    public Meta apply(Row row, String prefix) {
        Meta entity = new Meta();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setValor(converter.fromRow(row, prefix + "_valor", Integer.class));
        entity.setArea(converter.fromRow(row, prefix + "_area", AreaDoEnem.class));
        entity.setAlunoId(converter.fromRow(row, prefix + "_aluno_id", Long.class));
        return entity;
    }
}
