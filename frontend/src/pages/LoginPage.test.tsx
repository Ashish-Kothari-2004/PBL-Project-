import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { LoginPage } from './LoginPage'

const { login } = vi.hoisted(() => ({ login: vi.fn() }))

vi.mock('../auth/useAuth', () => ({
  useAuth: () => ({ login }),
}))

describe('LoginPage', () => {
  beforeEach(() => {
    login.mockReset()
  })

  it('submits valid credentials and navigates to the landing page', async () => {
    login.mockResolvedValue(undefined)
    const user = userEvent.setup()

    render(
      <MemoryRouter initialEntries={['/login']}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<h1>Welcome</h1>} />
        </Routes>
      </MemoryRouter>,
    )

    await user.type(screen.getByLabelText('Email'), 'alice@example.com')
    await user.type(screen.getByLabelText('Password'), 'Secret123!')
    await user.click(screen.getByRole('button', { name: 'Login' }))

    expect(login).toHaveBeenCalledWith('alice@example.com', 'Secret123!')
    expect(await screen.findByRole('heading', { name: 'Welcome' })).toBeTruthy()
  })
})
