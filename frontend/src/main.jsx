import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

// Clean up any stale Supabase tokens from previous client-side iterations
try {
  for (let i = localStorage.length - 1; i >= 0; i--) {
    const key = localStorage.key(i)
    if (key && (key.startsWith('sb-') || key.includes('supabase'))) {
      localStorage.removeItem(key)
    }
  }
} catch (e) {
  // Ignore storage access errors
}

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
