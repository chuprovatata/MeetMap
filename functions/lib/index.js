"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.dailyDigestNotification = void 0;
const scheduler_1 = require("firebase-functions/v2/scheduler");
const admin = __importStar(require("firebase-admin"));
admin.initializeApp();
// V2 синтаксис для scheduled functions
exports.dailyDigestNotification = (0, scheduler_1.onSchedule)({
    schedule: '0 10 * * *',
    timeZone: 'Europe/Moscow',
}, async (event) => {
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
        const response = await admin.messaging().sendEachForMulticast(Object.assign(Object.assign({}, message), { tokens: chunk }));
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
});
//# sourceMappingURL=index.js.map