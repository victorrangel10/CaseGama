import { ISimulado, NewSimulado } from './simulado.model';

export const sampleWithRequiredData: ISimulado = {
  id: 17376,
};

export const sampleWithPartialData: ISimulado = {
  id: 21471,
  notaMat: 16521,
  notaLang: 30588,
};

export const sampleWithFullData: ISimulado = {
  id: 3246,
  notaMat: 11108,
  notaPort: 19258,
  notaLang: 16937,
  notaHum: 4861,
};

export const sampleWithNewData: NewSimulado = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
