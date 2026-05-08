import {onSchedule} from 'firebase-functions/v2/scheduler';
import * as admin from 'firebase-admin';

admin.initializeApp();

// V2 синтаксис для scheduled functions
export const dailyDigestNotification = onSchedule(
    {
        schedule: '0 10 * * *',
        timeZone: 'Europe/Moscow',
    },
    async (event) => {

        console.log('Запуск ежедневной рассылки уведомлений');

        const tokensSnapshot = await admin.firestore()
            .collection('fcm_tokens')
            .get();

        const tokens = tokensSnapshot.docs.map(doc => doc.data().token);

        console.log(`📱 Найдено токенов: ${tokens.length}`);

        if (tokens.length === 0) {
            console.log('Нет токенов для отправки');
            return;
        }

        const message = {
            notification: {
                title: 'Новая подборка!',
                body: 'Загляни, мы тут кое-что для тебя собрали'
            },
            data: {
                type: 'PLACES_OF_DAY_UPDATED',
                buttonText: 'Смотреть подборку',
                screen: 'places_of_day'
            },
            tokens: tokens,
        };

        const chunkSize = 500;
        let successCount = 0;
        let failureCount = 0;

        for (let i = 0; i < tokens.length; i += chunkSize) {
            const chunk = tokens.slice(i, i + chunkSize);
            const response = await admin.messaging().sendEachForMulticast({
                ...message,
                tokens: chunk
            });

            successCount += response.successCount;
            failureCount += response.failureCount;

            console.log(`📨 Отправлено: ${response.successCount}, ошибок: ${response.failureCount}`);

            response.responses.forEach((resp, idx) => {
                if (!resp.success) {
                    const failedToken = chunk[idx];
                    admin.firestore()
                        .collection('fcm_tokens')
                        .doc(failedToken)
                        .delete()
                        .catch(console.error);
                }
            });
        }

        console.log(`Рассылка завершена. Успешно: ${successCount}, ошибок: ${failureCount}`);
    }
);