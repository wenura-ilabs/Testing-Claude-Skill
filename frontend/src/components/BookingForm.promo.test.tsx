import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import * as api from '../api'
import { booking, services } from '../testData'
import BookingForm from './BookingForm'

vi.mock('../api', async (importOriginal) => ({
  ...(await importOriginal<typeof api>()),
  createBooking: vi.fn(),
}))

async function fillAndSubmit(promoCode?: string) {
  const user = userEvent.setup()
  await user.selectOptions(screen.getByLabelText('Service'), '1')
  await user.type(screen.getByLabelText('Date'), '2030-01-15')
  await user.selectOptions(screen.getByLabelText('Time slot'), '10:00')
  await user.type(screen.getByLabelText('Name'), 'Ada Lovelace')
  await user.type(screen.getByLabelText('Email'), 'ada@example.com')
  if (promoCode) {
    await user.type(screen.getByLabelText('Promo code'), promoCode)
  }
  await user.click(screen.getByRole('button', { name: 'Book' }))
}

const valueOf = (term: string) => screen.getByText(term).nextElementSibling

describe('BookingForm promo codes', () => {
  beforeEach(() => vi.resetAllMocks())

  it('has an optional promo code field', () => {
    render(<BookingForm services={services} />)

    expect(screen.getByLabelText('Promo code')).not.toBeRequired()
  })

  it('sends the promo code and shows original price, discount and total', async () => {
    vi.mocked(api.createBooking).mockResolvedValue({
      ...booking,
      originalPrice: 25,
      discountAmount: 2.5,
      totalPrice: 22.5,
      promoCode: 'WELCOME10',
    })
    render(<BookingForm services={services} />)

    await fillAndSubmit('welcome10')

    expect(api.createBooking).toHaveBeenCalledWith(expect.objectContaining({ promoCode: 'welcome10' }))
    expect(await screen.findByText('Booking confirmed')).toBeInTheDocument()
    expect(valueOf('Original price')).toHaveTextContent('$25.00')
    expect(valueOf('Discount')).toHaveTextContent('$2.50')
    expect(valueOf('Total price')).toHaveTextContent('$22.50')
    expect(screen.getByText('WELCOME10')).toBeInTheDocument()
  })

  it('sends no promo code when the field is left empty or blank', async () => {
    vi.mocked(api.createBooking).mockResolvedValue(booking)
    render(<BookingForm services={services} />)

    await fillAndSubmit('   ')

    expect(vi.mocked(api.createBooking).mock.calls[0][0]).not.toHaveProperty('promoCode')
    expect(await screen.findByText('Booking confirmed')).toBeInTheDocument()
    expect(valueOf('Discount')).toHaveTextContent('$0.00')
  })

  it('shows the error for a rejected code and no confirmation', async () => {
    vi.mocked(api.createBooking).mockRejectedValue(new api.ApiError('Promo code OLDCODE has expired', 400))
    render(<BookingForm services={services} />)

    await fillAndSubmit('OLDCODE')

    expect(await screen.findByRole('alert')).toHaveTextContent('Promo code OLDCODE has expired')
    expect(screen.queryByText('Booking confirmed')).not.toBeInTheDocument()
  })
})
