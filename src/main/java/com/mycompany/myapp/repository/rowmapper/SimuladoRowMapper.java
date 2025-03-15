package com.mycompany.myapp.repository.rowmapper;

import com.mycompany.myapp.domain.Simulado;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Simulado}, with proper type conversions.
 */
@Service
public class SimuladoRowMapper implements BiFunction<Row, String, Simulado> {

    private final ColumnConverter converter;

    public SimuladoRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Simulado} stored in the database.
     */
    @Override
    public Simulado apply(Row row, String prefix) {
        Simulado entity = new Simulado();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setNotaMat(converter.fromRow(row, prefix + "_nota_mat", Integer.class));
        entity.setNotaPort(converter.fromRow(row, prefix + "_nota_port", Integer.class));
        entity.setNotaLang(converter.fromRow(row, prefix + "_nota_lang", Integer.class));
        entity.setNotaHum(converter.fromRow(row, prefix + "_nota_hum", Integer.class));
        entity.setAlunoId(converter.fromRow(row, prefix + "_aluno_id", Long.class));
        return entity;
    }
}
