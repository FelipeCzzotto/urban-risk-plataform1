import React, { useState } from 'react';
import { StyleSheet, Text, TextInput, TouchableOpacity, View } from 'react-native';

export default function Config({ url, setUrl, conectado }) {
  const [texto, setTexto] = useState(url);

  return (
    <View style={{ padding: 16 }}>
      <Text style={styles.titulo}>Configurações</Text>

      <Text style={styles.label}>Endereço do servidor</Text>
      <TextInput
        style={styles.input}
        value={texto}
        onChangeText={setTexto}
        autoCapitalize="none"
        autoCorrect={false}
      />
      <Text style={styles.cinza}>
        Exemplo: http://192.168.0.10:8080 (IP do computador) ou o endereço da porta 8080 do Codespaces.
      </Text>

      <TouchableOpacity style={styles.botao} onPress={() => setUrl(texto.trim().replace(/\/$/, ''))}>
        <Text style={styles.botaoTexto}>Salvar</Text>
      </TouchableOpacity>

      <Text style={styles.status}>
        Status: {conectado === null ? 'conectando...' : conectado ? 'conectado ✓' : 'desconectado ✕'}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  titulo: { fontSize: 26, fontWeight: 'bold', color: '#222' },
  label: { fontSize: 16, fontWeight: 'bold', color: '#222', marginTop: 18, marginBottom: 8 },
  input: { backgroundColor: '#fff', borderWidth: 1, borderColor: '#ccc', borderRadius: 8, padding: 10, fontSize: 16 },
  cinza: { color: '#555', marginTop: 6 },
  botao: { backgroundColor: '#0B5CAD', padding: 14, borderRadius: 8, alignItems: 'center', marginTop: 14 },
  botaoTexto: { color: '#fff', fontSize: 16, fontWeight: 'bold' },
  status: { marginTop: 20, fontSize: 16, color: '#222' },
});
