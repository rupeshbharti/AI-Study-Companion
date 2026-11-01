import React from 'react';
import { Link } from 'react-router-dom';
import { Sparkles, Sun, Moon, Zap, Rocket, MessageSquare, Library, BookOpen } from 'lucide-react';
import { useTheme } from '../context/ThemeContext';

export default function Landing() {
  const { theme, toggleTheme } = useTheme();

  return (
    <div style={{ minHeight: '100vh', background: 'var(--bg)', color: 'var(--text-1)' }}>
      <nav style={{
        position: 'fixed', top: 0, left: 0, right: 0, height: 'var(--topbar-h)',
        display: 'flex', justifyContent: 'space-between', alignItems: 'center',
        padding: '0 40px', backdropFilter: 'blur(16px)', WebkitBackdropFilter: 'blur(16px)',
        zIndex: 100, borderBottom: '1px solid var(--glass-border)',
        background: 'var(--bg-translucent)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{
            width: '36px', height: '36px', background: 'linear-gradient(135deg, var(--brand-from), var(--brand-to))',
            borderRadius: '9px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', flexShrink: 0
          }}>
            <Sparkles width={20} height={20} />
          </div>
          <span style={{
            fontSize: '18px', fontWeight: 800, background: 'linear-gradient(135deg, var(--brand-from), var(--brand-to))',
            WebkitBackgroundClip: 'text', color: 'transparent'
          }}>StudyAI</span>
        </div>
        
        <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
          <button 
            onClick={toggleTheme}
            style={{
              width: '36px', height: '36px', borderRadius: '9px', background: 'var(--glass)',
              border: '1px solid var(--glass-border)', display: 'flex', alignItems: 'center',
              justifyContent: 'center', color: 'var(--text-2)', cursor: 'pointer', transition: '0.2s'
            }}
            title="Toggle theme"
          >
            {theme === 'dark' ? <Sun width={18} height={18} /> : <Moon width={18} height={18} />}
          </button>
          <Link to="/login" className="btn" style={{ color: 'var(--text-1)', fontWeight: 600 }}>Sign In</Link>
          <Link to="/register" className="btn btn-primary" style={{ padding: '8px 16px' }}>Get Started</Link>
        </div>
      </nav>

      <main style={{
        minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center',
        flexDirection: 'column', textAlign: 'center', padding: '120px 20px 60px',
        background: 'radial-gradient(circle at center, rgba(99, 102, 241, 0.12) 0%, var(--bg) 60%)',
        position: 'relative', overflow: 'hidden'
      }}>
        <div style={{
          position: 'absolute', width: '500px', height: '500px',
          background: 'radial-gradient(circle, var(--brand-glow), transparent 70%)',
          top: '10%', left: '-150px', borderRadius: '50%', opacity: 0.6, pointerEvents: 'none'
        }}></div>
        <div style={{
          position: 'absolute', width: '600px', height: '600px',
          background: 'radial-gradient(circle, rgba(139, 92, 246, 0.15), transparent 70%)',
          bottom: '-150px', right: '-200px', borderRadius: '50%', pointerEvents: 'none'
        }}></div>

        <div className="badge badge-brand" style={{ marginBottom: '24px', padding: '6px 14px', fontSize: '13px' }}>
          <Zap width={14} height={14} /> Empower Your Learning Journey
        </div>
        
        <h1 style={{
          fontSize: 'clamp(40px, 6vw, 64px)', fontWeight: 800, lineHeight: 1.1,
          marginBottom: '24px', maxWidth: '900px', zIndex: 2, letterSpacing: '-0.02em'
        }}>
          Your Ultimate <span className="gradient-text">AI Study Companion</span>
        </h1>
        
        <p style={{
          fontSize: '18px', color: 'var(--text-2)', maxWidth: '650px', marginBottom: '40px',
          zIndex: 2, lineHeight: 1.6
        }}>
          Upload your materials, organize your subjects, and let AI help you learn faster. StudyAI acts as your personal tutor, available 24/7.
        </p>

        <div style={{ display: 'flex', gap: '16px', zIndex: 2, flexWrap: 'wrap', justifyContent: 'center' }}>
          <Link to="/register" className="btn btn-primary" style={{ padding: '16px 32px', fontSize: '16px', borderRadius: '30px' }}>
            <Rocket width={18} height={18} /> Start Learning for Free
          </Link>
          <a href="#features" className="btn glass-card" style={{ padding: '16px 32px', fontSize: '16px', borderRadius: '30px', border: '1px solid var(--glass-border)' }}>
            Explore Features
          </a>
        </div>

        <div id="features" style={{
          display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '24px',
          maxWidth: '1200px', margin: '80px auto 0', zIndex: 2, position: 'relative'
        }}>
          <div className="glass-card" style={{ padding: '32px', textAlign: 'left', transition: 'transform 0.3s' }}>
            <div style={{
              display: 'inline-flex', color: 'var(--brand-from)', marginBottom: '20px',
              background: 'rgba(99, 102, 241, 0.15)', padding: '14px', borderRadius: '14px',
              border: '1px solid rgba(99, 102, 241, 0.2)'
            }}>
              <MessageSquare width={32} height={32} />
            </div>
            <h3 style={{ fontSize: '22px', marginBottom: '12px', color: 'var(--text-1)' }}>Interactive AI Chat</h3>
            <p style={{ fontSize: '15px', color: 'var(--text-3)', margin: 0, lineHeight: 1.5 }}>
              Ask questions, clarify complex academic topics, and get step-by-step explanations instantly from our advanced AI model tailored to your academic level.
            </p>
          </div>

          <div className="glass-card" style={{ padding: '32px', textAlign: 'left', transition: 'transform 0.3s' }}>
            <div style={{
              display: 'inline-flex', color: '#22c55e', marginBottom: '20px',
              background: 'rgba(34,197,94,0.1)', padding: '14px', borderRadius: '14px',
              border: '1px solid rgba(34,197,94,0.2)'
            }}>
              <Library width={32} height={32} />
            </div>
            <h3 style={{ fontSize: '22px', marginBottom: '12px', color: 'var(--text-1)' }}>Smart Document Library</h3>
            <p style={{ fontSize: '15px', color: 'var(--text-3)', margin: 0, lineHeight: 1.5 }}>
              Upload PDFs, lecture notes, and video lectures. Chat directly with your documents and let AI summarize long materials in seconds.
            </p>
          </div>

          <div className="glass-card" style={{ padding: '32px', textAlign: 'left', transition: 'transform 0.3s' }}>
            <div style={{
              display: 'inline-flex', color: '#f59e0b', marginBottom: '20px',
              background: 'rgba(245,158,11,0.1)', padding: '14px', borderRadius: '14px',
              border: '1px solid rgba(245,158,11,0.2)'
            }}>
              <BookOpen width={32} height={32} />
            </div>
            <h3 style={{ fontSize: '22px', marginBottom: '12px', color: 'var(--text-1)' }}>Subject Organization</h3>
            <p style={{ fontSize: '15px', color: 'var(--text-3)', margin: 0, lineHeight: 1.5 }}>
              Keep your courses neatly organized in dedicated workspaces. Track your syllabus progress, manage notes, and boost your overall study efficiency.
            </p>
          </div>
        </div>
      </main>

      <footer style={{
        textAlign: 'center', padding: '40px', color: 'var(--text-3)', fontSize: '14px',
        borderTop: '1px solid var(--glass-border)', background: 'var(--bg-2)', position: 'relative', zIndex: 2
      }}>
        <p>&copy; 2026 StudyAI Companion. All rights reserved. Built to help students succeed.</p>
      </footer>
    </div>
  );
}
