package br.com.fiap.urbanrisk.service;

import br.com.fiap.urbanrisk.dto.EventoRequest;
import br.com.fiap.urbanrisk.model.TipoEvento;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Como nao temos sensores de verdade, essa classe gera leituras a cada 6 segundos
 * para alguns bairros de Sao Paulo. De vez em quando acontece um pico.
 */
@Component
@ConditionalOnProperty(name = "app.simulador.ativo", havingValue = "true")
public class SimuladorSensores {

    private final EventoService service;
    private final Random random = new Random();
    private final Map<String, Double> valores = new HashMap<>();
    private int rodadas = 0;

    public SimuladorSensores(EventoService service) {
        this.service = service;
    }

    @Scheduled(fixedRate = 6000, initialDelay = 3000)
    public void gerarLeituras() {
        rodadas++;
        enviar("SENSOR-IPIRANGA", TipoEvento.ALAGAMENTO, "Ipiranga", -23.5869, -46.6100, 8, 35);
        enviar("SENSOR-PINHEIROS", TipoEvento.ALAGAMENTO, "Pinheiros", -23.5670, -46.6950, 6, 30);
        enviar("SENSOR-ARICANDUVA", TipoEvento.ALAGAMENTO, "Aricanduva", -23.5780, -46.5110, 10, 40);
        enviar("TEMP-SE", TipoEvento.CALOR, "Se", -23.5505, -46.6333, 29, 8);
        enviar("TEMP-ITAQUERA", TipoEvento.CALOR, "Itaquera", -23.5400, -46.4560, 28, 8);
        enviar("ENERGIA-MOOCA", TipoEvento.FALTA_SERVICO, "Mooca", -23.5580, -46.5990, 0, 4000);

        // a cada 15 rodadas chega um boletim da Defesa Civil
        if (rodadas % 15 == 0) {
            double temp = 33 + random.nextDouble() * 6;
            service.registrarBasePublica(new EventoRequest(TipoEvento.CALOR, arredondar(temp), -23.5450, -46.4900,
                    "Zona Leste", "Defesa Civil SP", "Alerta de calor e baixa umidade. Evite sol entre 11h e 16h.", null));
        }
    }

    private void enviar(String id, TipoEvento tipo, String bairro, double lat, double lon, double base, double pico) {
        double atual = valores.getOrDefault(id, base);
        double novo;
        if (random.nextDouble() < 0.07) {
            novo = atual + pico;                                       // pico
        } else {
            novo = atual + (base - atual) * 0.2 + random.nextGaussian() * pico * 0.05; // volta pro normal
        }
        if (novo < 0) novo = 0;
        valores.put(id, novo);

        try {
            service.registrarSensor(new EventoRequest(tipo, arredondar(novo), lat, lon, bairro, id, null, null));
        } catch (Exception e) {
            System.out.println("Erro no simulador: " + e.getMessage());
        }
    }

    private double arredondar(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
