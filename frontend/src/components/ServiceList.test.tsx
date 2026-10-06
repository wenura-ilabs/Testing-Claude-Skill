import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { services } from '../testData'
import ServiceList from './ServiceList'

describe('ServiceList', () => {
  it('shows name, duration and price for each service', () => {
    render(<ServiceList services={services} />)

    expect(screen.getByRole('row', { name: 'Haircut 30 min $25.00' })).toBeInTheDocument()
    expect(screen.getByRole('row', { name: 'Massage 60 min $60.00' })).toBeInTheDocument()
  })

  it('shows a message when there are no services', () => {
    render(<ServiceList services={[]} />)

    expect(screen.getByText('No services available.')).toBeInTheDocument()
  })
})
