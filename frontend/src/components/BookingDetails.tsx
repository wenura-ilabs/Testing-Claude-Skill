import { useState } from 'react'
import { cancelBooking, type Booking } from '../api'
import { formatPrice } from '../format'

interface Props {
  booking: Booking
  onChange: (booking: Booking) => void
}

export default function BookingDetails({ booking, onChange }: Props) {
  const [error, setError] = useState('')
  const [cancelling, setCancelling] = useState(false)

  async function handleCancel() {
    setError('')
    setCancelling(true)
    try {
      onChange(await cancelBooking(booking.id))
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setCancelling(false)
    }
  }

  return (
    <div>
      <dl>
        <dt>Booking ID</dt>
        <dd>{booking.id}</dd>
        <dt>Service</dt>
        <dd>
          {booking.serviceName} ({booking.durationMinutes} min)
        </dd>
        <dt>When</dt>
        <dd>
          {booking.date} at {booking.time}
        </dd>
        <dt>Customer</dt>
        <dd>
          {booking.customerName} ({booking.customerEmail})
        </dd>
        <dt>Original price</dt>
        <dd>{formatPrice(booking.originalPrice)}</dd>
        <dt>Discount</dt>
        <dd>{formatPrice(booking.discountAmount)}</dd>
        <dt>Total price</dt>
        <dd>{formatPrice(booking.totalPrice)}</dd>
        {booking.promoCode && (
          <>
            <dt>Promo code</dt>
            <dd>{booking.promoCode}</dd>
          </>
        )}
        <dt>Status</dt>
        <dd>{booking.status}</dd>
      </dl>
      {booking.status === 'CONFIRMED' && (
        <button className="danger" onClick={handleCancel} disabled={cancelling}>
          Cancel booking
        </button>
      )}
      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}
    </div>
  )
}
