const admin = require('firebase-admin');
const axios = require('axios');

// Инициализация Firebase Admin SDK
if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert({
      projectId: process.env.FIREBASE_PROJECT_ID,
      clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
      privateKey: process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n')
    })
  });
}

const db = admin.firestore();
const ONE_SIGNAL_API = 'https://onesignal.com/api/v1/notifications';

// Типы уведомлений, для которых нужно отправлять пуши
const PUSH_TYPES = [
  'FRIEND_REQUEST',
  'FRIEND_ACCEPTED',
  'PLACE_LIKED',
  'NEW_PLACE_FROM_FRIEND'
];

async function processPersonalizedPushes() {
  console.log('🔄 Проверяем новые уведомления для отправки пушей...');

  // Ищем непрочитанные уведомления нужных типов, где пуш ещё не отправлен
  const snapshot = await db.collection('notifications')
    .where('pushSent', '==', false)
    .where('type', 'in', PUSH_TYPES)
    .limit(50)
    .get();

  if (snapshot.empty) {
    console.log('📭 Нет новых уведомлений для отправки');
    return;
  }

  console.log(`📦 Найдено ${snapshot.size} уведомлений для отправки`);

  for (const doc of snapshot.docs) {
    const notification = doc.data();

    try {
      // Получаем playerId пользователя
      const userDoc = await db.collection('users')
        .doc(notification.userId)
        .get();

      const playerId = userDoc.get('oneSignalPlayerId');

      if (playerId && playerId.length > 0) {
        // Отправляем пуш конкретному пользователю
        const response = await axios.post(ONE_SIGNAL_API, {
          app_id: process.env.ONESIGNAL_APP_ID,
          include_player_ids: [playerId],
          headings: { en: notification.title, ru: notification.title },
          contents: { en: notification.description, ru: notification.description },
          data: {
            type: notification.type,
            notificationId: doc.id,
            ...notification.data
          }
        }, {
          headers: {
            'Authorization': `Basic ${process.env.ONESIGNAL_API_KEY}`,
            'Content-Type': 'application/json'
          }
        });

        // Отмечаем, что пуш отправлен
        await doc.ref.update({
          pushSent: true,
          pushSentAt: admin.firestore.FieldValue.serverTimestamp(),
          pushResponseId: response.data.id
        });

        console.log(`✅ Пуш отправлен: ${notification.type} для пользователя ${notification.userId}`);
      } else {
        console.log(`⚠️ Нет playerId для пользователя ${notification.userId}`);
        // Отмечаем как безнадёжное (чоб не проверять снова)
        await doc.ref.update({
          pushSent: true,
          pushError: 'No playerId'
        });
      }

    } catch (error) {
      console.error(`❌ Ошибка отправки пуша: ${error.message}`);
      // Можно повторить позже, не помечаем как отправленное
    }

    // Небольшая задержка
    await new Promise(resolve => setTimeout(resolve, 100));
  }

  console.log('🎉 Обработка завершена');
}

processPersonalizedPushes().catch(console.error);