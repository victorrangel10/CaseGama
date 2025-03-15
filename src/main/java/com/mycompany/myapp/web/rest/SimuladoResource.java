package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.Simulado;
import com.mycompany.myapp.repository.SimuladoRepository;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.Simulado}.
 */
@RestController
@RequestMapping("/api/simulados")
@Transactional
public class SimuladoResource {

    private static final Logger LOG = LoggerFactory.getLogger(SimuladoResource.class);

    private static final String ENTITY_NAME = "simulado";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SimuladoRepository simuladoRepository;

    public SimuladoResource(SimuladoRepository simuladoRepository) {
        this.simuladoRepository = simuladoRepository;
    }

    /**
     * {@code POST  /simulados} : Create a new simulado.
     *
     * @param simulado the simulado to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new simulado, or with status {@code 400 (Bad Request)} if the simulado has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<Simulado>> createSimulado(@RequestBody Simulado simulado) throws URISyntaxException {
        LOG.debug("REST request to save Simulado : {}", simulado);
        if (simulado.getId() != null) {
            throw new BadRequestAlertException("A new simulado cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return simuladoRepository
            .save(simulado)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/simulados/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /simulados/:id} : Updates an existing simulado.
     *
     * @param id the id of the simulado to save.
     * @param simulado the simulado to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated simulado,
     * or with status {@code 400 (Bad Request)} if the simulado is not valid,
     * or with status {@code 500 (Internal Server Error)} if the simulado couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Simulado>> updateSimulado(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Simulado simulado
    ) throws URISyntaxException {
        LOG.debug("REST request to update Simulado : {}, {}", id, simulado);
        if (simulado.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, simulado.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return simuladoRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return simuladoRepository
                    .save(simulado)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /simulados/:id} : Partial updates given fields of an existing simulado, field will ignore if it is null
     *
     * @param id the id of the simulado to save.
     * @param simulado the simulado to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated simulado,
     * or with status {@code 400 (Bad Request)} if the simulado is not valid,
     * or with status {@code 404 (Not Found)} if the simulado is not found,
     * or with status {@code 500 (Internal Server Error)} if the simulado couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<Simulado>> partialUpdateSimulado(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Simulado simulado
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Simulado partially : {}, {}", id, simulado);
        if (simulado.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, simulado.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return simuladoRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<Simulado> result = simuladoRepository
                    .findById(simulado.getId())
                    .map(existingSimulado -> {
                        if (simulado.getNotaMat() != null) {
                            existingSimulado.setNotaMat(simulado.getNotaMat());
                        }
                        if (simulado.getNotaPort() != null) {
                            existingSimulado.setNotaPort(simulado.getNotaPort());
                        }
                        if (simulado.getNotaLang() != null) {
                            existingSimulado.setNotaLang(simulado.getNotaLang());
                        }
                        if (simulado.getNotaHum() != null) {
                            existingSimulado.setNotaHum(simulado.getNotaHum());
                        }

                        return existingSimulado;
                    })
                    .flatMap(simuladoRepository::save);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /simulados} : get all the simulados.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of simulados in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<Simulado>> getAllSimulados() {
        LOG.debug("REST request to get all Simulados");
        return simuladoRepository.findAll().collectList();
    }

    /**
     * {@code GET  /simulados} : get all the simulados as a stream.
     * @return the {@link Flux} of simulados.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Simulado> getAllSimuladosAsStream() {
        LOG.debug("REST request to get all Simulados as a stream");
        return simuladoRepository.findAll();
    }

    /**
     * {@code GET  /simulados/:id} : get the "id" simulado.
     *
     * @param id the id of the simulado to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the simulado, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Simulado>> getSimulado(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Simulado : {}", id);
        Mono<Simulado> simulado = simuladoRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(simulado);
    }

    /**
     * {@code DELETE  /simulados/:id} : delete the "id" simulado.
     *
     * @param id the id of the simulado to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteSimulado(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Simulado : {}", id);
        return simuladoRepository
            .deleteById(id)
            .then(
                Mono.just(
                    ResponseEntity.noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }
}
