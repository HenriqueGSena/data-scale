import { useEffect, useRef, useState } from 'react'
import { Button } from "../components/ui/button.tsx"
import { Card, CardContent, CardHeader, CardTitle } from "../components/ui/card.tsx"
import { Input } from "../components/ui/input.tsx"
import { Progress } from "../components/ui/progress.tsx"
import {useUploadStore} from "../store/uploadStore.ts";
import {subscribeToProgress, uploadFile} from "../api/ingestion.ts";


export function UploadPage() {
    const [file, setFile] = useState<File | null>(null)
    const [submitting, setSubmitting] = useState(false)
    const eventSourceRef = useRef<EventSource | null>(null)

    const {
        jobId,
        fileName,
        status,
        linesRead,
        linesProcessed,
        linesWithError,
        errorMessage,
        startUpload,
        updateProgress,
        setError,
        reset,
    } = useUploadStore()

    // Fecha a conexão SSE se o usuário sair da tela no meio de um upload.
    useEffect(() => {
        return () => {
            eventSourceRef.current?.close()
        }
    }, [])

    async function handleSubmit() {
        if (!file) return
        setSubmitting(true)
        try {
            const response = await uploadFile(file)
            if (!response.jobId) {
                setError(response.message)
                return
            }
            startUpload(response.jobId, file.name)

            eventSourceRef.current?.close()
            eventSourceRef.current = subscribeToProgress(
                response.jobId,
                (progress) => {
                    updateProgress(progress)
                    if (progress.status === 'COMPLETED' || progress.status === 'FAILED') {
                        eventSourceRef.current?.close()
                    }
                },
                () => {
                    setError('Conexao de progresso perdida.')
                    eventSourceRef.current?.close()
                },
            )
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Falha ao enviar o arquivo.')
        } finally {
            setSubmitting(false)
        }
    }

    const totalConcluido = linesProcessed + linesWithError
    const percent = linesRead > 0 ? Math.min(100, Math.round((totalConcluido / linesRead) * 100)) : 0
    const jobAtivo = status === 'RECEIVED' || status === 'PROCESSING'

    return (
        <Card className="max-w-xl">
            <CardHeader>
                <CardTitle>Upload de transações (CSV)</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
                <Input
                    type="file"
                    accept=".csv"
                    onChange={(e) => setFile(e.target.files?.[0] ?? null)}
                    disabled={submitting || jobAtivo}
                />

                <Button onClick={handleSubmit} disabled={!file || submitting || jobAtivo}>
                    {submitting ? 'Enviando...' : 'Enviar arquivo'}
                </Button>

                {jobId && (
                    <div className="space-y-2">
                        <p className="text-sm text-muted-foreground">
                            {fileName} — status: {status}
                        </p>
                        <Progress value={percent} />
                        <p className="text-xs text-muted-foreground">
                            {linesProcessed.toLocaleString()} processadas · {linesWithError.toLocaleString()} com erro ·{' '}
                            {linesRead.toLocaleString()} lidas
                        </p>
                    </div>
                )}

                {errorMessage && <p className="text-sm text-destructive">{errorMessage}</p>}

                {(status === 'COMPLETED' || status === 'FAILED') && (
                    <Button variant="outline" onClick={reset}>
                        Novo upload
                    </Button>
                )}
            </CardContent>
        </Card>
    )
}
