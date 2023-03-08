import React, {useState} from "react";
import {Box, Button, ButtonGroup, Chip, Grid, Input, MenuItem, Select, SelectChangeEvent, Slider} from "@mui/material";
import {
    BalancingParameters,
    useMetaInsightsRequest,
    BalancingParameterKeys,
    extenders,
    Extender,
    patternTypes,
    PatternType, PatternTypeResultFunction, patternTypeResultFunctions, patternTypeResultFunctionsMap
} from "../store";


const parameters = [
    {name: "t", min: 0, max: 0.999, step: 0.01, defaultValue: 0.3},
    {name: "k", min: 0, max: 10, step: 1, defaultValue: 3},
    {name: "r", min: 0, max: 10, step: 0.1, defaultValue: 1},
    {name: "g", min: 0, max: 0.999, step: 0.01, defaultValue: 0.1},
];

export default function SelectAdvancedSettings() {

    const {advancedSettings, setAdvancedSettings, desiredTime, setDesiredTime} = useMetaInsightsRequest();
    const [balancingParameters, setBalancingParameters] = useState<BalancingParameters>(advancedSettings.balancingParameters);
    const [selectedExtenders, setSelectedExtenders] = useState<Extender[]>([...extenders]);
    const [selectedPatternTypes, setSelectedPatternTypes] = useState<PatternType[]>([...patternTypes]);
    const [selectedPatternTypeResultFunction, setSelectedPatternTypeResultFunction] = useState<PatternTypeResultFunction>(advancedSettings.patternTypeResultFunction);
    const [selectedPatternTypeResultFunctionValue, setSelectedPatternTypeResultFunctionValue] = useState<number>(advancedSettings.patternTypeResultFunctionValue);

    const handleDesiredTime = (event: React.ChangeEvent<HTMLInputElement>) => {
        const value = Number(event.target.value || 0);
        setDesiredTime(value);
    }

    const handleSliderChange = (event: Event, newValue: number | number[]) => {
        const name = (event.target as HTMLInputElement).name;
        const value = newValue as number;

        const newBalancingParameters = {...balancingParameters, [name]: value};
        setBalancingParameters(newBalancingParameters);
        setAdvancedSettings(newBalancingParameters, undefined, undefined);
    };

    const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const name = event.target.name;
        let value = Number(event.target.value) ?? NaN;
        if (isNaN(value)) {
            value = parameters.find(parameter => parameter.name === name)?.defaultValue || 0;
        }

        const newBalancingParameters = {...balancingParameters, [name]: value};
        setBalancingParameters(newBalancingParameters);
        setAdvancedSettings(newBalancingParameters, undefined, undefined);
    };

    const handleExtenders = (e: SelectChangeEvent<string[]>) => {
        const newExtenders = [...e.target.value] as Extender[];
        setSelectedExtenders(newExtenders);
        setAdvancedSettings(undefined, newExtenders, undefined);
    }

    const handlePatternTypes = (e: SelectChangeEvent<string[]>) => {
        const newPatternTypes = [...e.target.value] as PatternType[];
        setSelectedPatternTypes(newPatternTypes);
        setAdvancedSettings(undefined, undefined, newPatternTypes);
    }

    const handlePatternTypeFunction = (value: string) => {
        const patternTypeResultFunction = value as PatternTypeResultFunction;
        const defaultValue = patternTypeResultFunctionsMap[patternTypeResultFunction].defaultValue;
        setSelectedPatternTypeResultFunction(patternTypeResultFunction);
        setSelectedPatternTypeResultFunctionValue(defaultValue);
        setAdvancedSettings(undefined, undefined, undefined, patternTypeResultFunction, defaultValue);
    }

    const handlePatternTypeFunctionValue = (event: React.ChangeEvent<HTMLInputElement>) => {
        const value = Number(event.target.value || 0);
        setSelectedPatternTypeResultFunctionValue(value);
        setAdvancedSettings(undefined, undefined, undefined, undefined, value);
    }

    return (
        <div className="flex gap-10">
            <div style={{width: "25rem"}}>
                <div>
                    <h3 className="mb-0">Maximum Execution Time (seconds)</h3>
                    <span className="block mb-3">(if set to 0, then no limit gets applied)</span>
                    <Input
                        value={desiredTime}
                        style={{width: "10rem"}}
                        size="small"
                        onChange={handleDesiredTime}
                        inputProps={{
                            min: 0,
                            max: Infinity,
                            type: 'number',
                        }}
                    />
                </div>
                <div>
                    <h3>Balancing parameters</h3>
                    {parameters.map((parameter, key) => {
                        const value = balancingParameters[parameter.name as BalancingParameterKeys];
                        return (
                            <Grid key={key} container spacing={2} alignItems="center" style={{width: "25rem"}}>
                                <Grid item>
                                    {parameter.name}
                                </Grid>
                                <Grid item xs>
                                    <Slider
                                        value={value}
                                        onChange={handleSliderChange}
                                        min={parameter.min}
                                        max={parameter.max}
                                        step={parameter.step}
                                        name={parameter.name}
                                        aria-labelledby="input-slider"
                                    />
                                </Grid>
                                <Grid item>
                                    <Input
                                        value={value}
                                        style={{width: "5rem"}}
                                        size="small"
                                        onChange={handleInputChange}
                                        name={parameter.name}
                                        inputProps={{
                                            min: parameter.min,
                                            max: parameter.max,
                                            step: parameter.step,
                                            type: 'number',
                                            'aria-labelledby': 'input-slider',
                                        }}
                                    />
                                </Grid>
                            </Grid>
                        )
                    })}
                </div>
                <div>
                    <h3 className="mt-2">Pattern Type Result Filter Strategy</h3>
                    <ButtonGroup variant="contained">
                        {patternTypeResultFunctions.map((patternTypeResultFunction, key) => (
                            <Button key={key}
                                    onClick={() => handlePatternTypeFunction(patternTypeResultFunction)}
                                    style={{backgroundColor: patternTypeResultFunction !== selectedPatternTypeResultFunction ? "#383839" : "#5e80fc"}}>
                                {patternTypeResultFunctionsMap[patternTypeResultFunction].name}
                            </Button>
                        ))}
                    </ButtonGroup>
                    <div className="mt-2">
                        {
                            patternTypeResultFunctionsMap[selectedPatternTypeResultFunction].min &&
							<>
								<span>Filter value: </span>
								<Input
									value={selectedPatternTypeResultFunctionValue}
									style={{width: "10rem"}}
									size="small"
									onChange={handlePatternTypeFunctionValue}
									inputProps={{
                                        min: patternTypeResultFunctionsMap[selectedPatternTypeResultFunction].min,
                                        max: patternTypeResultFunctionsMap[selectedPatternTypeResultFunction].max,
                                        type: 'number',
                                    }}
								/>
							</>
                        }
                    </div>
                </div>
            </div>

            <div style={{width: "100%"}}>
                <div>
                    <h3>Extenders</h3>
                    <Select
                        multiple
                        fullWidth
                        displayEmpty
                        value={selectedExtenders as string[]}
                        onChange={handleExtenders}
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
                            <em>Choose extender(s)</em>
                        </MenuItem>
                        {extenders.map((extender, key) => (
                            <MenuItem key={key} value={extender}>
                                {extender}
                            </MenuItem>
                        ))}
                    </Select>
                </div>

                <div>
                    <h3>Pattern Types</h3>
                    <Select
                        multiple
                        fullWidth
                        displayEmpty
                        value={selectedPatternTypes as string[]}
                        onChange={handlePatternTypes}
                        renderValue={(selected) => {
                            if (selected.length === 0) {
                                return (<em>Choose pattern type(s)</em>)
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
                            <em>Choose pattern type(s)</em>
                        </MenuItem>
                        {patternTypes.map((patternType, key) => (
                            <MenuItem key={key} value={patternType}>
                                {patternType}
                            </MenuItem>
                        ))}
                    </Select>
                </div>
            </div>
        </div>
    )
};