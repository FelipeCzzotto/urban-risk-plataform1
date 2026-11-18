import React, { useEffect, useRef, useState } from 'react';
import { StyleSheet, Text, TouchableOpacity, Vibration, View } from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { buscarAlertas, buscarResumo } from './src/api';
import { configurarNotificacoes, notificar } from './src/notificacao';
import { PRIORIDADES, TIPOS } from './src/utils';
import BannerConexao from './src/componentes/BannerConexao';
import Painel from './src/telas/Painel';
import Eventos from './src/telas/Eventos';
import Relatar from './src/telas/Relatar';
import Config from './src/telas/Config';

const URL_PADRAO = process.env.EXPO_PUBLIC_API_URL || 'http://localhost:8080';
const INTERVALO = 5000; // atualiza a cada 5 segundos

export default function App() {
  const [aba, setAba] = useState('painel');
  const [url, setUrl] = useState(URL_PADRAO);
  const [alertas, setAlertas] = useState([]);
  const [resumo, setResumo] = useState(null);
  const [conectado, setConectado] = useState(null);
  const [reconectou, setReconectou] = useState(false);
  const [ultimaAtualizacao, setUltimaAtualizacao] = useState(null);
  const [tempoResposta, setTempoResposta] = useState(null);

  const estavaOffline = useRef(false);
  const alertasConhecidos = useRef(null);

  useEffect(() => {
    configurarNotificacoes();
  }, []);

  async function atualizar() {
    const inicio = Date.now();
    try {
      const [listaAlertas, dadosResumo] = await Promise.all([buscarAlertas(url), buscarResumo(url)]);
      setTempoResposta(Date.now() - inicio);
      setAlertas(listaAlertas);
      setResumo(dadosResumo);
      setUltimaAtualizacao(new Date());
      setConectado(true);
      verificarNovosAlertas(listaAlertas);

      if (estavaOffline.current) {
        estavaOffline.current = false;
        setReconectou(true);
        setTimeout(() => setReconectou(false), 3000);
      }
    } catch (e) {
      // servidor fora do ar: mantem os ultimos dados na tela e tenta de novo depois
      setConectado(false);
      estavaOffline.current = true;
    }
  }

  // avisa quando aparece alerta novo de prioridade ALTA ou CRITICA
  function verificarNovosAlertas(lista) {
    if (alertasConhecidos.current === null) {
      alertasConhecidos.current = lista.map((a) => a.id + a.prioridade);
      return;
    }
    const novos = lista.filter(
      (a) => (a.prioridade === 'ALTA' || a.prioridade === 'CRITICA') &&
        !alertasConhecidos.current.includes(a.id + a.prioridade),
    );
    alertasConhecidos.current = lista.map((a) => a.id + a.prioridade);

    if (novos.length > 0) {
      const a = novos[0];
      Vibration.vibrate(500);
      notificar(
        'Alerta ' + PRIORIDADES[a.prioridade].nome + ': ' + TIPOS[a.tipo].nome + ' em ' + a.bairro,
        a.descricao || 'Abra o app para ver os detalhes',
      );
    }
  }

  useEffect(() => {
    atualizar();
    const timer = setInterval(atualizar, INTERVALO);
    return () => clearInterval(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [url]);

  return (
    <View style={styles.app}>
      <StatusBar style="dark" />
      <BannerConexao
        conectado={conectado}
        reconectou={reconectou}
        ultimaAtualizacao={ultimaAtualizacao}
        onTentar={atualizar}
      />

      <View style={{ flex: 1 }}>
        {aba === 'painel' && (
          <Painel resumo={resumo} alertas={alertas} ultimaAtualizacao={ultimaAtualizacao} tempoResposta={tempoResposta} />
        )}
        {aba === 'eventos' && <Eventos alertas={alertas} />}
        {aba === 'relatar' && <Relatar url={url} onEnviado={atualizar} />}
        {aba === 'config' && <Config url={url} setUrl={setUrl} conectado={conectado} />}
      </View>

      <View style={styles.menu}>
        <BotaoMenu texto="📊 Painel" ativo={aba === 'painel'} onPress={() => setAba('painel')} />
        <BotaoMenu texto="📋 Eventos" ativo={aba === 'eventos'} onPress={() => setAba('eventos')} />
        <BotaoMenu texto="📣 Relatar" ativo={aba === 'relatar'} onPress={() => setAba('relatar')} />
        <BotaoMenu texto="⚙️ Config" ativo={aba === 'config'} onPress={() => setAba('config')} />
      </View>
    </View>
  );
}

function BotaoMenu({ texto, ativo, onPress }) {
  return (
    <TouchableOpacity style={styles.botaoMenu} onPress={onPress} accessibilityRole="tab" accessibilityState={{ selected: ativo }}>
      <Text style={[styles.textoMenu, ativo && { color: '#0B5CAD', fontWeight: 'bold' }]}>{texto}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  app: { flex: 1, backgroundColor: '#F2F4F7', paddingTop: 40 },
  menu: { flexDirection: 'row', backgroundColor: '#fff', borderTopWidth: 1, borderTopColor: '#ddd', paddingBottom: 20 },
  botaoMenu: { flex: 1, alignItems: 'center', paddingVertical: 12 },
  textoMenu: { fontSize: 13, color: '#555' },
});
