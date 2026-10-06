import { useState, type FormEvent } from 'react'
import { getBooking, type Booking } from '../api'
import BookingDetails from './BookingDetails'

export default function BookingLookup() {
  const [id, setId] = useState('')
  const [booking, setBooking] = useState<Booking | null>(null)
  const [error, setError] = useState('')

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError('')
    setBooking(null)
    try {
      setBooking(await getBooking(Number(id)))
    } catch (e) {
      setError((e as Error).message)
    }
  }

  return (
    <>
      <form className="inline" onSubmit={handleSubmit}>
        <label>
          Booking ID
          <input type="number" min="1" value={id} onChange={(e) => setId(e.target.value)} required />
        </label>
        <button type="submit">Find</button>
      </form>
      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}
      {booking && <BookingDetails booking={booking} onChange={setBooking} />}
    </>
  )
}
