package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.AlunoAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Aluno;
import com.mycompany.myapp.repository.AlunoRepository;
import com.mycompany.myapp.repository.EntityManager;
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
 * Integration tests for the {@link AlunoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class AlunoResourceIT {

    private static final String DEFAULT_NOME = "AAAAAAAAAA";
    private static final String UPDATED_NOME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/alunos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Aluno aluno;

    private Aluno insertedAluno;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Aluno createEntity() {
        return new Aluno().nome(DEFAULT_NOME);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Aluno createUpdatedEntity() {
        return new Aluno().nome(UPDATED_NOME);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Aluno.class).block();
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
        aluno = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedAluno != null) {
            alunoRepository.delete(insertedAluno).block();
            insertedAluno = null;
        }
        deleteEntities(em);
    }

    @Test
    void createAluno() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Aluno
        var returnedAluno = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(aluno))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Aluno.class)
            .returnResult()
            .getResponseBody();

        // Validate the Aluno in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertAlunoUpdatableFieldsEquals(returnedAluno, getPersistedAluno(returnedAluno));

        insertedAluno = returnedAluno;
    }

    @Test
    void createAlunoWithExistingId() throws Exception {
        // Create the Aluno with an existing ID
        aluno.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(aluno))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Aluno in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkNomeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        aluno.setNome(null);

        // Create the Aluno, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(aluno))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllAlunosAsStream() {
        // Initialize the database
        alunoRepository.save(aluno).block();

        List<Aluno> alunoList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Aluno.class)
            .getResponseBody()
            .filter(aluno::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(alunoList).isNotNull();
        assertThat(alunoList).hasSize(1);
        Aluno testAluno = alunoList.get(0);

        // Test fails because reactive api returns an empty object instead of null
        // assertAlunoAllPropertiesEquals(aluno, testAluno);
        assertAlunoUpdatableFieldsEquals(aluno, testAluno);
    }

    @Test
    void getAllAlunos() {
        // Initialize the database
        insertedAluno = alunoRepository.save(aluno).block();

        // Get all the alunoList
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
            .value(hasItem(aluno.getId().intValue()))
            .jsonPath("$.[*].nome")
            .value(hasItem(DEFAULT_NOME));
    }

    @Test
    void getAluno() {
        // Initialize the database
        insertedAluno = alunoRepository.save(aluno).block();

        // Get the aluno
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, aluno.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(aluno.getId().intValue()))
            .jsonPath("$.nome")
            .value(is(DEFAULT_NOME));
    }

    @Test
    void getNonExistingAluno() {
        // Get the aluno
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingAluno() throws Exception {
        // Initialize the database
        insertedAluno = alunoRepository.save(aluno).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the aluno
        Aluno updatedAluno = alunoRepository.findById(aluno.getId()).block();
        updatedAluno.nome(UPDATED_NOME);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedAluno.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedAluno))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Aluno in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAlunoToMatchAllProperties(updatedAluno);
    }

    @Test
    void putNonExistingAluno() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        aluno.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, aluno.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(aluno))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Aluno in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchAluno() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        aluno.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(aluno))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Aluno in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamAluno() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        aluno.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(aluno))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Aluno in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateAlunoWithPatch() throws Exception {
        // Initialize the database
        insertedAluno = alunoRepository.save(aluno).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the aluno using partial update
        Aluno partialUpdatedAluno = new Aluno();
        partialUpdatedAluno.setId(aluno.getId());

        partialUpdatedAluno.nome(UPDATED_NOME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAluno.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedAluno))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Aluno in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAlunoUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedAluno, aluno), getPersistedAluno(aluno));
    }

    @Test
    void fullUpdateAlunoWithPatch() throws Exception {
        // Initialize the database
        insertedAluno = alunoRepository.save(aluno).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the aluno using partial update
        Aluno partialUpdatedAluno = new Aluno();
        partialUpdatedAluno.setId(aluno.getId());

        partialUpdatedAluno.nome(UPDATED_NOME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAluno.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedAluno))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Aluno in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAlunoUpdatableFieldsEquals(partialUpdatedAluno, getPersistedAluno(partialUpdatedAluno));
    }

    @Test
    void patchNonExistingAluno() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        aluno.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, aluno.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(aluno))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Aluno in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchAluno() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        aluno.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(aluno))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Aluno in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamAluno() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        aluno.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(aluno))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Aluno in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteAluno() {
        // Initialize the database
        insertedAluno = alunoRepository.save(aluno).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the aluno
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, aluno.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return alunoRepository.count().block();
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

    protected Aluno getPersistedAluno(Aluno aluno) {
        return alunoRepository.findById(aluno.getId()).block();
    }

    protected void assertPersistedAlunoToMatchAllProperties(Aluno expectedAluno) {
        // Test fails because reactive api returns an empty object instead of null
        // assertAlunoAllPropertiesEquals(expectedAluno, getPersistedAluno(expectedAluno));
        assertAlunoUpdatableFieldsEquals(expectedAluno, getPersistedAluno(expectedAluno));
    }

    protected void assertPersistedAlunoToMatchUpdatableProperties(Aluno expectedAluno) {
        // Test fails because reactive api returns an empty object instead of null
        // assertAlunoAllUpdatablePropertiesEquals(expectedAluno, getPersistedAluno(expectedAluno));
        assertAlunoUpdatableFieldsEquals(expectedAluno, getPersistedAluno(expectedAluno));
    }
}
