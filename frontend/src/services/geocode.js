const cache = new Map();

function buildLabel(data) {
  const addr = data?.address || {};
  const street = addr.road || addr.pedestrian || addr.street || '';
  const number = addr.house_number || '';
  const locality = addr.suburb || addr.neighbourhood || addr.city_district || addr.village || addr.town || addr.city || '';

  let label = [street, number].filter(Boolean).join(' ').trim();
  if (!label) label = data?.display_name?.split(',').slice(0, 2).join(',').trim() || '';
  if (locality && !label.includes(locality)) label = label ? `${label}, ${locality}` : locality;
  return label;
}

export async function reverseGeocode(lat, lng) {
  const key = `${Number(lat).toFixed(5)},${Number(lng).toFixed(5)}`;
  if (cache.has(key)) return cache.get(key);

  const fallback = `${Number(lat).toFixed(5)}, ${Number(lng).toFixed(5)}`;
  try {
    const res = await fetch(
      `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lng}&addressdetails=1`,
      { headers: { 'Accept-Language': 'es' } }
    );
    const data = await res.json();
    const label = buildLabel(data) || fallback;
    cache.set(key, label);
    return label;
  } catch (err) {
    console.error('Error en geocodificación inversa', err);
    return fallback;
  }
}
