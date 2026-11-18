import React from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import CardEvento from '../componentes/CardEvento';
import { PRIORIDADES, TIPOS, tempoAtras } from '../utils';

export default function Painel({ resumo, alertas, ultimaAtualizacao, tempoResposta }) {
  const porPrioridade = resumo ? resumo.porPrioridade : {};
  const porTipo = resumo ? resumo.porTipo : {};
  const bairros = resumo ? Object.entries(resumo.porBairro) : [];

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.titulo}>Painel</Text>
      <Text style={styles.cinza}>
        Atualizado {tempoAtras(ultimaAtualizacao)}
        {tempoResposta ? ' · resposta em ' + tempoResposta + ' ms' : ''}
      </Text>

      <View style={styles.linha}>
        <Caixa numero={resumo ? resumo.total : '-'} texto="Ativos" />
        <Caixa numero={porPrioridade.CRITICA ?? '-'} texto="Críticos" cor={PRIORIDADES.CRITICA.cor} />
        <Caixa numero={porPrioridade.ALTA ?? '-'} texto="Altos" cor={PRIORIDADES.ALTA.cor} />
      </View>

      <View style={styles.linha}>
        {Object.keys(TIPOS).map((t) => (
          <Caixa key={t} numero={porTipo[t] ?? '-'} texto={TIPOS[t].icone + ' ' + TIPOS[t].nome} />
        ))}
      </View>

      {bairros.length > 0 && (
        <View style={styles.box}>
          <Text style={styles.subtitulo}>Alertas por bairro</Text>
          {bairros.map(([bairro, qtd]) => (
            <Text key={bairro} style={styles.bairro}>
              {bairro}: {qtd}
            </Text>
          ))}
        </View>
      )}

      <Text style={styles.subtitulo}>Mais urgentes</Text>
      {alertas.length === 0 && <Text style={styles.cinza}>Nenhum alerta ativo.</Text>}
      {alertas.slice(0, 5).map((e) => (
        <CardEvento key={e.id} evento={e} />
      ))}
    </ScrollView>
  );
}

function Caixa({ numero, texto, cor }) {
  return (
    <View style={styles.caixa} accessible accessibilityLabel={texto + ': ' + numero}>
      <Text style={[styles.numero, cor && { color: cor }]}>{numero}</Text>
      <Text style={styles.caixaTexto}>{texto}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { padding: 16 },
  titulo: { fontSize: 26, fontWeight: 'bold', color: '#222' },
  subtitulo: { fontSize: 18, fontWeight: 'bold', color: '#222', marginTop: 16, marginBottom: 8 },
  cinza: { color: '#555', marginBottom: 12 },
  linha: { flexDirection: 'row', marginBottom: 8 },
  caixa: {
    flex: 1,
    backgroundColor: '#fff',
    margin: 4,
    padding: 12,
    borderRadius: 8,
    alignItems: 'center',
    elevation: 2,
  },
  numero: { fontSize: 26, fontWeight: 'bold', color: '#222' },
  caixaTexto: { fontSize: 13, color: '#555', textAlign: 'center' },
  box: { backgroundColor: '#fff', borderRadius: 8, padding: 14, marginTop: 8, elevation: 2 },
  bairro: { fontSize: 15, color: '#222', marginTop: 4 },
});
