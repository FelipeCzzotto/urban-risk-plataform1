package br.com.fiap.urbanrisk.service;

import br.com.fiap.urbanrisk.dto.EventoRequest;
import br.com.fiap.urbanrisk.model.Evento;
import br.com.fiap.urbanrisk.model.Origem;
import br.com.fiap.urbanrisk.model.Prioridade;
import br.com.fiap.urbanrisk.model.TipoEvento;
import br.com.fiap.urbanrisk.repository.EventoRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class EventoService {

    // abaixo disso a leitura do sensor e considerada normal e nao vira alerta
    private static final double LIMITE_SENSOR = 0.4;

    private final EventoRepository repository;
    private final AnaliseRiscoService analise;

    // historico das ultimas leituras de cada sensor (em memoria)
    private final Map<String, LinkedList<Double>> historicoSensores = new HashMap<>();

    public EventoService(EventoRepository repository, AnaliseRiscoService analise) {
        this.repository = repository;
        this.analise = analise;
    }

    public synchronized Evento registrarSensor(EventoRequest req) {
        if (req.fonte() == null || req.fonte().isBlank()) {
            throw new IllegalArgumentException("fonte (id do sensor) e obrigatoria");
        }

        LinkedList<Double> historico = historicoSensores.computeIfAbsent(req.fonte(), k -> new LinkedList<>());
        boolean subiuRapido = analise.subiuRapido(historico, req.valor());
        historico.add(req.valor());
        if (historico.size() > 10) {
            historico.removeFirst();
        }

        double severidade = analise.calcularSeveridade(req.tipo(), req.valor());
        Optional<Evento> existente = repository.findFirstByFonteAndAtivoTrue(req.fonte());

        // voltou ao normal: encerra o alerta que existia
        if (severidade < LIMITE_SENSOR) {
            existente.ifPresent(e -> {
                e.setAtivo(false);
                repository.save(e);
            });
            return null;
        }

        Evento evento = existente.orElse(new Evento());
        preencher(evento, req, Origem.SENSOR);
        evento.setSeveridade(severidade);
        evento.setDescricao(descricaoSensor(req.tipo(), req.valor(), subiuRapido));
        analisar(evento, subiuRapido);
        return repository.save(evento);
    }

    public Evento registrarBasePublica(EventoRequest req) {
        String fonte = req.fonte() == null ? "Base publica" : req.fonte();
        Evento evento = repository.findFirstByFonteAndAtivoTrue(fonte).orElse(new Evento());
        preencher(evento, req, Origem.BASE_PUBLICA);
        evento.setFonte(fonte);
        evento.setSeveridade(analise.calcularSeveridade(req.tipo(), req.valor()));
        evento.setDescricao(req.descricao());
        analisar(evento, false);
        return repository.save(evento);
    }

    public Evento registrarRelato(EventoRequest req) {
        if (req.valor() < 1 || req.valor() > 5) {
            throw new IllegalArgumentException("intensidade deve ser de 1 a 5");
        }

        Evento evento = new Evento();
        preencher(evento, req, Origem.COMUNIDADE);
        evento.setFonte("Morador");
        evento.setSeveridade(req.valor() / 5.0);
        evento.setDescricao(removerDadosPessoais(req.descricao()));
        evento.setContatoHash(gerarHash(req.contato()));
        analisar(evento, false);
        Evento salvo = repository.save(evento);

        // o novo relato tambem reforca os relatos parecidos que ja existiam
        for (Evento outro : buscarParecidos(req.tipo(), req.bairro(), salvo.getId())) {
            if (outro.getOrigem() == Origem.COMUNIDADE) {
                analisar(outro, false);
                repository.save(outro);
            }
        }
        return salvo;
    }

    public List<Evento> listarAlertas() {
        return repository.findByAtivoTrueOrderByPontuacaoDesc();
    }

    public Evento buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new NoSuchElementException("Evento nao encontrado"));
    }

    public Evento resolver(Long id) {
        Evento evento = buscarPorId(id);
        evento.setAtivo(false);
        return repository.save(evento);
    }

    public Map<String, Object> resumo() {
        List<Evento> ativos = repository.findByAtivoTrueOrderByPontuacaoDesc();

        Map<String, Long> porPrioridade = new LinkedHashMap<>();
        for (Prioridade p : Prioridade.values()) {
            porPrioridade.put(p.name(), ativos.stream().filter(e -> e.getPrioridade() == p).count());
        }

        Map<String, Long> porTipo = new LinkedHashMap<>();
        for (TipoEvento t : TipoEvento.values()) {
            porTipo.put(t.name(), ativos.stream().filter(e -> e.getTipo() == t).count());
        }

        Map<String, Long> porBairro = new TreeMap<>();
        for (Evento e : ativos) {
            porBairro.merge(e.getBairro(), 1L, Long::sum);
        }

        Map<String, Object> resumo = new LinkedHashMap<>();
        resumo.put("total", ativos.size());
        resumo.put("porPrioridade", porPrioridade);
        resumo.put("porTipo", porTipo);
        resumo.put("porBairro", porBairro);
        resumo.put("horario", Instant.now());
        return resumo;
    }

    private void preencher(Evento evento, EventoRequest req, Origem origem) {
        evento.setTipo(req.tipo());
        evento.setOrigem(origem);
        evento.setFonte(req.fonte());
        evento.setBairro(req.bairro());
        evento.setLatitude(req.latitude());
        evento.setLongitude(req.longitude());
        evento.setValor(req.valor());
    }

    // calcula confianca, pontuacao e prioridade do evento
    private void analisar(Evento evento, boolean subiuRapido) {
        int confirmacoes = buscarParecidos(evento.getTipo(), evento.getBairro(), evento.getId()).size();
        double confianca = analise.calcularConfianca(evento.getOrigem(), confirmacoes);
        double pontuacao = analise.calcularPontuacao(evento.getSeveridade(), confianca, confirmacoes, subiuRapido);

        evento.setConfirmacoes(confirmacoes);
        evento.setConfianca(confianca);
        evento.setMotivoConfianca(analise.explicarConfianca(evento.getOrigem(), confirmacoes));
        evento.setPontuacao(pontuacao);
        evento.setPrioridade(analise.definirPrioridade(pontuacao));
    }

    // eventos ativos do mesmo tipo, no mesmo bairro, nas ultimas 3 horas
    private List<Evento> buscarParecidos(TipoEvento tipo, String bairro, Long ignorarId) {
        Instant limite = Instant.now().minus(Duration.ofHours(3));
        List<Evento> lista = new ArrayList<>(repository.findByTipoAndBairroAndAtivoTrueAndAtualizadoEmAfter(tipo, bairro, limite));
        lista.removeIf(e -> e.getId().equals(ignorarId));
        return lista;
    }

    private String descricaoSensor(TipoEvento tipo, double valor, boolean subiuRapido) {
        String texto;
        if (tipo == TipoEvento.ALAGAMENTO) {
            texto = String.format("Nivel da agua em %.0f cm", valor);
        } else if (tipo == TipoEvento.CALOR) {
            texto = String.format("Temperatura de %.1f graus", valor);
        } else {
            texto = String.format("Cerca de %.0f pessoas sem servico", valor);
        }
        if (subiuRapido) {
            texto += " (subindo rapido)";
        }
        return texto;
    }

    // LGPD: tira e-mail e telefone que a pessoa escreveu na descricao
    String removerDadosPessoais(String texto) {
        if (texto == null) return null;
        return texto
                .replaceAll("[\\w.+-]+@[\\w-]+\\.[\\w.]+", "[removido]")
                .replaceAll("\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}", "[removido]");
    }

    // LGPD: transforma o contato em um hash SHA-256 (nao da para voltar ao original)
    String gerarHash(String contato) {
        if (contato == null || contato.isBlank()) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(contato.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
