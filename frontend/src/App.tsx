import { useState } from 'react'
import './App.css'

function App() {
  const [name, setName] = useState('')
  const [message, setMessage] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setMessage(null)
    const trimmed = name.trim()
    if (!trimmed) {
      setError('Please enter a name')
      return
    }
    setLoading(true)
    try {
      const res = await fetch(`/api?name=${encodeURIComponent(trimmed)}`)
      if (!res.ok) {
        throw new Error(`Request failed: ${res.status}`)
      }
      const text = await res.text()
      setMessage(text)
    } catch (err: any) {
      setError(err?.message ?? 'Request failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card" style={{ maxWidth: 420, margin: '3rem auto' }}>
      <h1>Say Hello</h1>
      <form onSubmit={handleSubmit} style={{ display: 'flex', gap: 8 }}>
        <input
          type="text"
          placeholder="Enter your name"
          value={name}
          onChange={(e) => setName(e.target.value)}
          style={{ flex: 1 }}
        />
        <button type="submit" disabled={loading}>
          {loading ? 'Sending…' : 'Greet'}
        </button>
      </form>
      {error && (
        <p style={{ color: 'crimson', marginTop: 12 }}>Error: {error}</p>
      )}
      {message && (
        <p style={{ marginTop: 12 }} data-testid="greeting">
          {message}
        </p>
      )}
    </div>
  )
}

export default App
