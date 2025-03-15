package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Simulado;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Simulado entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SimuladoRepository extends ReactiveCrudRepository<Simulado, Long>, SimuladoRepositoryInternal {
    @Query("SELECT * FROM simulado entity WHERE entity.aluno_id = :id")
    Flux<Simulado> findByAluno(Long id);

    @Query("SELECT * FROM simulado entity WHERE entity.aluno_id IS NULL")
    Flux<Simulado> findAllWhereAlunoIsNull();

    @Override
    <S extends Simulado> Mono<S> save(S entity);

    @Override
    Flux<Simulado> findAll();

    @Override
    Mono<Simulado> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface SimuladoRepositoryInternal {
    <S extends Simulado> Mono<S> save(S entity);

    Flux<Simulado> findAllBy(Pageable pageable);

    Flux<Simulado> findAll();

    Mono<Simulado> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Simulado> findAllBy(Pageable pageable, Criteria criteria);
}
