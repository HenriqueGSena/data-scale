package br.com.sena.datascale.dto;

import java.util.List;

public record CursorPage<T>(List<T> content, String nextCursor, boolean hasNext) {
}
