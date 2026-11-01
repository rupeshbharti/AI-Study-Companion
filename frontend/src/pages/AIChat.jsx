import { useEffect, useState } from 'react'
import { chatApi } from '../lib/api'
import { useAuth } from '../context/AuthContext'
import { useGeminiChat } from '../hooks/useGeminiChat'
import ChatWindow from '../components/chat/ChatWindow'

export default function AIChat() {
    const { user } = useAuth()
    const [sessionId, setSessionId] = useState(null)

    useEffect(() => {
        if (!user) return
        async function getOrCreate() {
            try {
                const session = await chatApi.getOrCreateSession({
                    sessionType: 'general',
                    title: 'General Chat'
                })
                if (session?.id) setSessionId(session.id)
            } catch (err) {
                console.error('Failed to get or create general chat session:', err)
            }
        }
        getOrCreate()
    }, [user])

    const chatHook = useGeminiChat(sessionId, {})

    return (
        <div className="aichat-page">
            {sessionId ? (
                <ChatWindow
                    hook={chatHook}
                    context={{ sessionId }}
                    title="AI Study Assistant"
                    subtitle="Ask me anything — I'll adapt to your education level"
                />
            ) : (
                <div className="aichat-loading">
                    <span className="spinner" style={{ width: 32, height: 32 }} />
                    <p>Starting your session…</p>
                </div>
            )}

            <style>{`
        .aichat-page {
          height: calc(100vh - var(--topbar-h));
          display: flex;
          flex-direction: column;
          overflow: hidden;
        }
        .aichat-loading {
          flex: 1;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          gap: 14px;
          color: var(--text-3);
          font-size: 14px;
        }
      `}</style>
        </div>
    )
}
