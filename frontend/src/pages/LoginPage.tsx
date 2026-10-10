import { useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { useAuth } from '../auth/useAuth'
import { getApiErrorMessage } from '../api/client'

const loginSchema = z.object({
  email: z.string().email('Enter a valid email'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
})

export function LoginPage() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [formError, setFormError] = useState<string | null>(null)
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(loginSchema) })

  const onSubmit = async (values: { email: string; password: string }) => {
    setFormError(null)
    try {
      await login(values.email, values.password)
      navigate('/')
    } catch (error) {
      setFormError(getApiErrorMessage(error))
    }
  }

  return (
    <main className="auth-shell">
      <section className="auth-card">
        <h1>Sign in</h1>
        <form onSubmit={handleSubmit(onSubmit)} className="auth-form">
          {formError ? <p role="alert">{formError}</p> : null}
          <label>
            Email
            <input {...register('email')} type="email" placeholder="name@example.com" />
            {errors.email ? <span>{errors.email.message as string}</span> : null}
          </label>

          <label>
            Password
            <input {...register('password')} type="password" placeholder="••••••••" />
            {errors.password ? <span>{errors.password.message as string}</span> : null}
          </label>

          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Signing in...' : 'Login'}
          </button>
        </form>

        <p>
          Need an account? <Link to="/register">Register</Link>
        </p>
      </section>
    </main>
  )
}
