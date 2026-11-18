export const PRIORIDADES = {
  CRITICA: { nome: 'CRÍTICA', cor: '#B00020' },
  ALTA: { nome: 'ALTA', cor: '#C2410C' },
  MEDIA: { nome: 'MÉDIA', cor: '#8A6100' },
  BAIXA: { nome: 'BAIXA', cor: '#1E7B34' },
};

export const TIPOS = {
  ALAGAMENTO: { nome: 'Alagamento', icone: '🌊' },
  CALOR: { nome: 'Calor', icone: '🌡️' },
  FALTA_SERVICO: { nome: 'Falta de serviço', icone: '⚡' },
};

export const ORIGENS = {
  SENSOR: 'Sensor',
  BASE_PUBLICA: 'Órgão público',
  COMUNIDADE: 'Morador',
};

export function nivelConfianca(valor) {
  if (valor >= 0.75) return 'Alta';
  if (valor >= 0.5) return 'Média';
  return 'Baixa';
}

export function tempoAtras(data) {
  if (!data) return 'nunca';
  const segundos = Math.floor((Date.now() - new Date(data).getTime()) / 1000);
  if (segundos < 5) return 'agora';
  if (segundos < 60) return 'há ' + segundos + 's';
  const minutos = Math.floor(segundos / 60);
  if (minutos < 60) return 'há ' + minutos + ' min';
  return 'há ' + Math.floor(minutos / 60) + ' h';
}
