// Supabase client disabled - project runs on Spring Boot backend
export const supabase = {
    auth: {
        getSession: async () => ({ data: { session: null }, error: null }),
        onAuthStateChange: () => ({ data: { subscription: { unsubscribe: () => {} } } }),
        signOut: async () => ({ error: null }),
    },
    storage: {
        from: () => ({
            upload: async () => ({ data: null, error: null }),
            getPublicUrl: () => ({ data: { publicUrl: '' } }),
        })
    }
}

export const SUPABASE_URL = ''
