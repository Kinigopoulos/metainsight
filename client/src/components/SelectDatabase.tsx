import React, {useEffect, useState} from "react";
import {getDatabases} from "../services/service";
import {FormControl, MenuItem, InputLabel, Select, SelectChangeEvent} from "@mui/material";
import {useMetaInsightsRequest} from "../store";

export default function SelectDatabase() {

    const [databases, setDatabases] = useState<string[]>([]);
    const {database, setDatabase} = useMetaInsightsRequest();
    const [selectedDatabase, setSelectedDatabase] = useState<string>("");

    useEffect(() => {
        getDatabases().then(databases => {
            setDatabases(databases);
            if (database && databases.includes(database)) {
                setSelectedDatabase(database);
            }
        });
    }, []);

    function handleChange(e: SelectChangeEvent) {
        setSelectedDatabase(e.target.value);
        setDatabase(e.target.value);
    }

    return (
        <div>
            <FormControl fullWidth>
                <InputLabel id="database-select-label">Choose database</InputLabel>
                <Select
                    labelId="database-select-label"
                    id="database-select"
                    label="Choose database"
                    defaultValue=""
                    value={selectedDatabase}
                    onChange={handleChange}>
                    {databases.map((database: string, key: number) => <MenuItem key={key} value={database}>{database}</MenuItem>)}
                </Select>
            </FormControl>
        </div>
    )

}