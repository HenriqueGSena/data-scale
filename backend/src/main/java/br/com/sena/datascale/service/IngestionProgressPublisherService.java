package br.com.sena.datascale.service;

import br.com.sena.datascale.dto.IngestionProgress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionProgressPublisherService {

    private static final Duration KEY_TTL = Duration.ofHours(1);
    private static final String KEY_PREFIX = "ingestion:progress:";
    private static final String CHANNEL_PREFIX = "ingestion:progress:channel:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Grava o último estado (com TTL, pra não acumular chave pra sempre) e publica no canal.
     * A chave serve pra quem conectar no SSE depois que o job já começou (ou já terminou);
     * o canal serve pra quem já está conectado receber em tempo real.
     */
    public void publish(UUID jobId, IngestionProgress progress) {
        try {
            String json = objectMapper.writeValueAsString(progress);
            redisTemplate.opsForValue().set(progressKey(jobId), json, KEY_TTL);
            redisTemplate.convertAndSend(channel(jobId), json);
        } catch (Exception e) {
            // Falha ao publicar progresso não deve derrubar o Job: o dado real já está
            // gravado no banco (financial_transaction/ingestion_audit), isso aqui é só UX.
            log.warn("Falha ao publicar progresso no Redis para jobId={}", jobId, e);
        }
    }

    public String progressKey(UUID jobId) {
        return KEY_PREFIX + jobId;
    }

    public String channel(UUID jobId) {
        return CHANNEL_PREFIX + jobId;
    }
}
