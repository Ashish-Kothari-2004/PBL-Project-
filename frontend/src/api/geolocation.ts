export function captureApproximateLocation(): Promise<{ approxLat: number; approxLng: number }> {
  if (!navigator.geolocation) {
    return Promise.reject(new Error('Geolocation is not supported by this browser.'))
  }

  return new Promise((resolve, reject) => {
    navigator.geolocation.getCurrentPosition(
      ({ coords }) =>
        resolve({
          approxLat: Math.round(coords.latitude * 100) / 100,
          approxLng: Math.round(coords.longitude * 100) / 100,
        }),
      () => reject(new Error('Location could not be captured. Check your browser permissions and try again.')),
      { enableHighAccuracy: false, timeout: 10000, maximumAge: 300000 },
    )
  })
}
