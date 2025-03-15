package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Aluno.
 */
@Table("aluno")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Aluno implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Column("nome")
    private String nome;

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "aluno" }, allowSetters = true)
    private Set<Meta> metas = new HashSet<>();

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "aluno" }, allowSetters = true)
    private Set<Simulado> simulados = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Aluno id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return this.nome;
    }

    public Aluno nome(String nome) {
        this.setNome(nome);
        return this;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Set<Meta> getMetas() {
        return this.metas;
    }

    public void setMetas(Set<Meta> metas) {
        if (this.metas != null) {
            this.metas.forEach(i -> i.setAluno(null));
        }
        if (metas != null) {
            metas.forEach(i -> i.setAluno(this));
        }
        this.metas = metas;
    }

    public Aluno metas(Set<Meta> metas) {
        this.setMetas(metas);
        return this;
    }

    public Aluno addMetas(Meta meta) {
        this.metas.add(meta);
        meta.setAluno(this);
        return this;
    }

    public Aluno removeMetas(Meta meta) {
        this.metas.remove(meta);
        meta.setAluno(null);
        return this;
    }

    public Set<Simulado> getSimulados() {
        return this.simulados;
    }

    public void setSimulados(Set<Simulado> simulados) {
        if (this.simulados != null) {
            this.simulados.forEach(i -> i.setAluno(null));
        }
        if (simulados != null) {
            simulados.forEach(i -> i.setAluno(this));
        }
        this.simulados = simulados;
    }

    public Aluno simulados(Set<Simulado> simulados) {
        this.setSimulados(simulados);
        return this;
    }

    public Aluno addSimulados(Simulado simulado) {
        this.simulados.add(simulado);
        simulado.setAluno(this);
        return this;
    }

    public Aluno removeSimulados(Simulado simulado) {
        this.simulados.remove(simulado);
        simulado.setAluno(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Aluno)) {
            return false;
        }
        return getId() != null && getId().equals(((Aluno) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Aluno{" +
            "id=" + getId() +
            ", nome='" + getNome() + "'" +
            "}";
    }
}
