import { useEffect, useState } from 'react'
import { listServices, type Service } from './api'
import BookingForm from './components/BookingForm'
import BookingLookup from './components/BookingLookup'
import ServiceList from './components/ServiceList'

export default function App() {
  const [services, setServices] = useState<Service[] | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    listServices()
      .then(setServices)
      .catch((e: Error) => setError(e.message))
  }, [])

  return (
    <main>
      <h1>Booking</h1>
      <section>
        <h2>Services</h2>
        {error && (
          <p className="error" role="alert">
            {error}
          </p>
        )}
        {!error && !services && <p>Loading services…</p>}
        {services && <ServiceList services={services} />}
      </section>
      <section>
        <h2>Book a service</h2>
        <BookingForm services={services ?? []} />
      </section>
      <section>
        <h2>Find a booking</h2>
        <BookingLookup />
      </section>
    </main>
  )
}
