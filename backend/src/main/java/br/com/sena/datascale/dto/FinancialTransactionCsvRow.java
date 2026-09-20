package br.com.sena.datascale.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialTransactionCsvRow {

    private String id;
    private String data;
    private String categoria;
    private String valor;
    private String descricao;
}
