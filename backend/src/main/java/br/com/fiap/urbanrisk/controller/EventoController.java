package br.com.fiap.urbanrisk.controller;

import br.com.fiap.urbanrisk.dto.EventoRequest;
import br.com.fiap.urbanrisk.model.Evento;
import br.com.fiap.urbanrisk.service.EventoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EventoController {

    private final EventoService service;

    @Value("${app.api-key}")
    private String apiKey;

    public EventoController(EventoService service) {
        this.service = service;
    }

    @GetMapping("/alertas")
    public List<Evento> alertas() {
        return service.listarAlertas();
    }

    @GetMapping("/resumo")
    public Map<String, Object> resumo() {
        return service.resumo();
    }

    @GetMapping("/eventos/{id}")
    public Evento buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping("/sensores")
    public ResponseEntity<?> sensor(@RequestHeader(value = "X-API-KEY", required = false) String chave,
                                    @Valid @RequestBody EventoRequest req) {
        if (!apiKey.equals(chave)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "chave invalida"));
        }
        Evento evento = service.registrarSensor(req);
        if (evento == null) {
            // leitura normal, so foi registrada
            return ResponseEntity.accepted().body(Map.of("mensagem", "leitura normal"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(evento);
    }

    @PostMapping("/publico")
    public ResponseEntity<?> basePublica(@RequestHeader(value = "X-API-KEY", required = false) String chave,
                                         @Valid @RequestBody EventoRequest req) {
        if (!apiKey.equals(chave)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "chave invalida"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarBasePublica(req));
    }

    @PostMapping("/relatos")
    public ResponseEntity<Evento> relato(@Valid @RequestBody EventoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarRelato(req));
    }

    @PutMapping("/eventos/{id}/resolver")
    public ResponseEntity<?> resolver(@RequestHeader(value = "X-API-KEY", required = false) String chave,
                                      @PathVariable Long id) {
        if (!apiKey.equals(chave)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "chave invalida"));
        }
        return ResponseEntity.ok(service.resolver(id));
    }
}
