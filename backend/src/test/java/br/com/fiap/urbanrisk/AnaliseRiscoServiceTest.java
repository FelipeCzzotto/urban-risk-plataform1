package br.com.fiap.urbanrisk;

import br.com.fiap.urbanrisk.model.Origem;
import br.com.fiap.urbanrisk.model.Prioridade;
import br.com.fiap.urbanrisk.model.TipoEvento;
import br.com.fiap.urbanrisk.service.AnaliseRiscoService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnaliseRiscoServiceTest {

    private final AnaliseRiscoService analise = new AnaliseRiscoService();

    @Test
    void deveCalcularSeveridadeDoAlagamento() {
        assertEquals(0.5, analise.calcularSeveridade(TipoEvento.ALAGAMENTO, 25));
        assertEquals(1.0, analise.calcularSeveridade(TipoEvento.ALAGAMENTO, 80));
    }

    @Test
    void temperaturaNormalNaoTemSeveridade() {
        assertEquals(0.0, analise.calcularSeveridade(TipoEvento.CALOR, 25));
    }

    @Test
    void sensorTemMaisConfiancaQueRelatoSozinho() {
        double sensor = analise.calcularConfianca(Origem.SENSOR, 0);
        double relato = analise.calcularConfianca(Origem.COMUNIDADE, 0);
        assertTrue(sensor > relato);
    }

    @Test
    void relatoConfirmadoGanhaConfianca() {
        assertEquals(0.40, analise.calcularConfianca(Origem.COMUNIDADE, 0));
        assertEquals(0.60, analise.calcularConfianca(Origem.COMUNIDADE, 2));
        assertEquals(0.80, analise.calcularConfianca(Origem.COMUNIDADE, 10));
    }

    @Test
    void deveDetectarSubidaRapida() {
        assertTrue(analise.subiuRapido(List.of(10.0, 11.0, 9.0), 30));
        assertFalse(analise.subiuRapido(List.of(10.0, 11.0, 9.0), 11));
        assertFalse(analise.subiuRapido(List.of(10.0), 30));
    }

    @Test
    void alagamentoForteDeSensorDeveSerCritico() {
        double pontos = analise.calcularPontuacao(1.0, 0.85, 1, true);
        assertEquals(Prioridade.CRITICA, analise.definirPrioridade(pontos));
    }

    @Test
    void relatoFracoDeveSerBaixo() {
        double pontos = analise.calcularPontuacao(0.2, 0.4, 0, false);
        assertEquals(Prioridade.BAIXA, analise.definirPrioridade(pontos));
    }
}
