import { afterEach, describe, expect, it, vi } from 'vitest'
import { ApiError, cancelBooking, getBooking, listServices } from './api'
import { booking, services } from './testData'

function mockFetch(status: number, body: unknown) {
  const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(body), { status }))
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

afterEach(() => vi.unstubAllGlobals())

describe('api', () => {
  it('lists services', async () => {
    const fetchMock = mockFetch(200, services)

    await expect(listServices()).resolves.toEqual(services)
    expect(fetchMock).toHaveBeenCalledWith('/api/services', undefined)
  })

  it('cancels a booking with a POST', async () => {
    const fetchMock = mockFetch(200, { ...booking, status: 'CANCELLED' })

    await expect(cancelBooking(42)).resolves.toMatchObject({ status: 'CANCELLED' })
    expect(fetchMock).toHaveBeenCalledWith('/api/bookings/42/cancel', { method: 'POST' })
  })

  it('throws an ApiError carrying the server message and field errors', async () => {
    mockFetch(400, { status: 400, message: 'Validation failed', fieldErrors: { date: 'Date is required' } })

    const error = await getBooking(1).catch((e) => e)

    expect(error).toBeInstanceOf(ApiError)
    expect(error.message).toBe('Validation failed')
    expect(error.status).toBe(400)
    expect(error.fieldErrors).toEqual({ date: 'Date is required' })
  })

  it('reports a clear error when the server is unreachable', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')))

    await expect(getBooking(1)).rejects.toThrow('Could not reach the server')
  })
})
