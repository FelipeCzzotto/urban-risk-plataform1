import React, { useState } from 'react';
import { ScrollView, StyleSheet, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { enviarRelato } from '../api';
import { TIPOS } from '../utils';

const BAIRROS = [
  { nome: 'Se', lat: -23.5505, lon: -46.6333 },
  { nome: 'Mooca', lat: -23.558, lon: -46.599 },
  { nome: 'Ipiranga', lat: -23.5869, lon: -46.61 },
  { nome: 'Pinheiros', lat: -23.567, lon: -46.695 },
  { nome: 'Aricanduva', lat: -23.578, lon: -46.511 },
  { nome: 'Itaquera', lat: -23.54, lon: -46.456 },
];

export default function Relatar({ url, onEnviado }) {
  const [tipo, setTipo] = useState(null);
  const [intensidade, setIntensidade] = useState(3);
  const [bairro, setBairro] = useState(null);
  const [descricao, setDescricao] = useState('');
  const [contato, setContato] = useState('');
  const [mensagem, setMensagem] = useState('');
  const [enviando, setEnviando] = useState(false);

  async function enviar() {
    if (!tipo || !bairro) {
      setMensagem('Escolha o tipo e o bairro.');
      return;
    }
    setEnviando(true);
    try {
      await enviarRelato(url, {
        tipo: tipo,
        valor: intensidade,
        bairro: bairro.nome,
        latitude: bairro.lat,
        longitude: bairro.lon,
        descricao: descricao,
        contato: contato,
      });
      setMensagem('Relato enviado! Obrigado.');
      setTipo(null);
      setDescricao('');
      onEnviado();
    } catch (e) {
      setMensagem('Não foi possível enviar. Verifique a conexão e tente de novo.');
    }
    setEnviando(false);
  }

  return (
    <ScrollView contentContainerStyle={{ padding: 16 }}>
      <Text style={styles.titulo}>Relatar problema</Text>

      <Text style={styles.label}>O que está acontecendo?</Text>
      <View style={styles.linha}>
        {Object.keys(TIPOS).map((t) => (
          <Opcao key={t} texto={TIPOS[t].icone + '\n' + TIPOS[t].nome} ativo={tipo === t} onPress={() => setTipo(t)} />
        ))}
      </View>

      <Text style={styles.label}>Intensidade (1 = fraco, 5 = muito forte)</Text>
      <View style={styles.linha}>
        {[1, 2, 3, 4, 5].map((n) => (
          <Opcao key={n} texto={String(n)} ativo={intensidade === n} onPress={() => setIntensidade(n)} />
        ))}
      </View>

      <Text style={styles.label}>Bairro</Text>
      <View style={[styles.linha, { flexWrap: 'wrap' }]}>
        {BAIRROS.map((b) => (
          <Opcao key={b.nome} texto={b.nome} ativo={bairro?.nome === b.nome} onPress={() => setBairro(b)} />
        ))}
      </View>

      <Text style={styles.label}>Descrição (opcional)</Text>
      <TextInput
        style={[styles.input, { height: 80 }]}
        value={descricao}
        onChangeText={setDescricao}
        multiline
        placeholder="Ex: rua alagada perto do mercado"
      />

      <Text style={styles.label}>Contato (opcional)</Text>
      <TextInput style={styles.input} value={contato} onChangeText={setContato} autoCapitalize="none" />
      <Text style={styles.aviso}>
        Seu contato não é salvo. O servidor guarda apenas um código (hash) e remove telefone e e-mail da descrição.
      </Text>

      {mensagem ? <Text style={styles.mensagem}>{mensagem}</Text> : null}

      <TouchableOpacity style={styles.botao} onPress={enviar} disabled={enviando}>
        <Text style={styles.botaoTexto}>{enviando ? 'Enviando...' : 'Enviar relato'}</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

function Opcao({ texto, ativo, onPress }) {
  return (
    <TouchableOpacity
      style={[styles.opcao, ativo && styles.opcaoAtiva]}
      onPress={onPress}
      accessibilityState={{ selected: ativo }}
    >
      <Text style={{ color: ativo ? '#fff' : '#222', textAlign: 'center', fontSize: 15 }}>{texto}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  titulo: { fontSize: 26, fontWeight: 'bold', color: '#222' },
  label: { fontSize: 16, fontWeight: 'bold', color: '#222', marginTop: 18, marginBottom: 8 },
  linha: { flexDirection: 'row' },
  opcao: {
    flexGrow: 1,
    minHeight: 48,
    justifyContent: 'center',
    padding: 10,
    margin: 3,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#ccc',
    backgroundColor: '#fff',
  },
  opcaoAtiva: { backgroundColor: '#0B5CAD', borderColor: '#0B5CAD' },
  input: { backgroundColor: '#fff', borderWidth: 1, borderColor: '#ccc', borderRadius: 8, padding: 10, fontSize: 16 },
  aviso: { color: '#555', fontSize: 13, marginTop: 6 },
  mensagem: { marginTop: 14, fontSize: 15, fontWeight: 'bold', color: '#0B5CAD' },
  botao: { backgroundColor: '#0B5CAD', padding: 16, borderRadius: 8, alignItems: 'center', marginTop: 18 },
  botaoTexto: { color: '#fff', fontSize: 17, fontWeight: 'bold' },
});
