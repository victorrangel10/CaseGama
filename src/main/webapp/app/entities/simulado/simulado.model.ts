import { IAluno } from 'app/entities/aluno/aluno.model';

export interface ISimulado {
  id: number;
  notaMat?: number | null;
  notaPort?: number | null;
  notaLang?: number | null;
  notaHum?: number | null;
  aluno?: IAluno | null;
}

export type NewSimulado = Omit<ISimulado, 'id'> & { id: null };
