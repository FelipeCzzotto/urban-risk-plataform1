import { Platform } from 'react-native';
import * as Notifications from 'expo-notifications';

let permitido = false;

export async function configurarNotificacoes() {
  try {
    Notifications.setNotificationHandler({
      handleNotification: async () => ({
        shouldShowAlert: true,
        shouldShowBanner: true,
        shouldShowList: true,
        shouldPlaySound: true,
        shouldSetBadge: false,
      }),
    });
    if (Platform.OS === 'android') {
      await Notifications.setNotificationChannelAsync('alertas', {
        name: 'Alertas',
        importance: Notifications.AndroidImportance.HIGH,
      });
    }
    const { status } = await Notifications.requestPermissionsAsync();
    permitido = status === 'granted';
  } catch (e) {
    console.log('Notificacoes nao disponiveis:', e.message);
  }
}

export async function notificar(titulo, texto) {
  if (!permitido) return;
  try {
    await Notifications.scheduleNotificationAsync({
      content: { title: titulo, body: texto, sound: true },
      trigger: null,
    });
  } catch (e) {
    console.log('Erro ao notificar:', e.message);
  }
}
