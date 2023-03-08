import React, {useState} from 'react';
import Box from '@mui/material/Box';
import Stepper from '@mui/material/Stepper';
import Step from '@mui/material/Step';
import StepLabel from '@mui/material/StepLabel';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import SelectDatabase from "../components/SelectDatabase";
import SelectDimensions from "../components/SelectDimensions";
import {useMetaInsightsRequest} from "../store";
import SelectAdvancedSettings from "../components/SelectAdvancedSettings";
import ConfirmationStep from "../components/ConfirmationStep";

const steps = {
    "Select database": <SelectDatabase/>,
    "Select dimensions & measures": <SelectDimensions/>,
    "Advanced settings": <SelectAdvancedSettings/>,
    "Confirm": <ConfirmationStep/>
};
const stepKeys = Object.keys(steps);
const stepValues = Object.values(steps);

export default function New() {
    const metaInsightsRequest = useMetaInsightsRequest();
    const disabledConditionsPerStep = [
        !metaInsightsRequest.database,
        !(
            metaInsightsRequest.dimensions.length > 0 &&
            metaInsightsRequest.dimensions.some(dimension => dimension.categoryType === "Numerical" && dimension.aggregationFunctions.length > 0) &&
            metaInsightsRequest.dimensions.filter(dimension => ["Temporal", "Categorical"].includes(dimension.categoryType)).length > 1
        ),
        false
    ];

    const [activeStep, setActiveStep] = useState(0);
    const [skipped, setSkipped] = useState(new Set<number>());

    const isStepOptional = (step: number) => {
        return step === 2;
    };

    const isStepSkipped = (step: number) => {
        return skipped.has(step);
    };

    const handleNext = () => {
        if (activeStep === stepKeys.length - 1) {
            window.location.href = `/results?query=${encodeURIComponent(JSON.stringify(metaInsightsRequest))}`;
            return;
        }
        let newSkipped = skipped;
        if (isStepSkipped(activeStep)) {
            newSkipped = new Set(newSkipped.values());
            newSkipped.delete(activeStep);
        }

        setActiveStep((prevActiveStep) => prevActiveStep + 1);
        setSkipped(newSkipped);
    };

    const handleBack = () => {
        setActiveStep((prevActiveStep) => prevActiveStep - 1);
    };

    const handleSkip = () => {
        if (!isStepOptional(activeStep)) {
            throw new Error("You can't skip a step that isn't optional.");
        }

        setActiveStep((prevActiveStep) => prevActiveStep + 1);
        setSkipped((prevSkipped) => {
            const newSkipped = new Set(prevSkipped.values());
            newSkipped.add(activeStep);
            return newSkipped;
        });
    };

    return (
        <div className="p-5 flex gap-2">
            <Box sx={{width: '20%'}}>
                <Stepper activeStep={activeStep} orientation="vertical">
                    {stepKeys.map((label, index) => {
                        const stepProps: { completed?: boolean } = {};
                        const labelProps: {
                            optional?: React.ReactNode;
                        } = {};
                        if (isStepOptional(index)) {
                            labelProps.optional = (
                                <Typography variant="caption">Optional</Typography>
                            );
                        }
                        if (isStepSkipped(index)) {
                            stepProps.completed = false;
                        }
                        return (
                            <Step key={label} {...stepProps}>
                                <StepLabel {...labelProps}>{label}</StepLabel>
                            </Step>
                        );
                    })}
                </Stepper>
            </Box>
            <Box sx={activeStep === stepKeys.length - 1 ? {width: '30%', minWidth: "30rem"} : {width: '80%'}}>
                <>
                    {stepValues[activeStep]}
                    <Box sx={{display: 'flex', flexDirection: 'row', pt: 2}}>
                        <Button
                            color="inherit"
                            disabled={activeStep === 0}
                            onClick={handleBack}
                            sx={{mr: 1}}
                        >
                            Back
                        </Button>
                        <Box sx={{flex: '1 1 auto'}}/>
                        {isStepOptional(activeStep) && (
                            <Button color="inherit" onClick={handleSkip} sx={{mr: 1}}>
                                Skip
                            </Button>
                        )}
                        <Button onClick={handleNext} disabled={disabledConditionsPerStep[activeStep]}>
                            {activeStep === stepKeys.length - 1 ? 'Finish' : 'Next'}
                        </Button>
                    </Box>
                </>
            </Box>
        </div>
    );
}