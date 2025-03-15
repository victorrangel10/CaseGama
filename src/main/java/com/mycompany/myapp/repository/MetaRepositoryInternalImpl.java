package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Meta;
import com.mycompany.myapp.repository.rowmapper.AlunoRowMapper;
import com.mycompany.myapp.repository.rowmapper.MetaRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC custom repository implementation for the Meta entity.
 */
@SuppressWarnings("unused")
class MetaRepositoryInternalImpl extends SimpleR2dbcRepository<Meta, Long> implements MetaRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final AlunoRowMapper alunoMapper;
    private final MetaRowMapper metaMapper;

    private static final Table entityTable = Table.aliased("meta", EntityManager.ENTITY_ALIAS);
    private static final Table alunoTable = Table.aliased("aluno", "aluno");

    public MetaRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        AlunoRowMapper alunoMapper,
        MetaRowMapper metaMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Meta.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.alunoMapper = alunoMapper;
        this.metaMapper = metaMapper;
    }

    @Override
    public Flux<Meta> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Meta> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = MetaSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(AlunoSqlHelper.getColumns(alunoTable, "aluno"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(alunoTable)
            .on(Column.create("aluno_id", entityTable))
            .equals(Column.create("id", alunoTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Meta.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Meta> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Meta> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    private Meta process(Row row, RowMetadata metadata) {
        Meta entity = metaMapper.apply(row, "e");
        entity.setAluno(alunoMapper.apply(row, "aluno"));
        return entity;
    }

    @Override
    public <S extends Meta> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
