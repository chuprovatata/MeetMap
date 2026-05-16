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
const PLACES_OF_DAY_COUNT = 5;
const DAYS_BETWEEN_REPEATS = 5;

async function updatePlacesOfDay() {
  console.log('Обновляем подборку "Места дня"...');

  const placesSnapshot = await db.collection('places_info').get();
  const allPlaces = placesSnapshot.docs;

  if (allPlaces.length === 0) {
    console.log('Нет мест в базе данных');
    return;
  }

  console.log(`Найдено мест: ${allPlaces.length}`);

  const now = admin.firestore.Timestamp.now();
  const fiveDaysAgo = new Date();
  fiveDaysAgo.setDate(fiveDaysAgo.getDate() - DAYS_BETWEEN_REPEATS);

  const sortedPlaces = allPlaces
    .filter(doc => doc && doc.id)
    .map(doc => ({
      doc: doc,
      data: doc.data(),
      priority: calculatePriority(doc.data(), fiveDaysAgo)
    }))
    .filter(item => item.doc !== undefined);

  if (sortedPlaces.length === 0) {
    console.log(' Нет корректных мест для выбора');
    return;
  }

  sortedPlaces.sort((a, b) => b.priority - a.priority);

  const selectedPlaces = sortedPlaces.slice(0, PLACES_OF_DAY_COUNT);

  console.log(`Выбрано ${selectedPlaces.length} мест дня:`);

  let batch = db.batch();
  let count = 0;
  let batchCount = 0;

  for (const { doc } of allPlaces) {
    if (!doc || !doc.id) continue;

    const isSelected = selectedPlaces.some(sp => sp.doc && sp.doc.id === doc.id);

    if (isSelected) {
      batch.update(doc.ref, {
        place_ofday: true,
        lastTimeInDigest: now
      });
      const placeName = doc.data()?.name || 'Без названия';
      console.log(`   ✅ ${placeName}`);
    } else {
      batch.update(doc.ref, { place_ofday: false });
    }

    count++;
    batchCount++;

    if (batchCount >= 500) {
      await batch.commit();
      console.log(`Обработано ${count} мест`);
      batch = db.batch();
      batchCount = 0;
    }
  }

  if (batchCount > 0) {
    await batch.commit();
  }

  console.log('Готово! Места дня обновлены.');
}

function calculatePriority(placeData, fiveDaysAgo) {
  let priority = 0;

  if (!placeData) return priority;

  const lastTimeInDigest = placeData.lastTimeInDigest?.toDate();
  if (lastTimeInDigest && lastTimeInDigest > fiveDaysAgo) {
    return -100;
  }

  const createdAt = placeData.createdAt?.toDate();
  if (createdAt) {
    const daysSinceCreated = (Date.now() - createdAt) / (1000 * 60 * 60 * 24);
    if (daysSinceCreated <= 7) {
      priority += 100;
    } else if (daysSinceCreated <= 30) {
      priority += 30;
    }
  }

  const likesCount = placeData.likesCount || 0;
  if (likesCount === 0) {
    priority += 50;
  } else if (likesCount < 5) {
    priority += 20;
  }

  if (placeData.rarity === 'unique') priority += 40;
  else if (placeData.rarity === 'epic') priority += 20;
  else if (placeData.rarity === 'rare') priority += 10;

  priority += Math.random() * 10;

  return priority;
}

updatePlacesOfDay().catch(console.error);