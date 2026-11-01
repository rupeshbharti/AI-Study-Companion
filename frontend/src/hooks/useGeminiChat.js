import { useState, useCallback, useRef } from 'react'
import { chatApi } from '../lib/api'
import { useAuth } from '../context/AuthContext'
import { v4 as uuidv4 } from 'uuid'

export function useGeminiChat(sessionId, context = {}) {
    const [messages, setMessages] = useState([])
    const [streaming, setStreaming] = useState(false)
    const [streamingText, setStreamingText] = useState('')
    const [error, setError] = useState(null)
    const { user } = useAuth()
    const messagesRef = useRef([])

    const loadMessages = useCallback(async () => {
        if (!sessionId) return
        try {
            const data = await chatApi.getMessages(sessionId)
            if (data) {
                setMessages(data)
                messagesRef.current = data
            }
        } catch (err) {
            console.error('Failed to load messages:', err)
        }
    }, [sessionId])

    const sendMessage = useCallback(async (content, attachments = []) => {
        if (!user || !sessionId || !content.trim()) return
        setError(null)
        setStreaming(true)
        setStreamingText('')

        const userMsg = {
            id: uuidv4(),
            sessionId,
            userId: user.id,
            role: 'user',
            content,
            attachments,
            createdAt: new Date().toISOString(),
        }

        setMessages(prev => {
            const next = [...prev, userMsg]
            messagesRef.current = next
            return next
        })

        try {
            const fullResponse = await chatApi.streamChat(
                {
                    message: content,
                    sessionId,
                    subjectName: context.subjectName,
                    sectionType: context.sectionType,
                    attachments,
                },
                (chunk, currentFull) => {
                    setStreamingText(currentFull)
                }
            )

            const assistantMsg = {
                id: uuidv4(),
                sessionId,
                userId: user.id,
                role: 'assistant',
                content: fullResponse,
                createdAt: new Date().toISOString(),
            }

            setMessages(prev => {
                const next = [...prev, assistantMsg]
                messagesRef.current = next
                return next
            })
        } catch (err) {
            const message = err?.message || 'Failed to communicate with AI service'
            setError(message)
            const errMsg = {
                id: uuidv4(),
                role: 'assistant',
                content: `Error: ${message}`,
                createdAt: new Date().toISOString(),
            }
            setMessages(prev => {
                const next = [...prev, errMsg]
                messagesRef.current = next
                return next
            })
        } finally {
            setStreaming(false)
            setStreamingText('')
        }
    }, [user, sessionId, context])

    return { messages, sendMessage, streaming, streamingText, error, loadMessages, setMessages }
}
