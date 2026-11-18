package br.com.fiap.urbanrisk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoEvento tipo;

    @Enumerated(EnumType.STRING)
    private Origem origem;

    // nome do sensor ou do orgao que mandou o dado
    private String fonte;

    private String descricao;
    private String bairro;
    private Double latitude;
    private Double longitude;
    private Double valor;

    private double severidade;
    private double confianca;
    private String motivoConfianca;
    private double pontuacao;

    @Enumerated(EnumType.STRING)
    private Prioridade prioridade;

    private int confirmacoes;
    private boolean ativo = true;

    // LGPD: guardamos so o hash do contato, nunca o contato
    @JsonIgnore
    private String contatoHash;

    private Instant criadoEm;
    private Instant atualizadoEm;

    @PrePersist
    public void antesDeSalvar() {
        criadoEm = Instant.now();
        atualizadoEm = criadoEm;
    }

    @PreUpdate
    public void antesDeAtualizar() {
        atualizadoEm = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoEvento getTipo() { return tipo; }
    public void setTipo(TipoEvento tipo) { this.tipo = tipo; }
    public Origem getOrigem() { return origem; }
    public void setOrigem(Origem origem) { this.origem = origem; }
    public String getFonte() { return fonte; }
    public void setFonte(String fonte) { this.fonte = fonte; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }
    public double getSeveridade() { return severidade; }
    public void setSeveridade(double severidade) { this.severidade = severidade; }
    public double getConfianca() { return confianca; }
    public void setConfianca(double confianca) { this.confianca = confianca; }
    public String getMotivoConfianca() { return motivoConfianca; }
    public void setMotivoConfianca(String motivoConfianca) { this.motivoConfianca = motivoConfianca; }
    public double getPontuacao() { return pontuacao; }
    public void setPontuacao(double pontuacao) { this.pontuacao = pontuacao; }
    public Prioridade getPrioridade() { return prioridade; }
    public void setPrioridade(Prioridade prioridade) { this.prioridade = prioridade; }
    public int getConfirmacoes() { return confirmacoes; }
    public void setConfirmacoes(int confirmacoes) { this.confirmacoes = confirmacoes; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public String getContatoHash() { return contatoHash; }
    public void setContatoHash(String contatoHash) { this.contatoHash = contatoHash; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
