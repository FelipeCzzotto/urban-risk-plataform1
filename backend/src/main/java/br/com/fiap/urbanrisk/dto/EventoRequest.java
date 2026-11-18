package br.com.fiap.urbanrisk.dto;

import br.com.fiap.urbanrisk.model.TipoEvento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Mesmo formato para sensor, base publica e relato.
 * No relato, o campo valor e a intensidade de 1 a 5.
 */
public record EventoRequest(
        @NotNull TipoEvento tipo,
        @NotNull Double valor,
        @NotNull Double latitude,
        @NotNull Double longitude,
        @NotBlank String bairro,
        String fonte,
        @Size(max = 300) String descricao,
        String contato
) {
}
