import React, { useState } from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { ORIGENS, PRIORIDADES, TIPOS, nivelConfianca, tempoAtras } from '../utils';

// card de um evento. Ao tocar, mostra os detalhes
export default function CardEvento({ evento }) {
  const [aberto, setAberto] = useState(false);
  const prioridade = PRIORIDADES[evento.prioridade] || PRIORIDADES.BAIXA;
  const tipo = TIPOS[evento.tipo] || { nome: evento.tipo, icone: '❗' };

  return (
    <TouchableOpacity
      style={[styles.card, { borderLeftColor: prioridade.cor }]}
      onPress={() => setAberto(!aberto)}
      accessibilityLabel={`${tipo.nome} em ${evento.bairro}, prioridade ${prioridade.nome}. Toque para ver detalhes`}
    >
      <View style={styles.linha}>
        <Text style={styles.icone}>{tipo.icone}</Text>
        <View style={{ flex: 1 }}>
          <Text style={styles.titulo}>{evento.bairro}</Text>
          <Text style={styles.cinza}>{tipo.nome}</Text>
        </View>
        <View style={[styles.etiqueta, { backgroundColor: prioridade.cor }]}>
          <Text style={styles.etiquetaTexto}>{prioridade.nome}</Text>
        </View>
      </View>

      {evento.descricao ? <Text style={styles.descricao}>{evento.descricao}</Text> : null}

      <Text style={styles.cinza}>
        {ORIGENS[evento.origem]} · Confiança {nivelConfianca(evento.confianca)} · {tempoAtras(evento.atualizadoEm)}
      </Text>

      {aberto && (
        <View style={styles.detalhes}>
          <Text style={styles.detalhe}>Fonte: {evento.fonte}</Text>
          <Text style={styles.detalhe}>Confiança: {Math.round(evento.confianca * 100)}% ({evento.motivoConfianca})</Text>
          <Text style={styles.detalhe}>Pontuação de risco: {evento.pontuacao} / 100</Text>
          <Text style={styles.detalhe}>Confirmações no bairro: {evento.confirmacoes}</Text>
          <Text style={styles.detalhe}>Local: {evento.latitude}, {evento.longitude}</Text>
        </View>
      )}
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#fff',
    borderRadius: 8,
    padding: 14,
    marginBottom: 10,
    borderLeftWidth: 6,
    elevation: 2,
  },
  linha: { flexDirection: 'row', alignItems: 'center' },
  icone: { fontSize: 26, marginRight: 10 },
  titulo: { fontSize: 17, fontWeight: 'bold', color: '#222' },
  cinza: { fontSize: 13, color: '#555', marginTop: 4 },
  descricao: { fontSize: 15, color: '#222', marginTop: 8 },
  etiqueta: { paddingHorizontal: 8, paddingVertical: 4, borderRadius: 6 },
  etiquetaTexto: { color: '#fff', fontWeight: 'bold', fontSize: 12 },
  detalhes: { marginTop: 10, paddingTop: 10, borderTopWidth: 1, borderTopColor: '#ddd' },
  detalhe: { fontSize: 14, color: '#333', marginBottom: 4 },
});
