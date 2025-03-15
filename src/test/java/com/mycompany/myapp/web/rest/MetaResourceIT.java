package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.MetaAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Meta;
import com.mycompany.myapp.domain.enumeration.AreaDoEnem;
import com.mycompany.myapp.repository.EntityManager;
import com.mycompany.myapp.repository.MetaRepository;
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
 * Integration tests for the {@link MetaResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class MetaResourceIT {

    private static final Integer DEFAULT_VALOR = 1;
    private static final Integer UPDATED_VALOR = 2;

    private static final AreaDoEnem DEFAULT_AREA = AreaDoEnem.LINGUAGENS;
    private static final AreaDoEnem UPDATED_AREA = AreaDoEnem.MATEMATICA;

    private static final String ENTITY_API_URL = "/api/metas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MetaRepository metaRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Meta meta;

    private Meta insertedMeta;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Meta createEntity() {
        return new Meta().valor(DEFAULT_VALOR).area(DEFAULT_AREA);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Meta createUpdatedEntity() {
        return new Meta().valor(UPDATED_VALOR).area(UPDATED_AREA);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Meta.class).block();
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
        meta = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedMeta != null) {
            metaRepository.delete(insertedMeta).block();
            insertedMeta = null;
        }
        deleteEntities(em);
    }

    @Test
    void createMeta() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Meta
        var returnedMeta = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(meta))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Meta.class)
            .returnResult()
            .getResponseBody();

        // Validate the Meta in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertMetaUpdatableFieldsEquals(returnedMeta, getPersistedMeta(returnedMeta));

        insertedMeta = returnedMeta;
    }

    @Test
    void createMetaWithExistingId() throws Exception {
        // Create the Meta with an existing ID
        meta.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(meta))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Meta in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkValorIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        meta.setValor(null);

        // Create the Meta, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(meta))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkAreaIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        meta.setArea(null);

        // Create the Meta, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(meta))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllMetasAsStream() {
        // Initialize the database
        metaRepository.save(meta).block();

        List<Meta> metaList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Meta.class)
            .getResponseBody()
            .filter(meta::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(metaList).isNotNull();
        assertThat(metaList).hasSize(1);
        Meta testMeta = metaList.get(0);

        // Test fails because reactive api returns an empty object instead of null
        // assertMetaAllPropertiesEquals(meta, testMeta);
        assertMetaUpdatableFieldsEquals(meta, testMeta);
    }

    @Test
    void getAllMetas() {
        // Initialize the database
        insertedMeta = metaRepository.save(meta).block();

        // Get all the metaList
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
            .value(hasItem(meta.getId().intValue()))
            .jsonPath("$.[*].valor")
            .value(hasItem(DEFAULT_VALOR))
            .jsonPath("$.[*].area")
            .value(hasItem(DEFAULT_AREA.toString()));
    }

    @Test
    void getMeta() {
        // Initialize the database
        insertedMeta = metaRepository.save(meta).block();

        // Get the meta
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, meta.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(meta.getId().intValue()))
            .jsonPath("$.valor")
            .value(is(DEFAULT_VALOR))
            .jsonPath("$.area")
            .value(is(DEFAULT_AREA.toString()));
    }

    @Test
    void getNonExistingMeta() {
        // Get the meta
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingMeta() throws Exception {
        // Initialize the database
        insertedMeta = metaRepository.save(meta).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the meta
        Meta updatedMeta = metaRepository.findById(meta.getId()).block();
        updatedMeta.valor(UPDATED_VALOR).area(UPDATED_AREA);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedMeta.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedMeta))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Meta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMetaToMatchAllProperties(updatedMeta);
    }

    @Test
    void putNonExistingMeta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        meta.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, meta.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(meta))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Meta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchMeta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        meta.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(meta))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Meta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamMeta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        meta.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(meta))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Meta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateMetaWithPatch() throws Exception {
        // Initialize the database
        insertedMeta = metaRepository.save(meta).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the meta using partial update
        Meta partialUpdatedMeta = new Meta();
        partialUpdatedMeta.setId(meta.getId());

        partialUpdatedMeta.valor(UPDATED_VALOR).area(UPDATED_AREA);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedMeta.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedMeta))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Meta in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMetaUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedMeta, meta), getPersistedMeta(meta));
    }

    @Test
    void fullUpdateMetaWithPatch() throws Exception {
        // Initialize the database
        insertedMeta = metaRepository.save(meta).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the meta using partial update
        Meta partialUpdatedMeta = new Meta();
        partialUpdatedMeta.setId(meta.getId());

        partialUpdatedMeta.valor(UPDATED_VALOR).area(UPDATED_AREA);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedMeta.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedMeta))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Meta in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMetaUpdatableFieldsEquals(partialUpdatedMeta, getPersistedMeta(partialUpdatedMeta));
    }

    @Test
    void patchNonExistingMeta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        meta.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, meta.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(meta))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Meta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchMeta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        meta.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(meta))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Meta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamMeta() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        meta.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(meta))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Meta in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteMeta() {
        // Initialize the database
        insertedMeta = metaRepository.save(meta).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the meta
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, meta.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return metaRepository.count().block();
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

    protected Meta getPersistedMeta(Meta meta) {
        return metaRepository.findById(meta.getId()).block();
    }

    protected void assertPersistedMetaToMatchAllProperties(Meta expectedMeta) {
        // Test fails because reactive api returns an empty object instead of null
        // assertMetaAllPropertiesEquals(expectedMeta, getPersistedMeta(expectedMeta));
        assertMetaUpdatableFieldsEquals(expectedMeta, getPersistedMeta(expectedMeta));
    }

    protected void assertPersistedMetaToMatchUpdatableProperties(Meta expectedMeta) {
        // Test fails because reactive api returns an empty object instead of null
        // assertMetaAllUpdatablePropertiesEquals(expectedMeta, getPersistedMeta(expectedMeta));
        assertMetaUpdatableFieldsEquals(expectedMeta, getPersistedMeta(expectedMeta));
    }
}
