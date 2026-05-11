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

const NOTIFICATION_CONFIG = {
  PLACES_OF_DAY_UPDATED: {
    push: {
      headings: { en: 'Свежие места дня 🔥', ru: 'Свежие места дня 🔥' },
      contents: {
        en: 'Подборка мест дня обновилась! Смотри, что нового мы для тебя нашли.',
        ru: 'Подборка мест дня обновилась! Смотри, что нового мы для тебя нашли.'
      },
      data: { type: 'PLACES_OF_DAY_UPDATED' }
    },
    internal: {
      title: 'Свежие места дня 🔥',
      description: 'Подборка мест дня обновилась! Смотри, что нового мы для тебя нашли.',
      type: 'PLACES_OF_DAY_UPDATED',
      buttonText: 'Смотреть подборку',
      data: {}
    }
  }
};

async function sendPlacesOfDayNotification() {
  const config = NOTIFICATION_CONFIG.PLACES_OF_DAY_UPDATED;

  console.log('Отправляем уведомление о новых местах дня...');

  const usersSnapshot = await db.collection('users').get();
  const allUsers = usersSnapshot.docs;
  console.log(`Найдено пользователей: ${allUsers.length}`);

  await sendPushNotification(config);

  await createInternalNotifications(config, allUsers);

  console.log('Готово!');
}

async function sendPushNotification(config) {
  console.log('Отправляем пуш через OneSignal...');

  try {
    const response = await axios.post(ONE_SIGNAL_API, {
      app_id: process.env.ONESIGNAL_APP_ID,
      included_segments: ['Total Subscriptions'],
      headings: config.push.headings,
      contents: config.push.contents,
      data: {
        ...config.push.data,
        timestamp: new Date().toISOString()
      }
    }, {
      headers: {
        'Authorization': `Basic ${process.env.ONESIGNAL_API_KEY}`,
        'Content-Type': 'application/json'
      }
    });

    console.log(`Пуш отправлен: ${response.data.recipients || 'всем'} получателям`);
  } catch (error) {
    console.error(`Ошибка отправки пуша: ${error.message}`);
  }
}

async function createInternalNotifications(config, users) {
  console.log(`Создаём внутренние уведомления для ${users.length} пользователей...`);

  let count = 0;
  let batch = db.batch();
  let batchCount = 0;

  for (const userDoc of users) {
    const userId = userDoc.id;
    const notificationRef = db.collection('notifications').doc();

    const notification = {
      id: notificationRef.id,
      userId: userId,
      type: config.internal.type,
      title: config.internal.title,
      description: config.internal.description,
      data: config.internal.data,
      buttonText: config.internal.buttonText,
      read: false,
      pushSent: true,
      pushSentAt: admin.firestore.FieldValue.serverTimestamp(),
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      expiresAt: null
    };

    batch.set(notificationRef, notification);
    count++;
    batchCount++;

    if (batchCount >= 500) {
      await batch.commit();
      console.log(`Сохранено ${count} уведомлений`);
      batch = db.batch();
      batchCount = 0;
    }
  }

  if (batchCount > 0) {
    await batch.commit();
  }

  console.log(`Создано ${count} внутренних уведомлений`);
}

sendPlacesOfDayNotification().catch(console.error);