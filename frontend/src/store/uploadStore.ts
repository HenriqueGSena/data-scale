import { create } from 'zustand'
import type {IngestionProgress, IngestionStatus} from "../types/type.ts";


interface UploadState {
    jobId: string | null
    fileName: string | null
    status: IngestionStatus | null
    linesRead: number
    linesProcessed: number
    linesWithError: number
    errorMessage: string | null
    reset: () => void
    startUpload: (jobId: string, fileName: string) => void
    updateProgress: (progress: IngestionProgress) => void
    setError: (message: string) => void
}

export const useUploadStore = create<UploadState>((set) => ({
    jobId: null,
    fileName: null,
    status: null,
    linesRead: 0,
    linesProcessed: 0,
    linesWithError: 0,
    errorMessage: null,

    reset: () =>
        set({
            jobId: null,
            fileName: null,
            status: null,
            linesRead: 0,
            linesProcessed: 0,
            linesWithError: 0,
            errorMessage: null,
        }),

    startUpload: (jobId, fileName) =>
        set({
            jobId,
            fileName,
            status: 'RECEIVED',
            linesRead: 0,
            linesProcessed: 0,
            linesWithError: 0,
            errorMessage: null,
        }),

    updateProgress: (progress) =>
        set({
            status: progress.status,
            linesRead: progress.linesRead,
            linesProcessed: progress.linesProcessed,
            linesWithError: progress.linesWithError,
        }),

    setError: (message) => set({ status: 'FAILED', errorMessage: message }),
}))
