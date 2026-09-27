import axios from 'axios'

/**
 * Sem headers manuais de Content-Type aqui de propósito: o axios já define
 * "application/json" sozinho quando o body é um objeto, e o boundary de
 * multipart sozinho quando o body é FormData (como no upload do CSV).
 * Forçar um Content-Type fixo no default quebraria o upload.
 */
export const axiosClient = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL ?? '',
})

// Normaliza qualquer falha (rede, timeout, 4xx/5xx) numa mensagem legível única,
// pra quem consome não precisar checar error.response.data em cada chamada.
axiosClient.interceptors.response.use(
    (response) => response,
    (error) => {
        const message =
            error.response?.data?.message ??
            error.response?.statusText ??
            error.message ??
            'Erro desconhecido na requisição'
        return Promise.reject(new Error(message))
    },
)
