const API_BASE_URL = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080/api'

export function getStoredToken() {
    return localStorage.getItem('study_auth_token')
}

export function setStoredToken(token) {
    if (token) localStorage.setItem('study_auth_token', token)
    else localStorage.removeItem('study_auth_token')
}

async function request(endpoint, options = {}) {
    const token = getStoredToken()
    const headers = {
        ...(options.headers || {})
    }

    if (token) {
        headers['Authorization'] = `Bearer ${token}`
    }

    if (!(options.body instanceof FormData) && !headers['Content-Type']) {
        headers['Content-Type'] = 'application/json'
    }

    const res = await fetch(`${API_BASE_URL}${endpoint}`, {
        ...options,
        headers,
    })

    if (!res.ok) {
        const errorData = await res.json().catch(() => ({}))
        const message = errorData.error || errorData.message || `HTTP ${res.status}`
        throw new Error(message)
    }

    if (res.status === 204) return null
    return res.json()
}

// 1. Auth APIs
export const authApi = {
    async register(email, password, fullName) {
        const data = await request('/auth/register', {
            method: 'POST',
            body: JSON.stringify({ email, password, fullName })
        })
        if (data.token) setStoredToken(data.token)
        return data
    },

    async login(email, password) {
        const data = await request('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        })
        if (data.token) setStoredToken(data.token)
        return data
    },

    async me() {
        return request('/auth/me')
    },

    logout() {
        setStoredToken(null)
    }
}

// 2. Profile APIs
export const profileApi = {
    async getProfile() {
        return request('/profile')
    },

    async updateProfile(updates) {
        return request('/profile', {
            method: 'PUT',
            body: JSON.stringify(updates)
        })
    },

    async uploadAvatar(file) {
        const formData = new FormData()
        formData.append('file', file)
        return request('/profile/avatar', {
            method: 'POST',
            body: formData
        })
    }
}

// 3. Subjects APIs
export const subjectsApi = {
    async getSubjects() {
        return request('/subjects')
    },

    async getSubject(id) {
        return request(`/subjects/${id}`)
    },

    async createSubject({ name, icon, color }) {
        return request('/subjects', {
            method: 'POST',
            body: JSON.stringify({ name, icon, color })
        })
    },

    async updateSubject(id, { name, icon, color }) {
        return request(`/subjects/${id}`, {
            method: 'PUT',
            body: JSON.stringify({ name, icon, color })
        })
    },

    async deleteSubject(id) {
        return request(`/subjects/${id}`, {
            method: 'DELETE'
        })
    }
}

// 4. Chat & Gemini Streaming APIs
export const chatApi = {
    async getSessions() {
        return request('/chat/sessions')
    },

    async getOrCreateSession({ sessionType = 'general', subjectId = null, title = '' }) {
        return request('/chat/sessions', {
            method: 'POST',
            body: JSON.stringify({ sessionType, subjectId, title })
        })
    },

    async getMessages(sessionId) {
        return request(`/chat/sessions/${sessionId}/messages`)
    },

    async saveMessage(sessionId, { content, attachments = [], role = 'user' }) {
        return request(`/chat/sessions/${sessionId}/messages?role=${role}`, {
            method: 'POST',
            body: JSON.stringify({ content, attachments })
        })
    },

    async streamChat({ message, sessionId, subjectName, sectionType, attachments = [], model }, onChunk) {
        const token = getStoredToken()
        const res = await fetch(`${API_BASE_URL}/chat/stream`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...(token ? { 'Authorization': `Bearer ${token}` } : {})
            },
            body: JSON.stringify({
                message,
                sessionId,
                subjectName,
                sectionType,
                attachments,
                model
            })
        })

        if (!res.ok) {
            const err = await res.json().catch(() => ({}))
            throw new Error(err.error || `HTTP ${res.status}`)
        }

        const reader = res.body.getReader()
        const decoder = new TextDecoder()
        let fullText = ''

        while (true) {
            const { done, value } = await reader.read()
            if (done) break
            const chunk = decoder.decode(value, { stream: true })
            fullText += chunk
            if (onChunk) onChunk(chunk, fullText)
        }

        return fullText
    }
}

// 5. Materials APIs
export const materialsApi = {
    async getMaterials(subjectId) {
        const query = subjectId ? `?subjectId=${subjectId}` : ''
        return request(`/materials${query}`)
    },

    async uploadMaterial(file, subjectId) {
        const formData = new FormData()
        formData.append('file', file)
        if (subjectId) formData.append('subjectId', subjectId)

        return request('/materials/upload', {
            method: 'POST',
            body: formData
        })
    },

    async deleteMaterial(id) {
        return request(`/materials/${id}`, {
            method: 'DELETE'
        })
    }
}

// 6. Analytics APIs
export const analyticsApi = {
    async getDashboard() {
        return request('/analytics/dashboard')
    },

    async getInsights() {
        return request('/analytics/insights')
    }
}

