# Urban Risk Intelligence Platform

Global Solution 2026.2 - Advanced Programming And Mobile Dev

Plataforma para monitorar riscos urbanos do dia a dia: **alagamentos**, **calor extremo** e **falta de serviços** (luz, água). O backend recebe dados de sensores, de órgãos públicos e relatos de moradores, calcula a prioridade de cada alerta e o app mostra tudo em tempo real.

## Integrante

| Nome | RM |
|------|----|
| Felipe Chiozzotto Gozzani | RM554715 |

## Tecnologias

- **Backend:** Java 17, Spring Boot 3, Spring Data JPA, banco H2
- **Mobile:** React Native com Expo

## Estrutura

```
backend/
  model/        entidade Evento e enums (TipoEvento, Origem, Prioridade)
  repository/   acesso ao banco
  dto/          dados recebidos pela API
  service/      regras de negócio, análise de risco e simulador de sensores
  controller/   endpoints REST e tratamento de erros
  config/       CORS
mobile/
  App.js        navegação entre as telas e atualização automática
  src/telas/    Painel, Eventos, Relatar e Config
  src/componentes/  card do evento e aviso de conexão
```

O backend foi feito em camadas (controller → service → repository) para separar as responsabilidades.

## Como a prioridade é calculada

A classe `AnaliseRiscoService` faz a análise de cada evento:

1. **Severidade (0 a 1):** depende do tipo. Alagamento usa o nível da água (50 cm = máximo), calor usa a temperatura (30 °C = 0, 42 °C = máximo) e falta de serviço usa o número de pessoas afetadas.
2. **Confiança:** depende da origem do dado. Sensor = 85%, órgão público = 90%, relato de morador = 40%. O relato ganha +10% para cada outra fonte que confirma o mesmo problema no mesmo bairro (até 80%).
3. **Subida rápida:** o valor novo do sensor é comparado com a média das últimas leituras. Se subiu muito de repente, ganha pontos extras.

```
pontuação = severidade × 50 + confiança × 30 + confirmações × 5 (máx. 15) + 10 se subiu rápido
```

| Pontuação | Prioridade |
|---|---|
| 70 ou mais | CRÍTICA |
| 50 a 69 | ALTA |
| 30 a 49 | MÉDIA |
| abaixo de 30 | BAIXA |

Todo evento retornado pela API informa a **origem** (`SENSOR`, `BASE_PUBLICA` ou `COMUNIDADE`), a **fonte**, a **confiança** e o **motivo da confiança**.

Leituras normais de sensor não viram alerta. Quando o valor volta ao normal, o alerta daquele sensor é encerrado automaticamente.

## Proteção de dados, escalabilidade e disponibilidade

- O app não pede login nem localização.
- O contato do morador (opcional) é salvo apenas como hash SHA-256 e nunca é devolvido pela API.
- Telefones e e-mails escritos na descrição do relato são removidos antes de salvar.
- Sensores, órgãos públicos e a ação de resolver evento precisam do header `X-API-KEY`.
- A API não guarda sessão, então pode rodar em várias instâncias atrás de um balanceador. Para produção, a ideia é trocar o H2 por PostgreSQL.
- `/actuator/health` mostra se a API está no ar.
- O app não trava se o servidor cair: continua mostrando os últimos dados, avisa que está desconectado e tenta reconectar sozinho a cada 5 segundos.

## Como rodar

### Backend

Precisa de Java 17+ e Maven.

```bash
cd backend
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. Um simulador gera leituras de sensores a cada 6 segundos para alguns bairros de São Paulo, então os alertas começam a aparecer sozinhos.

Para rodar os testes:

```bash
cd backend
mvn test
```

### App

Precisa de Node.js e do app Expo Go no celular.

```bash
cd mobile
npm install
npx expo install --fix
npx expo start
```

Leia o QR code com o Expo Go. Depois, na aba **Config** do app, coloque o endereço do backend:

- celular na mesma rede Wi-Fi: `http://IP_DO_COMPUTADOR:8080`
- emulador Android: `http://10.0.2.2:8080`
- GitHub Codespaces: o endereço da porta 8080 (deixe a porta como pública) e rode o app com `npx expo start --tunnel`

## Endpoints

| Método | Rota | O que faz |
|---|---|---|
| GET | `/api/alertas` | alertas ativos, do mais urgente para o menos urgente |
| GET | `/api/resumo` | totais para o painel |
| GET | `/api/eventos/{id}` | detalhes de um evento |
| POST | `/api/sensores` | leitura de sensor (precisa de `X-API-KEY`) |
| POST | `/api/publico` | dado de órgão público (precisa de `X-API-KEY`) |
| POST | `/api/relatos` | relato de morador (`valor` = intensidade de 1 a 5) |
| PUT | `/api/eventos/{id}/resolver` | encerra um evento (precisa de `X-API-KEY`) |

A chave padrão é `fiap2026` (fica no `application.properties`).

Exemplo de relato:

```bash
curl -X POST http://localhost:8080/api/relatos -H "Content-Type: application/json" \
  -d '{"tipo":"ALAGAMENTO","valor":4,"latitude":-23.5869,"longitude":-46.61,"bairro":"Ipiranga","descricao":"rua alagada"}'
```

Exemplo de sensor:

```bash
curl -X POST http://localhost:8080/api/sensores -H "Content-Type: application/json" -H "X-API-KEY: fiap2026" \
  -d '{"tipo":"ALAGAMENTO","valor":70,"latitude":-23.5869,"longitude":-46.61,"bairro":"Ipiranga","fonte":"SENSOR-TESTE"}'
```

## Demonstração

1. Subir o backend e abrir `/api/alertas` no navegador.
2. Abrir o app e mostrar o painel atualizando a cada 5 segundos.
3. Enviar um relato ou um pico de sensor e mostrar a notificação chegando.
4. Parar o backend (Ctrl+C): o app mostra "Sem conexão com o servidor" e continua funcionando com os últimos dados.
5. Subir o backend de novo: o app reconecta sozinho e mostra "Conexão restabelecida".

## Indicadores

- **Tempo de atualização:** o app atualiza a cada 5 s e mostra quanto tempo a API levou para responder.
- **Qualidade dos alertas:** cada alerta mostra origem, confiança e pontuação, e relatos confirmados por outras fontes sobem de prioridade.
- **Facilidade de uso:** 4 abas, botões grandes, prioridade escrita por extenso (não só cor) e rótulos de acessibilidade para leitor de tela.
