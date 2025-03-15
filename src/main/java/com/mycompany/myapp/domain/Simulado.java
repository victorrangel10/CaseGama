package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Simulado.
 */
@Table("simulado")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Simulado implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @Column("nota_mat")
    private Integer notaMat;

    @Column("nota_port")
    private Integer notaPort;

    @Column("nota_lang")
    private Integer notaLang;

    @Column("nota_hum")
    private Integer notaHum;

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "metas", "simulados" }, allowSetters = true)
    private Aluno aluno;

    @Column("aluno_id")
    private Long alunoId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Simulado id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNotaMat() {
        return this.notaMat;
    }

    public Simulado notaMat(Integer notaMat) {
        this.setNotaMat(notaMat);
        return this;
    }

    public void setNotaMat(Integer notaMat) {
        this.notaMat = notaMat;
    }

    public Integer getNotaPort() {
        return this.notaPort;
    }

    public Simulado notaPort(Integer notaPort) {
        this.setNotaPort(notaPort);
        return this;
    }

    public void setNotaPort(Integer notaPort) {
        this.notaPort = notaPort;
    }

    public Integer getNotaLang() {
        return this.notaLang;
    }

    public Simulado notaLang(Integer notaLang) {
        this.setNotaLang(notaLang);
        return this;
    }

    public void setNotaLang(Integer notaLang) {
        this.notaLang = notaLang;
    }

    public Integer getNotaHum() {
        return this.notaHum;
    }

    public Simulado notaHum(Integer notaHum) {
        this.setNotaHum(notaHum);
        return this;
    }

    public void setNotaHum(Integer notaHum) {
        this.notaHum = notaHum;
    }

    public Aluno getAluno() {
        return this.aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
        this.alunoId = aluno != null ? aluno.getId() : null;
    }

    public Simulado aluno(Aluno aluno) {
        this.setAluno(aluno);
        return this;
    }

    public Long getAlunoId() {
        return this.alunoId;
    }

    public void setAlunoId(Long aluno) {
        this.alunoId = aluno;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Simulado)) {
            return false;
        }
        return getId() != null && getId().equals(((Simulado) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Simulado{" +
            "id=" + getId() +
            ", notaMat=" + getNotaMat() +
            ", notaPort=" + getNotaPort() +
            ", notaLang=" + getNotaLang() +
            ", notaHum=" + getNotaHum() +
            "}";
    }
}
