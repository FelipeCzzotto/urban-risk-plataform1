package br.com.fiap.urbanrisk.repository;

import br.com.fiap.urbanrisk.model.Evento;
import br.com.fiap.urbanrisk.model.TipoEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    List<Evento> findByAtivoTrueOrderByPontuacaoDesc();

    Optional<Evento> findFirstByFonteAndAtivoTrue(String fonte);

    // usado para achar outros eventos parecidos no mesmo bairro (confirmacoes)
    List<Evento> findByTipoAndBairroAndAtivoTrueAndAtualizadoEmAfter(TipoEvento tipo, String bairro, Instant depoisDe);
}
