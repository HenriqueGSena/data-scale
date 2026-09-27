import { useState } from "react"
import {
    type CellData,
    type ColumnDef,
    createFilteredRowModel,
    createPaginatedRowModel,
    createSortedRowModel,
    columnFilteringFeature,
    flexRender,
    globalFilteringFeature,
    rowPaginationFeature,
    rowSortingFeature,
    tableFeatures,
    useTable,
} from "@tanstack/react-table"

import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { ChevronLeft, ChevronRight, ChevronsUpDown } from "lucide-react"
import { cn } from "cn"

const features = tableFeatures({
    columnFilteringFeature,
    globalFilteringFeature,
    rowSortingFeature,
    rowPaginationFeature,
    filteredRowModel: createFilteredRowModel(),
    sortedRowModel: createSortedRowModel(),
    paginatedRowModel: createPaginatedRowModel(),
})

export type DataTableColumn<TData extends object> = ColumnDef<typeof features, TData, CellData>

export interface DataTableProps<TData extends object> {
    columns: DataTableColumn<TData>[]
    data: TData[]
    filterPlaceholder?: string
    pageSize?: number
    isLoading?: boolean
    emptyMessage?: string
    className?: string
}

export function DataTable<TData extends object>({
    columns,
    data,
    filterPlaceholder = "Filtrar registros...",
    pageSize = 10,
    isLoading = false,
    emptyMessage = "Nenhum registro encontrado",
    className,
}: DataTableProps<TData>) {
    const [globalFilter, setGlobalFilter] = useState("")

    const table = useTable({
        features,
        data,
        columns,
        state: { globalFilter },
        onGlobalFilterChange: setGlobalFilter,
        initialState: {
            pagination: { pageIndex: 0, pageSize },
        },
    })

    return (
        <div className={cn("space-y-4", className)}>
            <div className="flex items-center">
                <Input
                    value={globalFilter}
                    onChange={(event) => setGlobalFilter(event.target.value)}
                    placeholder={filterPlaceholder}
                    className="max-w-sm"
                    aria-label="Filtrar registros"
                />
            </div>

            <div className="rounded-md border">
            <Table>
                <TableHeader>
                    {table.getHeaderGroups().map((headerGroup) => (
                        <TableRow key={headerGroup.id}>
                            {headerGroup.headers.map((header) => (
                                <TableHead key={header.id}>
                                    {header.isPlaceholder
                                        ? null
                                        : (
                                            <div className="flex items-center gap-1">
                                                {flexRender(
                                                    header.column.columnDef.header,
                                                    header.getContext()
                                                )}
                                                {header.column.getCanSort() && (
                                                    <Button
                                                        variant="ghost"
                                                        size="icon-xs"
                                                        onClick={header.column.getToggleSortingHandler()}
                                                        aria-label={`Ordenar por ${header.column.id}`}
                                                    >
                                                        <ChevronsUpDown />
                                                    </Button>
                                                )}
                                            </div>
                                        )}
                                </TableHead>
                            ))}
                        </TableRow>
                    ))}
                </TableHeader>

                <TableBody>
                    {isLoading ? (
                        <TableRow>
                            <TableCell colSpan={columns.length} className="h-24 text-center">
                                Carregando...
                            </TableCell>
                        </TableRow>
                    ) : table.getRowModel().rows.length ? (
                        table.getRowModel().rows.map((row) => (
                            <TableRow key={row.id}>
                                {row.getAllCells().map((cell) => (
                                    <TableCell key={cell.id}>
                                        {flexRender(
                                            cell.column.columnDef.cell,
                                            cell.getContext()
                                        )}
                                    </TableCell>
                                ))}
                            </TableRow>
                        ))
                    ) : (
                        <TableRow>
                            <TableCell
                                colSpan={columns.length}
                                className="h-24 text-center"
                            >
                                {emptyMessage}
                            </TableCell>
                        </TableRow>
                    )}
                </TableBody>
            </Table>
            </div>

            <div className="flex items-center justify-between gap-4 text-sm text-muted-foreground">
                <span>
                    Página {table.state.pagination.pageIndex + 1} de{" "}
                    {Math.max(table.getPageCount(), 1)}
                </span>
                <div className="flex items-center gap-2">
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={() => table.previousPage()}
                        disabled={!table.getCanPreviousPage()}
                    >
                        <ChevronLeft />
                        Anterior
                    </Button>
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={() => table.nextPage()}
                        disabled={!table.getCanNextPage()}
                    >
                        Próxima
                        <ChevronRight />
                    </Button>
                </div>
            </div>
        </div>
    )
}
