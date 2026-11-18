package br.com.fiap.urbanrisk.service;

import br.com.fiap.urbanrisk.model.Origem;
import br.com.fiap.urbanrisk.model.Prioridade;
import br.com.fiap.urbanrisk.model.TipoEvento;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Analise usada para priorizar os alertas.
 *
 * pontuacao = severidade * 50 + confianca * 30 + confirmacoes * 5 (max 15) + 10 se o valor subiu rapido
 *
 * A pontuacao vai de 0 a 100 e define a prioridade.
 */
@Service
public class AnaliseRiscoService {

    // transforma o valor medido em uma severidade de 0 a 1
    public double calcularSeveridade(TipoEvento tipo, double valor) {
        double s;
        if (tipo == TipoEvento.ALAGAMENTO) {
            s = valor / 50.0;                    // 50 cm de agua ou mais = maximo
        } else if (tipo == TipoEvento.CALOR) {
            s = (valor - 30.0) / 12.0;           // 30 graus = 0, 42 graus = maximo
        } else {
            s = Math.log10(valor + 1) / 4.0;     // pessoas afetadas: 10.000 = maximo
        }
        return limitar(s);
    }

    public double calcularConfianca(Origem origem, int confirmacoes) {
        double c;
        if (origem == Origem.SENSOR) {
            c = 0.85;
            if (confirmacoes > 0) c += 0.05;
        } else if (origem == Origem.BASE_PUBLICA) {
            c = 0.90;
        } else {
            // relato de morador comeca baixo e sobe quando outros confirmam
            c = 0.40 + 0.10 * confirmacoes;
            if (c > 0.80) c = 0.80;
        }
        return Math.round(c * 100) / 100.0;
    }

    public String explicarConfianca(Origem origem, int confirmacoes) {
        String texto;
        if (origem == Origem.SENSOR) {
            texto = "Leitura automatica de sensor";
        } else if (origem == Origem.BASE_PUBLICA) {
            texto = "Dado de orgao publico";
        } else {
            texto = "Relato de morador";
        }
        if (confirmacoes > 0) {
            texto += ", confirmado por mais " + confirmacoes + " fonte(s) no bairro";
        }
        return texto;
    }

    // compara o valor novo com a media das ultimas leituras do mesmo sensor
    public boolean subiuRapido(List<Double> ultimasLeituras, double valorAtual) {
        if (ultimasLeituras == null || ultimasLeituras.size() < 3) {
            return false;
        }
        double soma = 0;
        for (double v : ultimasLeituras) {
            soma += v;
        }
        double media = soma / ultimasLeituras.size();
        return valorAtual > media * 1.3 + 1;
    }

    public double calcularPontuacao(double severidade, double confianca, int confirmacoes, boolean subiuRapido) {
        double pontos = severidade * 50 + confianca * 30 + Math.min(confirmacoes, 3) * 5;
        if (subiuRapido) {
            pontos += 10;
        }
        if (pontos > 100) pontos = 100;
        return Math.round(pontos * 10) / 10.0;
    }

    public Prioridade definirPrioridade(double pontuacao) {
        if (pontuacao >= 70) return Prioridade.CRITICA;
        if (pontuacao >= 50) return Prioridade.ALTA;
        if (pontuacao >= 30) return Prioridade.MEDIA;
        return Prioridade.BAIXA;
    }

    private double limitar(double valor) {
        if (valor < 0) return 0;
        if (valor > 1) return 1;
        return valor;
    }
}
