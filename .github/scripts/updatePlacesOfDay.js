const admin = require('firebase-admin');

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

async function updatePlacesOfDay() {
  console.log('Обновляем подборку Места дня...');

  const placesSnapshot = await db.collection('places_info').get();
  const allPlaces = placesSnapshot.docs;

  console.log(`Найдено мест: ${allPlaces.length}`);

  console.log('Сбрасываем старую подборку...');

  for (const doc of allPlaces) {
    await doc.ref.update({ place_ofday: false });
  }

  console.log('Старая подборка сброшена');

  const shuffled = [...allPlaces];
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
  }

  const selectedPlaces = shuffled.slice(0, 5);

  console.log('Новые места дня:');
  for (const doc of selectedPlaces) {
    const placeName = doc.data().name;
    console.log(`   ${placeName}`);
    await doc.ref.update({
      place_ofday: true,
      lastTimeInDigest: admin.firestore.Timestamp.now()
    });
  }

  console.log('Готово');
}

updatePlacesOfDay().catch(console.error);