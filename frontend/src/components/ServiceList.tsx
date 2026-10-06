import type { Service } from '../api'
import { formatPrice } from '../format'

export default function ServiceList({ services }: { services: Service[] }) {
  if (services.length === 0) {
    return <p>No services available.</p>
  }
  return (
    <table>
      <thead>
        <tr>
          <th>Service</th>
          <th>Duration</th>
          <th>Price</th>
        </tr>
      </thead>
      <tbody>
        {services.map((service) => (
          <tr key={service.id}>
            <td>{service.name}</td>
            <td>{service.durationMinutes} min</td>
            <td>{formatPrice(service.price)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
