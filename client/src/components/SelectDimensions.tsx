import React, {useEffect, useState} from "react";
import {getDatabaseData} from "../services/service";
import {
    Box, Chip,
    MenuItem, Paper,
    Select,
    SelectChangeEvent,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow
} from "@mui/material";
import {
    categoryTypes,
    CategoryType,
    aggregationFunctionTypes,
    AggregationFunctionType,
    Dimension,
    useMetaInsightsRequest
} from "../store";

export default function SelectDimensions() {

    const [rows, setRows] = useState<Dimension[]>([]);
    const {setDimensions} = useMetaInsightsRequest();

    useEffect(() => {
        getDatabaseData().then(columns => {
            const rows = makeRows(columns) || [];
            setRows(rows);
        });
    }, []);

    const columns = [
        {field: "categoryType", headerName: "Category Type"},
        {field: "dimensionName", headerName: "Dimension Name"},
        {field: "aggregationFunctions", headerName: "Aggregation Functions"},
    ];

    function makeRows(columns: string[]) {
        return columns.map((column, key) => {
            return {
                id: key,
                dimensionName: column,
                categoryType: "-" as CategoryType,
                aggregationFunctions: []
            };
        });
    }

    function changeCategoryType(e: SelectChangeEvent) {
        const newRows = rows;
        const row = newRows.find(row => row.dimensionName === e.target.name) as Dimension;
        row.categoryType = e.target.value as CategoryType;
        if (row.categoryType !== "Numerical") {
            row.aggregationFunctions = [];
        }
        setRows([...newRows]);
        setDimensions([...newRows]);
    }

    function changeAggregationFunctions(e: SelectChangeEvent<string[]>) {
        const newRows = rows;
        const row = newRows.find(row => row.dimensionName === e.target.name) as Dimension;
        row.aggregationFunctions = e.target.value as AggregationFunctionType[];
        setRows([...newRows]);
        setDimensions([...newRows]);
    }

    return (
        <div>
            <div style={{width: '100%'}}>
                <TableContainer component={Paper}>
                    <Table sx={{minWidth: 650}}>
                        <TableHead>
                            <TableRow>
                                {columns.map((column, key) =>
                                    <TableCell key={key}>
                                        {column.headerName}
                                    </TableCell>
                                )}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {rows.map((row, key) => (
                                <TableRow key={key} sx={{'&:last-child td, &:last-child th': {border: 0}}}>
                                    <TableCell style={{width: "12rem"}}>
                                        <Select defaultValue="-" style={{width: "12rem"}} value={row.categoryType}
                                                name={row.dimensionName} onChange={changeCategoryType}>
                                            {categoryTypes.map((categoryType: string, key: number) => <MenuItem
                                                key={key} value={categoryType}>{categoryType}</MenuItem>)}
                                        </Select>
                                    </TableCell>
                                    <TableCell>{row.dimensionName}</TableCell>
                                    <TableCell style={{width: "28rem"}}>
                                        {
                                            row.categoryType === "Numerical" &&
											<Select
												multiple
												fullWidth
												displayEmpty
												value={row.aggregationFunctions as string[]}
												name={row.dimensionName}
												onChange={changeAggregationFunctions}
												renderValue={(selected) => {
                                                    if (selected.length === 0) {
                                                        return (<em>Choose aggregation function(s)</em>)
                                                    }
                                                    return (
                                                        <Box sx={{display: 'flex', flexWrap: 'wrap', gap: 0.5}}>
                                                            {selected.map((value) => (
                                                                <Chip key={value} label={value}/>
                                                            ))}
                                                        </Box>
                                                    )
                                                }}>
												<MenuItem disabled value="">
													<em>Choose aggregation function(s)</em>
												</MenuItem>
                                                {aggregationFunctionTypes.map((aggregationFunctionType, key) => (
                                                    <MenuItem key={key} value={aggregationFunctionType}>
                                                        {aggregationFunctionType}
                                                    </MenuItem>
                                                ))}
											</Select>
                                        }
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            </div>
        </div>
    )
}