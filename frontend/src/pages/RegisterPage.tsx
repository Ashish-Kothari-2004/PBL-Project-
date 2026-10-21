import { useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { useAuth } from '../auth/useAuth'
import { getApiErrorMessage } from '../api/client'

const registerSchema = z.object({
  name: z.string().min(2, 'Name is required'),
  email: z.string().email('Enter a valid email'),
  password: z.string()
    .min(8, 'Password must be at least 8 characters')
    .max(72, 'Password must be no more than 72 characters')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).+$/, 'Use uppercase, lowercase, a number, and a special character'),
  role: z.enum(['DONOR', 'RECIPIENT']),
})

export function RegisterPage() {
  const navigate = useNavigate()
  const { register: registerUser } = useAuth()
  const [formError, setFormError] = useState<string | null>(null)
  const [showPassword, setShowPassword] = useState(false)
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(registerSchema) })

  const onSubmit = async (values: {
    name: string
    email: string
    password: string
    role: 'DONOR' | 'RECIPIENT'
  }) => {
    setFormError(null)
    try {
      await registerUser(values)
      navigate('/')
    } catch (error) {
      setFormError(getApiErrorMessage(error))
    }
  }

  return (
    <main className="auth-shell">
      <section className="auth-card">
        <h1>Create account</h1>
        <form onSubmit={handleSubmit(onSubmit)} className="auth-form">
          {formError ? <p role="alert">{formError}</p> : null}
          <label>
            Full name
            <input {...register('name')} type="text" placeholder="Your full name" />
            {errors.name ? <span>{errors.name.message as string}</span> : null}
          </label>

          <label>
            Email
            <input {...register('email')} type="email" placeholder="name@example.com" />
            {errors.email ? <span>{errors.email.message as string}</span> : null}
          </label>

          <label>
            Password
            <input
  {...register('password')}
  type={showPassword ? 'text' : 'password'}
  placeholder="At least 8 chars"
/>
<button type="button" onClick={() => setShowPassword(!showPassword)}>
  {showPassword ? 'Hide' : 'Show'} Password
</button>
            {errors.password ? <span>{errors.password.message as string}</span> : null}
          </label>

          <label>
            Role
            <select {...register('role')} defaultValue="DONOR">
              <option value="DONOR">Donor</option>
              <option value="RECIPIENT">Recipient</option>
            </select>
          </label>

          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Creating account...' : 'Register'}
          </button>
        </form>

        <p>
          Already have an account? <Link to="/login">Login</Link>
        </p>
      </section>
    </main>
  )
}
