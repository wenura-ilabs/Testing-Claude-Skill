import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import * as api from '../api'
import { booking, services } from '../testData'
import BookingForm from './BookingForm'

vi.mock('../api', async (importOriginal) => ({
  ...(await importOriginal<typeof api>()),
  createBooking: vi.fn(),
  cancelBooking: vi.fn(),
}))

async function fillAndSubmit() {
  const user = userEvent.setup()
  await user.selectOptions(screen.getByLabelText('Service'), '1')
  await user.type(screen.getByLabelText('Date'), '2030-01-15')
  await user.selectOptions(screen.getByLabelText('Time slot'), '10:00')
  await user.type(screen.getByLabelText('Name'), 'Ada Lovelace')
  await user.type(screen.getByLabelText('Email'), 'ada@example.com')
  await user.click(screen.getByRole('button', { name: 'Book' }))
  return user
}

describe('BookingForm', () => {
  beforeEach(() => vi.resetAllMocks())

  it('submits the booking and shows its total price', async () => {
    vi.mocked(api.createBooking).mockResolvedValue(booking)
    render(<BookingForm services={services} />)

    await fillAndSubmit()

    expect(api.createBooking).toHaveBeenCalledWith({
      serviceId: 1,
      date: '2030-01-15',
      time: '10:00',
      customerName: 'Ada Lovelace',
      customerEmail: 'ada@example.com',
    })
    expect(await screen.findByText('Booking confirmed')).toBeInTheDocument()
    expect(screen.getByText('Total price').nextElementSibling).toHaveTextContent('$25.00')
    expect(screen.getByText('42')).toBeInTheDocument()
  })

  it('shows the server error when the slot is already booked', async () => {
    vi.mocked(api.createBooking).mockRejectedValue(
      new api.ApiError('The 10:00 slot on 2030-01-15 is already booked', 409),
    )
    render(<BookingForm services={services} />)

    await fillAndSubmit()

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'The 10:00 slot on 2030-01-15 is already booked',
    )
    expect(screen.queryByText('Booking confirmed')).not.toBeInTheDocument()
  })

  it('shows field errors returned by the server', async () => {
    vi.mocked(api.createBooking).mockRejectedValue(
      new api.ApiError('Validation failed', 400, {
        customerEmail: 'Customer email must be a valid email address',
      }),
    )
    render(<BookingForm services={services} />)

    await fillAndSubmit()

    expect(await screen.findByText('Customer email must be a valid email address')).toBeInTheDocument()
  })

  it('cancels a booking that was just created', async () => {
    vi.mocked(api.createBooking).mockResolvedValue(booking)
    vi.mocked(api.cancelBooking).mockResolvedValue({ ...booking, status: 'CANCELLED' })
    render(<BookingForm services={services} />)

    const user = await fillAndSubmit()
    await user.click(await screen.findByRole('button', { name: 'Cancel booking' }))

    expect(api.cancelBooking).toHaveBeenCalledWith(42)
    expect(await screen.findByText('CANCELLED')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Cancel booking' })).not.toBeInTheDocument()
  })
})
