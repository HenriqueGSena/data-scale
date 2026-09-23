export type IngestionStatus = 'RECEIVED' | 'PROCESSING' | 'COMPLETED' | 'FAILED'

export interface UploadResponse {
    jobId: string | null
    status: IngestionStatus
    message: string
}

export interface IngestionProgress {
    jobId: string
    status: IngestionStatus
    linesRead: number
    linesProcessed: number
    linesWithError: number
}

export interface IngestionStatusResponse {
    jobId: string
    fileName: string
    status: IngestionStatus
    totalLinesRead: number
    linesProcessed: number
    linesWithError: number
    startedAt: string
    finishedAt: string | null
    errorMessage: string | null
}
