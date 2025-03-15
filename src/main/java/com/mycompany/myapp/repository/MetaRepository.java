package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Meta;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Meta entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MetaRepository extends ReactiveCrudRepository<Meta, Long>, MetaRepositoryInternal {
    @Query("SELECT * FROM meta entity WHERE entity.aluno_id = :id")
    Flux<Meta> findByAluno(Long id);

    @Query("SELECT * FROM meta entity WHERE entity.aluno_id IS NULL")
    Flux<Meta> findAllWhereAlunoIsNull();

    @Override
    <S extends Meta> Mono<S> save(S entity);

    @Override
    Flux<Meta> findAll();

    @Override
    Mono<Meta> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface MetaRepositoryInternal {
    <S extends Meta> Mono<S> save(S entity);

    Flux<Meta> findAllBy(Pageable pageable);

    Flux<Meta> findAll();

    Mono<Meta> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Meta> findAllBy(Pageable pageable, Criteria criteria);
}
