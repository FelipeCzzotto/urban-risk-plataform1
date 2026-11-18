// faz a requisicao e desiste depois de 4 segundos (para nao travar o app)
async function requisicao(url, opcoes = {}) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 4000);
  try {
    const resposta = await fetch(url, { ...opcoes, signal: controller.signal });
    const dados = await resposta.json();
    if (!resposta.ok) {
      throw new Error(dados.erro || 'Erro ' + resposta.status);
    }
    return dados;
  } finally {
    clearTimeout(timer);
  }
}

export function buscarAlertas(baseUrl) {
  return requisicao(baseUrl + '/api/alertas');
}

export function buscarResumo(baseUrl) {
  return requisicao(baseUrl + '/api/resumo');
}

export function enviarRelato(baseUrl, relato) {
  return requisicao(baseUrl + '/api/relatos', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(relato),
  });
}
