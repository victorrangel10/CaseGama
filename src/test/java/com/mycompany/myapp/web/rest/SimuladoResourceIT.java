package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.SimuladoAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Simulado;
import com.mycompany.myapp.repository.EntityManager;
import com.mycompany.myapp.repository.SimuladoRepository;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link SimuladoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class SimuladoResourceIT {

    private static final Integer DEFAULT_NOTA_MAT = 1;
    private static final Integer UPDATED_NOTA_MAT = 2;

    private static final Integer DEFAULT_NOTA_PORT = 1;
    private static final Integer UPDATED_NOTA_PORT = 2;

    private static final Integer DEFAULT_NOTA_LANG = 1;
    private static final Integer UPDATED_NOTA_LANG = 2;

    private static final Integer DEFAULT_NOTA_HUM = 1;
    private static final Integer UPDATED_NOTA_HUM = 2;

    private static final String ENTITY_API_URL = "/api/simulados";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SimuladoRepository simuladoRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Simulado simulado;

    private Simulado insertedSimulado;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Simulado createEntity() {
        return new Simulado().notaMat(DEFAULT_NOTA_MAT).notaPort(DEFAULT_NOTA_PORT).notaLang(DEFAULT_NOTA_LANG).notaHum(DEFAULT_NOTA_HUM);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Simulado createUpdatedEntity() {
        return new Simulado().notaMat(UPDATED_NOTA_MAT).notaPort(UPDATED_NOTA_PORT).notaLang(UPDATED_NOTA_LANG).notaHum(UPDATED_NOTA_HUM);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Simulado.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    public void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    public void initTest() {
        simulado = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedSimulado != null) {
            simuladoRepository.delete(insertedSimulado).block();
            insertedSimulado = null;
        }
        deleteEntities(em);
    }

    @Test
    void createSimulado() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Simulado
        var returnedSimulado = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(simulado))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Simulado.class)
            .returnResult()
            .getResponseBody();

        // Validate the Simulado in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertSimuladoUpdatableFieldsEquals(returnedSimulado, getPersistedSimulado(returnedSimulado));

        insertedSimulado = returnedSimulado;
    }

    @Test
    void createSimuladoWithExistingId() throws Exception {
        // Create the Simulado with an existing ID
        simulado.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(simulado))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Simulado in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void getAllSimuladosAsStream() {
        // Initialize the database
        simuladoRepository.save(simulado).block();

        List<Simulado> simuladoList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Simulado.class)
            .getResponseBody()
            .filter(simulado::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(simuladoList).isNotNull();
        assertThat(simuladoList).hasSize(1);
        Simulado testSimulado = simuladoList.get(0);

        // Test fails because reactive api returns an empty object instead of null
        // assertSimuladoAllPropertiesEquals(simulado, testSimulado);
        assertSimuladoUpdatableFieldsEquals(simulado, testSimulado);
    }

    @Test
    void getAllSimulados() {
        // Initialize the database
        insertedSimulado = simuladoRepository.save(simulado).block();

        // Get all the simuladoList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(simulado.getId().intValue()))
            .jsonPath("$.[*].notaMat")
            .value(hasItem(DEFAULT_NOTA_MAT))
            .jsonPath("$.[*].notaPort")
            .value(hasItem(DEFAULT_NOTA_PORT))
            .jsonPath("$.[*].notaLang")
            .value(hasItem(DEFAULT_NOTA_LANG))
            .jsonPath("$.[*].notaHum")
            .value(hasItem(DEFAULT_NOTA_HUM));
    }

    @Test
    void getSimulado() {
        // Initialize the database
        insertedSimulado = simuladoRepository.save(simulado).block();

        // Get the simulado
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, simulado.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(simulado.getId().intValue()))
            .jsonPath("$.notaMat")
            .value(is(DEFAULT_NOTA_MAT))
            .jsonPath("$.notaPort")
            .value(is(DEFAULT_NOTA_PORT))
            .jsonPath("$.notaLang")
            .value(is(DEFAULT_NOTA_LANG))
            .jsonPath("$.notaHum")
            .value(is(DEFAULT_NOTA_HUM));
    }

    @Test
    void getNonExistingSimulado() {
        // Get the simulado
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingSimulado() throws Exception {
        // Initialize the database
        insertedSimulado = simuladoRepository.save(simulado).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the simulado
        Simulado updatedSimulado = simuladoRepository.findById(simulado.getId()).block();
        updatedSimulado.notaMat(UPDATED_NOTA_MAT).notaPort(UPDATED_NOTA_PORT).notaLang(UPDATED_NOTA_LANG).notaHum(UPDATED_NOTA_HUM);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedSimulado.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedSimulado))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Simulado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSimuladoToMatchAllProperties(updatedSimulado);
    }

    @Test
    void putNonExistingSimulado() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        simulado.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, simulado.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(simulado))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Simulado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchSimulado() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        simulado.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(simulado))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Simulado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamSimulado() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        simulado.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(simulado))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Simulado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateSimuladoWithPatch() throws Exception {
        // Initialize the database
        insertedSimulado = simuladoRepository.save(simulado).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the simulado using partial update
        Simulado partialUpdatedSimulado = new Simulado();
        partialUpdatedSimulado.setId(simulado.getId());

        partialUpdatedSimulado.notaMat(UPDATED_NOTA_MAT).notaPort(UPDATED_NOTA_PORT).notaLang(UPDATED_NOTA_LANG).notaHum(UPDATED_NOTA_HUM);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedSimulado.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedSimulado))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Simulado in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSimuladoUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedSimulado, simulado), getPersistedSimulado(simulado));
    }

    @Test
    void fullUpdateSimuladoWithPatch() throws Exception {
        // Initialize the database
        insertedSimulado = simuladoRepository.save(simulado).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the simulado using partial update
        Simulado partialUpdatedSimulado = new Simulado();
        partialUpdatedSimulado.setId(simulado.getId());

        partialUpdatedSimulado.notaMat(UPDATED_NOTA_MAT).notaPort(UPDATED_NOTA_PORT).notaLang(UPDATED_NOTA_LANG).notaHum(UPDATED_NOTA_HUM);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedSimulado.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedSimulado))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Simulado in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSimuladoUpdatableFieldsEquals(partialUpdatedSimulado, getPersistedSimulado(partialUpdatedSimulado));
    }

    @Test
    void patchNonExistingSimulado() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        simulado.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, simulado.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(simulado))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Simulado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchSimulado() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        simulado.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(simulado))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Simulado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamSimulado() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        simulado.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(simulado))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Simulado in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteSimulado() {
        // Initialize the database
        insertedSimulado = simuladoRepository.save(simulado).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the simulado
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, simulado.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return simuladoRepository.count().block();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Simulado getPersistedSimulado(Simulado simulado) {
        return simuladoRepository.findById(simulado.getId()).block();
    }

    protected void assertPersistedSimuladoToMatchAllProperties(Simulado expectedSimulado) {
        // Test fails because reactive api returns an empty object instead of null
        // assertSimuladoAllPropertiesEquals(expectedSimulado, getPersistedSimulado(expectedSimulado));
        assertSimuladoUpdatableFieldsEquals(expectedSimulado, getPersistedSimulado(expectedSimulado));
    }

    protected void assertPersistedSimuladoToMatchUpdatableProperties(Simulado expectedSimulado) {
        // Test fails because reactive api returns an empty object instead of null
        // assertSimuladoAllUpdatablePropertiesEquals(expectedSimulado, getPersistedSimulado(expectedSimulado));
        assertSimuladoUpdatableFieldsEquals(expectedSimulado, getPersistedSimulado(expectedSimulado));
    }
}
