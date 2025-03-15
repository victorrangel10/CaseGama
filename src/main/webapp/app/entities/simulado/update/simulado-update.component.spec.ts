import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IAluno } from 'app/entities/aluno/aluno.model';
import { AlunoService } from 'app/entities/aluno/service/aluno.service';
import { SimuladoService } from '../service/simulado.service';
import { ISimulado } from '../simulado.model';
import { SimuladoFormService } from './simulado-form.service';

import { SimuladoUpdateComponent } from './simulado-update.component';

describe('Simulado Management Update Component', () => {
  let comp: SimuladoUpdateComponent;
  let fixture: ComponentFixture<SimuladoUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let simuladoFormService: SimuladoFormService;
  let simuladoService: SimuladoService;
  let alunoService: AlunoService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [SimuladoUpdateComponent],
      providers: [
        provideHttpClient(),
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(SimuladoUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(SimuladoUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    simuladoFormService = TestBed.inject(SimuladoFormService);
    simuladoService = TestBed.inject(SimuladoService);
    alunoService = TestBed.inject(AlunoService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call Aluno query and add missing value', () => {
      const simulado: ISimulado = { id: 11574 };
      const aluno: IAluno = { id: 15328 };
      simulado.aluno = aluno;

      const alunoCollection: IAluno[] = [{ id: 15328 }];
      jest.spyOn(alunoService, 'query').mockReturnValue(of(new HttpResponse({ body: alunoCollection })));
      const additionalAlunos = [aluno];
      const expectedCollection: IAluno[] = [...additionalAlunos, ...alunoCollection];
      jest.spyOn(alunoService, 'addAlunoToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ simulado });
      comp.ngOnInit();

      expect(alunoService.query).toHaveBeenCalled();
      expect(alunoService.addAlunoToCollectionIfMissing).toHaveBeenCalledWith(
        alunoCollection,
        ...additionalAlunos.map(expect.objectContaining),
      );
      expect(comp.alunosSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const simulado: ISimulado = { id: 11574 };
      const aluno: IAluno = { id: 15328 };
      simulado.aluno = aluno;

      activatedRoute.data = of({ simulado });
      comp.ngOnInit();

      expect(comp.alunosSharedCollection).toContainEqual(aluno);
      expect(comp.simulado).toEqual(simulado);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ISimulado>>();
      const simulado = { id: 22678 };
      jest.spyOn(simuladoFormService, 'getSimulado').mockReturnValue(simulado);
      jest.spyOn(simuladoService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ simulado });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: simulado }));
      saveSubject.complete();

      // THEN
      expect(simuladoFormService.getSimulado).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(simuladoService.update).toHaveBeenCalledWith(expect.objectContaining(simulado));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ISimulado>>();
      const simulado = { id: 22678 };
      jest.spyOn(simuladoFormService, 'getSimulado').mockReturnValue({ id: null });
      jest.spyOn(simuladoService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ simulado: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: simulado }));
      saveSubject.complete();

      // THEN
      expect(simuladoFormService.getSimulado).toHaveBeenCalled();
      expect(simuladoService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ISimulado>>();
      const simulado = { id: 22678 };
      jest.spyOn(simuladoService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ simulado });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(simuladoService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareAluno', () => {
      it('Should forward to alunoService', () => {
        const entity = { id: 15328 };
        const entity2 = { id: 9303 };
        jest.spyOn(alunoService, 'compareAluno');
        comp.compareAluno(entity, entity2);
        expect(alunoService.compareAluno).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
