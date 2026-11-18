import React from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { tempoAtras } from '../utils';

export default function BannerConexao({ conectado, reconectou, ultimaAtualizacao, onTentar }) {
  if (reconectou) {
    return (
      <View style={[styles.banner, { backgroundColor: '#1E7B34' }]}>
        <Text style={styles.texto}>✓ Conexão restabelecida</Text>
      </View>
    );
  }

  if (conectado === false) {
    return (
      <View style={[styles.banner, { backgroundColor: '#B00020' }]} accessibilityRole="alert">
        <View style={{ flex: 1 }}>
          <Text style={[styles.texto, { fontWeight: 'bold' }]}>Sem conexão com o servidor</Text>
          <Text style={styles.texto}>
            Mostrando os últimos dados ({tempoAtras(ultimaAtualizacao)}). Tentando de novo a cada 5s…
          </Text>
        </View>
        <TouchableOpacity style={styles.botao} onPress={onTentar} accessibilityLabel="Tentar reconectar">
          <Text style={{ color: '#B00020', fontWeight: 'bold' }}>Tentar</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return null;
}

const styles = StyleSheet.create({
  banner: { flexDirection: 'row', alignItems: 'center', padding: 12 },
  texto: { color: '#fff', fontSize: 14 },
  botao: { backgroundColor: '#fff', paddingHorizontal: 14, paddingVertical: 10, borderRadius: 8, marginLeft: 8 },
});
