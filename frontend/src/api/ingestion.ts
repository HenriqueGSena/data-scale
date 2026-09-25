import { axiosClient } from './axiosClient'
import type {
    CursorPage,
    FinancialTransactionResponse,
    IngestionProgress,
    IngestionStatusResponse,
    UploadResponse
} from "../types/type.ts";

export function uploadFile(file: File): Promise<UploadResponse> {
    const formData = new FormData()
    formData.append('file', file)
    return axiosClient.post<UploadResponse>('api/upload', formData).then((res) => res.data)
}

export function getIngestionStatus(jobId: string): Promise<IngestionStatusResponse> {
    return axiosClient.get<IngestionStatusResponse>(`api/ingestion/${jobId}`).then((res) => res.data)
}

/**
 * EventSource não passa pelo axios (não é baseado em XHR/fetch, é um protocolo à parte).
 * Reaproveita o baseURL do axiosClient como única fonte de verdade da URL da API,
 * em vez de ler a env var de novo aqui.
 */
export function subscribeToProgress(
    jobId: string,
    onMessage: (progress: IngestionProgress) => void,
    onError?: (event: Event) => void,
): EventSource {
    const baseUrl = axiosClient.defaults.baseURL ?? ''
    const source = new EventSource(`${baseUrl}api/ingestion/${jobId}/progress`)

    source.addEventListener('progress', (event) => {
        const data = JSON.parse((event as MessageEvent).data) as IngestionProgress
        onMessage(data)
    })

    source.onerror = (event) => {
        onError?.(event)
    }

    return source
}

export function getFinancialTransactions(
    cursor?: string,
    size = 50,
): Promise<CursorPage<FinancialTransactionResponse>> {
    return axiosClient
        .get<CursorPage<FinancialTransactionResponse>>('api/transactions', {
            params: { cursor, size },
        })
        .then((res) => res.data)
}
