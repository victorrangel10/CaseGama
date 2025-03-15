import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IAluno } from 'app/entities/aluno/aluno.model';
import { AlunoService } from 'app/entities/aluno/service/aluno.service';
import { ISimulado } from '../simulado.model';
import { SimuladoService } from '../service/simulado.service';
import { SimuladoFormGroup, SimuladoFormService } from './simulado-form.service';

@Component({
  selector: 'jhi-simulado-update',
  templateUrl: './simulado-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class SimuladoUpdateComponent implements OnInit {
  isSaving = false;
  simulado: ISimulado | null = null;

  alunosSharedCollection: IAluno[] = [];

  protected simuladoService = inject(SimuladoService);
  protected simuladoFormService = inject(SimuladoFormService);
  protected alunoService = inject(AlunoService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: SimuladoFormGroup = this.simuladoFormService.createSimuladoFormGroup();

  compareAluno = (o1: IAluno | null, o2: IAluno | null): boolean => this.alunoService.compareAluno(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ simulado }) => {
      this.simulado = simulado;
      if (simulado) {
        this.updateForm(simulado);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const simulado = this.simuladoFormService.getSimulado(this.editForm);
    if (simulado.id !== null) {
      this.subscribeToSaveResponse(this.simuladoService.update(simulado));
    } else {
      this.subscribeToSaveResponse(this.simuladoService.create(simulado));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ISimulado>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(simulado: ISimulado): void {
    this.simulado = simulado;
    this.simuladoFormService.resetForm(this.editForm, simulado);

    this.alunosSharedCollection = this.alunoService.addAlunoToCollectionIfMissing<IAluno>(this.alunosSharedCollection, simulado.aluno);
  }

  protected loadRelationshipsOptions(): void {
    this.alunoService
      .query()
      .pipe(map((res: HttpResponse<IAluno[]>) => res.body ?? []))
      .pipe(map((alunos: IAluno[]) => this.alunoService.addAlunoToCollectionIfMissing<IAluno>(alunos, this.simulado?.aluno)))
      .subscribe((alunos: IAluno[]) => (this.alunosSharedCollection = alunos));
  }
}
