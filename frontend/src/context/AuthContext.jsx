import { createContext, useContext, useEffect, useState } from 'react'
import { authApi, profileApi, getStoredToken } from '../lib/api'

const AuthContext = createContext({})

const BACKEND_BASE = (import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080/api').replace('/api', '')

function normalizeProfile(p) {
    if (!p) return null
    const fullName = p.fullName || p.full_name || ''
    let avatarUrl = p.avatarUrl || p.avatar_url || ''
    if (avatarUrl && !avatarUrl.startsWith('http') && !avatarUrl.startsWith('data:')) {
        avatarUrl = `${BACKEND_BASE}${avatarUrl.startsWith('/') ? '' : '/'}${avatarUrl}`
    }
    const educationLevel = p.educationLevel || p.education_level || ''
    const educationDetails = p.educationDetails || p.education_details || null
    const onboardingCompleted = p.onboardingCompleted ?? p.onboarding_completed ?? false

    return {
        ...p,
        fullName,
        full_name: fullName,
        avatarUrl,
        avatar_url: avatarUrl,
        educationLevel,
        education_level: educationLevel,
        educationDetails,
        education_details: educationDetails,
        onboardingCompleted,
        onboarding_completed: onboardingCompleted
    }
}

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null)
    const [profile, setProfile] = useState(null)
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        async function checkAuth() {
            const token = getStoredToken()
            if (!token) {
                setLoading(false)
                return
            }

            try {
                const data = await authApi.me()
                if (data?.user) {
                    setUser(data.user)
                    setProfile(normalizeProfile(data.profile))
                } else {
                    authApi.logout()
                    setUser(null)
                    setProfile(null)
                }
            } catch (err) {
                console.warn('Auth check error, clearing session:', err.message)
                authApi.logout()
                setUser(null)
                setProfile(null)
            } finally {
                setLoading(false)
            }
        }

        checkAuth()
    }, [])

    async function register(email, password, fullName) {
        const data = await authApi.register(email, password, fullName)
        setUser(data.user)
        setProfile(normalizeProfile(data.profile))
        return data
    }

    async function login(email, password) {
        const data = await authApi.login(email, password)
        setUser(data.user)
        setProfile(normalizeProfile(data.profile))
        return data
    }

    async function refreshProfile() {
        try {
            const fresh = await profileApi.getProfile()
            setProfile(normalizeProfile(fresh))
        } catch (err) {
            console.error('Failed to refresh profile:', err)
        }
    }

    function signOut() {
        authApi.logout()
        setUser(null)
        setProfile(null)
    }

    return (
        <AuthContext.Provider value={{
            user,
            profile,
            session: user ? { user } : null,
            loading,
            login,
            register,
            signOut,
            refreshProfile
        }}>
            {children}
        </AuthContext.Provider>
    )
}

export function useAuth() {
    return useContext(AuthContext)
}
