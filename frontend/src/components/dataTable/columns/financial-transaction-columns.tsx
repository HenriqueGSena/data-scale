import type {DataTableColumn} from "@/components/dataTable/data-table.tsx";
import type {FinancialTransactionResponse} from "@/types/type.ts";

export const financialTransactionColumns: DataTableColumn<FinancialTransactionResponse>[] = [
    {
        accessorKey: "transactionDate",
        header: "Data",
        cell: ({ row }) =>
            new Intl.DateTimeFormat("pt-BR").format(
                new Date(`${row.original.transactionDate}T00:00:00`),
            ),
    },
    { accessorKey: "category", header: "Categoria" },
    {
        accessorKey: "amount",
        header: "Valor",
        cell: ({ row }) =>
            new Intl.NumberFormat("pt-BR", {
                style: "currency",
                currency: "BRL",
            }).format(row.original.amount),
    },
    { accessorKey: "description", header: "Descrição" },
];
