import { useQuery } from "@tanstack/react-query";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card.tsx";
import { DataTable } from "@/components/dataTable/data-table.tsx";
import { getFinancialTransactions } from "@/api/ingestion.ts";
import {financialTransactionColumns} from "@/components/dataTable/columns/financial-transaction-columns.tsx";

export function UpdateList() {
    const transactions = useQuery({
        queryKey: ["financial-transactions"],
        queryFn: () => getFinancialTransactions(),
    });

    return (
        <Card className="max-w-full">
            <CardHeader>
                <CardTitle>Lista de transações</CardTitle>
            </CardHeader>
            <CardContent>
                <DataTable
                    columns={financialTransactionColumns}
                    data={transactions.data?.content ?? []}
                    isLoading={transactions.isLoading}
                    emptyMessage={transactions.isError ? "Não foi possível carregar as transações" : undefined}
                    filterPlaceholder="Filtrar transações..."
                />
            </CardContent>
        </Card>
    )
}
