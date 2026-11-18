import React, { useState } from 'react';
import { FlatList, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import CardEvento from '../componentes/CardEvento';
import { TIPOS } from '../utils';

export default function Eventos({ alertas }) {
  const [filtro, setFiltro] = useState(null);

  const lista = filtro ? alertas.filter((e) => e.tipo === filtro) : alertas;

  return (
    <View style={{ flex: 1, padding: 16 }}>
      <Text style={styles.titulo}>Eventos</Text>

      <View style={styles.filtros}>
        <Filtro texto="Todos" ativo={filtro === null} onPress={() => setFiltro(null)} />
        {Object.keys(TIPOS).map((t) => (
          <Filtro key={t} texto={TIPOS[t].icone + ' ' + TIPOS[t].nome} ativo={filtro === t} onPress={() => setFiltro(t)} />
        ))}
      </View>

      <Text style={styles.cinza}>{lista.length} evento(s), do mais urgente para o menos urgente</Text>

      <FlatList
        data={lista}
        keyExtractor={(item) => String(item.id)}
        renderItem={({ item }) => <CardEvento evento={item} />}
        ListEmptyComponent={<Text style={styles.cinza}>Nenhum evento.</Text>}
      />
    </View>
  );
}

function Filtro({ texto, ativo, onPress }) {
  return (
    <TouchableOpacity
      style={[styles.filtro, ativo && styles.filtroAtivo]}
      onPress={onPress}
      accessibilityState={{ selected: ativo }}
    >
      <Text style={{ color: ativo ? '#fff' : '#222' }}>{texto}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  titulo: { fontSize: 26, fontWeight: 'bold', color: '#222', marginBottom: 10 },
  filtros: { flexDirection: 'row', flexWrap: 'wrap', marginBottom: 8 },
  filtro: {
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: '#ccc',
    backgroundColor: '#fff',
    marginRight: 6,
    marginBottom: 6,
  },
  filtroAtivo: { backgroundColor: '#0B5CAD', borderColor: '#0B5CAD' },
  cinza: { color: '#555', marginBottom: 8 },
});
