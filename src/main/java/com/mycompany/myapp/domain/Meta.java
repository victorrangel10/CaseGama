package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.myapp.domain.enumeration.AreaDoEnem;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Meta.
 */
@Table("meta")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Meta implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Column("valor")
    private Integer valor;

    @NotNull(message = "must not be null")
    @Column("area")
    private AreaDoEnem area;

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "metas", "simulados" }, allowSetters = true)
    private Aluno aluno;

    @Column("aluno_id")
    private Long alunoId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Meta id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getValor() {
        return this.valor;
    }

    public Meta valor(Integer valor) {
        this.setValor(valor);
        return this;
    }

    public void setValor(Integer valor) {
        this.valor = valor;
    }

    public AreaDoEnem getArea() {
        return this.area;
    }

    public Meta area(AreaDoEnem area) {
        this.setArea(area);
        return this;
    }

    public void setArea(AreaDoEnem area) {
        this.area = area;
    }

    public Aluno getAluno() {
        return this.aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
        this.alunoId = aluno != null ? aluno.getId() : null;
    }

    public Meta aluno(Aluno aluno) {
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
        if (!(o instanceof Meta)) {
            return false;
        }
        return getId() != null && getId().equals(((Meta) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Meta{" +
            "id=" + getId() +
            ", valor=" + getValor() +
            ", area='" + getArea() + "'" +
            "}";
    }
}
