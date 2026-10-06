import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import * as api from '../api'
import { booking } from '../testData'
import BookingLookup from './BookingLookup'

vi.mock('../api', async (importOriginal) => ({
  ...(await importOriginal<typeof api>()),
  getBooking: vi.fn(),
}))

describe('BookingLookup', () => {
  beforeEach(() => vi.resetAllMocks())

  it('shows the booking for the entered ID', async () => {
    vi.mocked(api.getBooking).mockResolvedValue(booking)
    const user = userEvent.setup()
    render(<BookingLookup />)

    await user.type(screen.getByLabelText('Booking ID'), '42')
    await user.click(screen.getByRole('button', { name: 'Find' }))

    expect(api.getBooking).toHaveBeenCalledWith(42)
    expect(await screen.findByText('Haircut (30 min)')).toBeInTheDocument()
    expect(screen.getByText('2030-01-15 at 10:00')).toBeInTheDocument()
    expect(screen.getByText('CONFIRMED')).toBeInTheDocument()
  })

  it('shows an error when the booking does not exist', async () => {
    vi.mocked(api.getBooking).mockRejectedValue(new api.ApiError('Booking 7 not found', 404))
    const user = userEvent.setup()
    render(<BookingLookup />)

    await user.type(screen.getByLabelText('Booking ID'), '7')
    await user.click(screen.getByRole('button', { name: 'Find' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Booking 7 not found')
  })
})
