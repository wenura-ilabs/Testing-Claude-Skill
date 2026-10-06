export interface Service {
  id: number
  name: string
  durationMinutes: number
  price: number
}

export interface Booking {
  id: number
  serviceId: number
  serviceName: string
  durationMinutes: number
  date: string
  time: string
  customerName: string
  customerEmail: string
  totalPrice: number
  status: 'CONFIRMED' | 'CANCELLED'
}

export interface CreateBookingRequest {
  serviceId: number
  date: string
  time: string
  customerName: string
  customerEmail: string
}

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly fieldErrors: Record<string, string> = {},
  ) {
    super(message)
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response
  try {
    response = await fetch(path, init)
  } catch {
    throw new ApiError('Could not reach the server. Is the backend running?', 0)
  }
  const body = await response.json().catch(() => null)
  if (!response.ok) {
    throw new ApiError(
      body?.message ?? `Request failed (${response.status})`,
      response.status,
      body?.fieldErrors ?? {},
    )
  }
  return body as T
}

export function listServices(): Promise<Service[]> {
  return request('/api/services')
}

export function createBooking(booking: CreateBookingRequest): Promise<Booking> {
  return request('/api/bookings', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(booking),
  })
}

export function getBooking(id: number): Promise<Booking> {
  return request(`/api/bookings/${id}`)
}

export function cancelBooking(id: number): Promise<Booking> {
  return request(`/api/bookings/${id}/cancel`, { method: 'POST' })
}
