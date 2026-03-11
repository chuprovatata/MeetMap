const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.onNotificationCreated = functions.firestore
    .document('notifications/{notificationId}')
    .onCreate(async (snapshot, context) => {
        const notification = snapshot.data();
        
        // Получаем токен получателя
        const userDoc = await admin.firestore()
            .collection('users')
            .doc(notification.userId)
            .get();
        
        const fcmToken = userDoc.data()?.fcmToken;
        
        if (!fcmToken) {
            console.log('Нет FCM токена для пользователя', notification.userId);
            return null;
        }

        const message = {
            token: fcmToken,
            notification: {
                title: notification.title,
                body: notification.description,
            },
            data: {
                type: notification.type,
                ...notification.data,
            },
        };

        try {
            await admin.messaging().send(message);
            console.log('Пуш отправлен');
            await snapshot.ref.update({ pushSent: true });
        } catch (error) {
            console.error('Ошибка отправки:', error);
        }
    });