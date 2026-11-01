import { NavLink } from 'react-router-dom'
import {
    LayoutDashboard, BookOpen, MessageSquare, Library,
    BarChart3
} from 'lucide-react'

const BOTTOM_NAV_ITEMS = [
    { to: '/', icon: LayoutDashboard, label: 'Home' },
    { to: '/subjects', icon: BookOpen, label: 'Subjects' },
    { to: '/chat', icon: MessageSquare, label: 'AI Chat' },
    { to: '/library', icon: Library, label: 'Library' },
    { to: '/insights', icon: BarChart3, label: 'Insights' },
]

export default function BottomNav() {
    return (
        <nav className="bottom-nav">
            {BOTTOM_NAV_ITEMS.map(({ to, icon: Icon, label }) => (
                <NavLink
                    key={to}
                    to={to}
                    end={to === '/'}
                    className={({ isActive }) => `bottom-nav-item ${isActive ? 'active' : ''}`}
                >
                    <Icon size={20} />
                    <span>{label}</span>
                </NavLink>
            ))}
            <style>{`
                .bottom-nav {
                    display: none;
                }
                
                @media (max-width: 768px) {
                    .bottom-nav {
                        display: flex;
                        align-items: center;
                        justify-content: space-around;
                        position: fixed;
                        bottom: 0;
                        left: 0;
                        right: 0;
                        height: var(--bottomnav-h);
                        background: var(--bg-translucent);
                        backdrop-filter: blur(16px);
                        -webkit-backdrop-filter: blur(16px);
                        border-top: 1px solid var(--glass-border);
                        z-index: 100;
                        padding-bottom: env(safe-area-inset-bottom);
                    }
                    
                    .bottom-nav-item {
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        gap: 4px;
                        color: var(--text-2);
                        text-decoration: none;
                        flex: 1;
                        height: 100%;
                        transition: color 0.2s;
                    }
                    
                    .bottom-nav-item span {
                        font-size: 10px;
                        font-weight: 500;
                    }
                    
                    .bottom-nav-item.active {
                        color: var(--brand-from);
                    }
                }
            `}</style>
        </nav>
    )
}
