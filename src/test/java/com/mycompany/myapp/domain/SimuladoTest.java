package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.AlunoTestSamples.*;
import static com.mycompany.myapp.domain.SimuladoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SimuladoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Simulado.class);
        Simulado simulado1 = getSimuladoSample1();
        Simulado simulado2 = new Simulado();
        assertThat(simulado1).isNotEqualTo(simulado2);

        simulado2.setId(simulado1.getId());
        assertThat(simulado1).isEqualTo(simulado2);

        simulado2 = getSimuladoSample2();
        assertThat(simulado1).isNotEqualTo(simulado2);
    }

    @Test
    void alunoTest() {
        Simulado simulado = getSimuladoRandomSampleGenerator();
        Aluno alunoBack = getAlunoRandomSampleGenerator();

        simulado.setAluno(alunoBack);
        assertThat(simulado.getAluno()).isEqualTo(alunoBack);

        simulado.aluno(null);
        assertThat(simulado.getAluno()).isNull();
    }
}
