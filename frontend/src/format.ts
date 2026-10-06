export function formatPrice(price: number): string {
  return `$${price.toFixed(2)}`
}

// Hourly slots, matching the backend's opening hours (first slot 09:00, last 16:00).
export const TIME_SLOTS = Array.from({ length: 8 }, (_, i) => `${String(9 + i).padStart(2, '0')}:00`)
