import type { Booking, Service } from './api'

export const services: Service[] = [
  { id: 1, name: 'Haircut', durationMinutes: 30, price: 25 },
  { id: 2, name: 'Massage', durationMinutes: 60, price: 60 },
]

export const booking: Booking = {
  id: 42,
  serviceId: 1,
  serviceName: 'Haircut',
  durationMinutes: 30,
  date: '2030-01-15',
  time: '10:00',
  customerName: 'Ada Lovelace',
  customerEmail: 'ada@example.com',
  totalPrice: 25,
  status: 'CONFIRMED',
}
