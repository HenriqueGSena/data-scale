#!/usr/bin/env python3
"""
Gera um dataset CSV sintetico de transacoes financeiras para testes de carga
do desafio de ingestao em larga escala.

Exemplos de uso:
    python generate_dataset.py --rows 1000000 --output transacoes_1m.csv
    python generate_dataset.py --rows 5000000 --output transacoes_5m.csv --error-rate 0.002 --seed 42
"""
import argparse
import csv
import random
from datetime import date, timedelta

CATEGORIAS = [
    "alimentacao", "transporte", "saude", "educacao", "lazer",
    "moradia", "vestuario", "servicos", "investimentos", "outros",
]

DATA_INICIO = date(2023, 1, 1)
DATA_FIM = date(2024, 12, 31)
TOTAL_DIAS = (DATA_FIM - DATA_INICIO).days


def data_aleatoria():
    return DATA_INICIO + timedelta(days=random.randint(0, TOTAL_DIAS))


def linha_valida(i):
    return {
        "id": i,
        "data": data_aleatoria().isoformat(),
        "categoria": random.choice(CATEGORIAS),
        "valor": round(random.uniform(5, 15000), 2),
        "descricao": f"transacao {i}",
    }


def linha_invalida(i):
    """Gera erros propositais para testar skip/retry e a tabela de auditoria."""
    tipo_erro = random.choice(
        ["valor_invalido", "data_invalida", "categoria_vazia", "coluna_faltando"]
    )
    linha = linha_valida(i)
    if tipo_erro == "valor_invalido":
        linha["valor"] = "N/A"
    elif tipo_erro == "data_invalida":
        linha["data"] = "31/02/2024"
    elif tipo_erro == "categoria_vazia":
        linha["categoria"] = ""
    elif tipo_erro == "coluna_faltando":
        del linha["descricao"]
    return linha


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--rows", type=int, default=1_000_000, help="quantidade de linhas a gerar")
    parser.add_argument("--output", default="transacoes.csv", help="arquivo CSV de saida")
    parser.add_argument(
        "--error-rate", type=float, default=0.001,
        help="fracao de linhas propositalmente invalidas (0 a 1, default 0.001 = 0.1%%)",
    )
    parser.add_argument("--seed", type=int, default=None, help="semente para reprodutibilidade")
    args = parser.parse_args()

    if args.seed is not None:
        random.seed(args.seed)

    colunas = ["id", "data", "categoria", "valor", "descricao"]

    with open(args.output, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=colunas)
        writer.writeheader()
        for i in range(1, args.rows + 1):
            if random.random() < args.error_rate:
                writer.writerow(linha_invalida(i))
            else:
                writer.writerow(linha_valida(i))
            if i % 100_000 == 0:
                print(f"{i:,} linhas geradas...")

    print(f"Concluido: {args.output} com {args.rows:,} linhas geradas.")


if __name__ == "__main__":
    main()
