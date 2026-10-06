import { useState, type FormEvent } from 'react'
import { ApiError, createBooking, type Booking, type Service } from '../api'
import { formatPrice, TIME_SLOTS } from '../format'
import BookingDetails from './BookingDetails'

export default function BookingForm({ services }: { services: Service[] }) {
  const [serviceId, setServiceId] = useState('')
  const [date, setDate] = useState('')
  const [time, setTime] = useState('')
  const [customerName, setCustomerName] = useState('')
  const [customerEmail, setCustomerEmail] = useState('')
  const [promoCode, setPromoCode] = useState('')
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)
  const [booking, setBooking] = useState<Booking | null>(null)

  const selected = services.find((service) => String(service.id) === serviceId)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError('')
    setFieldErrors({})
    setBooking(null)
    setSubmitting(true)
    try {
      setBooking(
        await createBooking({
          serviceId: Number(serviceId),
          date,
          time,
          customerName,
          customerEmail,
          ...(promoCode.trim() && { promoCode: promoCode.trim() }),
        }),
      )
    } catch (e) {
      setError((e as Error).message)
      if (e instanceof ApiError) {
        setFieldErrors(e.fieldErrors)
      }
    } finally {
      setSubmitting(false)
    }
  }

  const fieldError = (field: string) =>
    fieldErrors[field] && <span className="field-error">{fieldErrors[field]}</span>

  return (
    <>
      <form onSubmit={handleSubmit}>
        <label>
          Service
          <select value={serviceId} onChange={(e) => setServiceId(e.target.value)} required>
            <option value="">Select a service</option>
            {services.map((service) => (
              <option key={service.id} value={service.id}>
                {service.name} ({service.durationMinutes} min, {formatPrice(service.price)})
              </option>
            ))}
          </select>
          {fieldError('serviceId')}
        </label>
        <label>
          Date
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} required />
          {fieldError('date')}
        </label>
        <label>
          Time slot
          <select value={time} onChange={(e) => setTime(e.target.value)} required>
            <option value="">Select a time slot</option>
            {TIME_SLOTS.map((slot) => (
              <option key={slot} value={slot}>
                {slot}
              </option>
            ))}
          </select>
          {fieldError('time')}
        </label>
        <label>
          Name
          <input value={customerName} onChange={(e) => setCustomerName(e.target.value)} required />
          {fieldError('customerName')}
        </label>
        <label>
          Email
          <input
            type="email"
            value={customerEmail}
            onChange={(e) => setCustomerEmail(e.target.value)}
            required
          />
          {fieldError('customerEmail')}
        </label>
        <label>
          Promo code
          <input
            value={promoCode}
            onChange={(e) => setPromoCode(e.target.value)}
            placeholder="Optional"
          />
        </label>
        {selected && <p>Service price: {formatPrice(selected.price)}</p>}
        <button type="submit" disabled={submitting}>
          Book
        </button>
        {error && (
          <p className="error" role="alert">
            {error}
          </p>
        )}
      </form>
      {booking && (
        <div>
          <h3>Booking confirmed</h3>
          <BookingDetails booking={booking} onChange={setBooking} />
        </div>
      )}
    </>
  )
}
