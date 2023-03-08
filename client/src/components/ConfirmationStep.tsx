import React from "react";
import Button from "@mui/material/Button";
import {useMetaInsightsRequest} from "../store";

export default function ConfirmationStep() {

    const metaInsightsRequest = useMetaInsightsRequest();

    return (
        <>
            <h1>Confirmation</h1>

            <h2 className="mb-0">Database</h2>
            <span className="mt-0 block">{metaInsightsRequest.database}</span>

            <h2 className="mb-0">Dimensions</h2>
            {
                metaInsightsRequest.dimensions.filter(dimension => dimension.categoryType !== "-")
                    .map((dimension, key) => (
                        <span className="m-0 block" key={key}>
                            <span>{dimension.dimensionName}</span>
                            <span>, {dimension.categoryType}</span>
                            {dimension.aggregationFunctions.length > 0 &&
								<span> ({dimension.aggregationFunctions.join(", ")})</span>}
                        </span>
                    ))
            }
        </>
    )
}