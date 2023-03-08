import React, {useEffect, useRef, useState} from "react";
import {useSearchParams} from "react-router-dom";
import {getResults} from "../services/service";
import {Accordion, AccordionDetails, AccordionSummary, Button} from "@mui/material";
import {ExpandMore, ContentCopy} from "@mui/icons-material";
import {ResponsiveBar} from "@nivo/bar";
import {ResponsiveLine} from "@nivo/line";
import {ResponsivePie} from "@nivo/pie";
import {CopyToClipboard} from "react-copy-to-clipboard";
import * as htmlToImage from "html-to-image";
import {PatternType} from "../store";

interface Result {
    insights: {
        scopes: {
            category: string,
            dataScope: {
                subspace: {
                    dimension: string,
                    value: any
                }[],
                breakdownDimension: string,
                measure: string,
                aggregationFunction: string
            },
            resultSet: {
                label: any,
                value: number
            }[],
            patternTypeResult: {
                evaluates: boolean | null;
                highlight: any | null;
                patternType: string | null;
                score: number | null;
            }
        }[],
        score: number,
        setsSize: number,
        exceptionsSize: number
    }[];
}

const theme = {
    background: 'rgba(34,34,34,0)',
    textColor: '#ffffff',
    grid: {
        line: {
            stroke: '#555555'
        }
    },
    labels: {
        text: {
            fill: "#262626",
            fontSize: "large",
            fontWeight: 700
        }
    },
    axis: {
        domain: {
            line: {
                stroke: '#ffffff'
            }
        },
        ticks: {
            line: {
                stroke: '#ffffff'
            }
        }
    },
    tooltip: {
        container: {
            background: '#000000'
        }
    }
}

function getSubspaceString(subspace: any): string {
    if (subspace.length === 0) {
        return "{*}"
    }
    const subspaceString = subspace.map((filter: any) => `${filter.dimension}: ${filter.value}`).join(", ");
    return `{${subspaceString}}`
}

function getExtendStrategy(scopes: any[]): string {
    const [firstDataScope, secondDataScope] = scopes.map(scope => scope.dataScope);
    if (!firstDataScope || !secondDataScope) {
        return "-";
    }
    if (JSON.stringify(firstDataScope.subspace) !== JSON.stringify(secondDataScope.subspace)) {
        return "Subspace";
    } else if (JSON.stringify(firstDataScope.breakdownDimension) !== JSON.stringify(secondDataScope.breakdownDimension)) {
        return "Dimension";
    } else if (JSON.stringify({measure: firstDataScope.measure, aggregationFunction: firstDataScope.aggregationFunction})
        !== JSON.stringify({measure: secondDataScope.measure, aggregationFunction: secondDataScope.aggregationFunction})) {
        return "Measure";
    }
    return "Unknown";
}

function getChartType(patternType: string | null): "Bar" | "Line" | "Pie" {
    if (patternType === null) {
        return "Bar";
    }
    switch (patternType as PatternType) {
        case "Outstanding Last":
        case "Outstanding First":
        case "Evenness":
        case "Outstanding Top 2":
        case "Outstanding Last 2":
            return "Bar";
        case "Trend":
        case "Outlier":
        case "Seasonality":
        case "Change Point":
        case "Unimodality":
            return "Line";
        case "Attribution":
            return "Pie";
    }
}

const highlightColor = "#ff7300";
const pieHighlightColor = "#ff9e4f";

function getColorFunction(highlight: any, patternType: string | null): any {
    const defaultColor = "#b9b9b9";
    if (!patternType) {
        return () => defaultColor;
    }
    const highColor = "#71da6f";
    const lowColor = "#cc5353";
    switch (patternType as PatternType) {
        case "Outstanding First":
            return ({data}: any) => data.label === highlight ? highColor : defaultColor;
        case "Outstanding Last":
            return ({data}: any) => data.label === highlight ? lowColor : defaultColor;
        case "Outstanding Top 2":
            return ({data}: any) => highlight.includes(data.label) ? highColor : defaultColor;
        case "Outstanding Last 2":
            return ({data}: any) => highlight.includes(data.label) ? lowColor : defaultColor;
        case "Evenness":
            return () => defaultColor;
        case "Trend":
            const color = highlight === "increasing" ? highColor : lowColor;
            return () => color;
        case "Attribution":
            return ({data}: any) => data.label === highlight ? pieHighlightColor : defaultColor;
        default:
            return () => defaultColor;
    }
}

export default function Results() {

    const [searchParams] = useSearchParams();
    const [error, setError] = useState(false);
    const [loading, setLoading] = useState(false);
    const [data, setData] = useState<Result>({insights: []});

    useEffect(() => {
        const queryString = searchParams.get("query") || "{}";
        const query = JSON.parse(queryString);

        if (loading) return;
        setLoading(true);

        console.log(query);
        getResults(query)
            .then(data => {
                console.log(data);

                setLoading(false);
                if (data.success) {
                    setData(data.results);
                } else {
                    setError(true);
                }
            })
            .catch(e => {
                console.log(e);
                setError(true);
            });
    }, []);

    if (error || loading || data.insights.length === 0) {
        const text = error ? "An error occurred. Please check console." :
            (loading ? "Loading..." : "No insights were found!");
        return (
            <div className="flex items-center justify-center" style={{height: "32rem"}}>
                <span className="accent-gray-600" style={{fontSize: "1.5rem"}}>{text}</span>
            </div>
        )
    }

    return (
        <div className="p-3">
            <h2 className="mt-1">Results</h2>
            {
                data.insights.map((insight, key) => {
                    const extendStrategy = getExtendStrategy(insight.scopes);
                    const firstDataScope = insight.scopes[0].dataScope;
                    const firstSubspaceString = getSubspaceString(firstDataScope.subspace);
                    const firstBreakdownDimension = firstDataScope.breakdownDimension;
                    const firstMeasure = firstDataScope.measure;

                    return (
                        <Accordion key={key} TransitionProps={{unmountOnExit: true}}>
                            <AccordionSummary expandIcon={<ExpandMore/>}>
                                <span style={{width: "2.5rem"}}>{key + 1}.</span>
                                <span style={{marginRight: "4rem"}}>Score: {insight.score.toFixed(3)}</span>
                                <span
                                    style={{width: "22rem"}}>{insight.setsSize} patterns belong to a rule - {insight.exceptionsSize} exceptions</span>
                                <span style={{width: "16rem"}}>Extend Strategy: {extendStrategy}</span>
                                <span>from {`{ Subspace: ${firstSubspaceString}, Breakdown: ${firstBreakdownDimension}, Measure: ${firstMeasure} }`}</span>
                            </AccordionSummary>
                            <AccordionDetails className="flex flex-wrap">

                                {
                                    insight.scopes.map((scope, scopeKey) => <AccordionDetailsComponent scope={scope} key={scopeKey} scopeKey={scopeKey}/>)
                                }
                            </AccordionDetails>
                        </Accordion>
                    )
                })
            }
        </div>
    )
}


function AccordionDetailsComponent({scope, scopeKey}: any) {

    const subspaceString = getSubspaceString(scope.dataScope.subspace);
    const breakdownDimension = scope.dataScope.breakdownDimension;
    const measure = `${scope.dataScope.aggregationFunction}(${scope.dataScope.measure})`
    const graphData = scope.resultSet.map((r: any) => ({...r, labelColor: "#de0404"}));
    const patternType = scope.patternTypeResult.patternType;
    const highlight = scope.patternTypeResult.highlight;
    const colorFunction = getColorFunction(highlight, patternType);
    const chartType = getChartType(patternType);

    const chartRef = useRef<any>(<div></div>);
    const handleCopy = () => {
        htmlToImage.toBlob(chartRef.current).then((blob) => {
            if (!blob) {
                return;
            }
            navigator.clipboard
                .write([new ClipboardItem({'image/png': blob})])
                .then();
        });
    }

    return (
        <div key={scopeKey} className="m-1 px-3 py-2 relative"
             style={{border: "1px solid #2e2e2e", borderRadius: "0.5rem"}}>
            <span className="block">Category: {scope.category}</span>
            <span
                className="block">Highlight: {Array.isArray(highlight) ? highlight.join(", ") : highlight}</span>
            <span className="block">Subspace: {subspaceString}</span>
            <span className="block">Measure: {measure}</span>
            <span className="block">Pattern Type: {patternType}</span>
            <div className="absolute top-0 right-0 mt-1 mr-1">
                <CopyToClipboard text="" onCopy={handleCopy}>
                    <Button size="small" style={{backgroundColor: "#545454"}}><ContentCopy/></Button>
                </CopyToClipboard>
            </div>


            <div ref={chartRef} style={{height: "20rem", width: "35rem"}}>
                {
                    chartType === "Bar" &&
					<ResponsiveBar
						data={graphData}
						keys={["value"]}
						margin={{top: 50, right: 60, bottom: 50, left: 60}}
						padding={0.3}
						indexBy="label"
						theme={theme}
						valueScale={{type: "linear"}}
						indexScale={{type: "band", round: true}}
						colors={colorFunction}
						legends={[]}
						role="application"
						motionConfig="slow"
						ariaLabel="Result set"
						axisLeft={{
                            tickSize: 5,
                            tickPadding: 5,
                            tickRotation: 0,
                            legend: measure,
                            legendPosition: "middle",
                            legendOffset: -40
                        }}
						axisBottom={{
                            tickSize: 5,
                            tickPadding: 5,
                            tickRotation: 0,
                            legend: breakdownDimension,
                            legendPosition: "middle",
                            legendOffset: 40
                        }}
						tooltipLabel={(data) => `${data.indexValue}`}
						animate/>
                }
                {
                    chartType === "Line" &&
					<ResponsiveLine
						data={[{
                            id: 1,
                            data: graphData.map((r: any) => ({x: r.label, y: r.value}))
                        }]}
						xScale={{type: 'point'}}
						pointSize={10}
						colors={colorFunction}
						layers={[
                            "grid",
                            "markers",
                            "axes",
                            "areas",
                            "crosshair",
                            "lines",
                            "points",
                            ({points}) => (
                                <g>
                                    {points.map((point, index) => {
                                        if (point.data.x === highlight) {
                                            return (
                                                <circle
                                                    key={index}
                                                    cx={point.x}
                                                    cy={point.y}
                                                    r={6}
                                                    fill={highlightColor}
                                                    strokeWidth={2}
                                                    style={{zIndex: 500}}
                                                />
                                            );
                                        } else {
                                            return null;
                                        }
                                    })}
                                </g>
                            ),
                            "legends",
                        ]}
						margin={{top: 50, right: 60, bottom: 50, left: 60}}
						theme={theme}
						legends={[]}
						role="application"
						motionConfig="slow"
						axisLeft={{
                            tickSize: 5,
                            tickPadding: 5,
                            tickRotation: 0,
                            legend: measure,
                            legendPosition: "middle",
                            legendOffset: -40
                        }}
						axisBottom={{
                            tickSize: 5,
                            tickPadding: 5,
                            tickRotation: 0,
                            legend: breakdownDimension,
                            legendPosition: "middle",
                            legendOffset: 40
                        }}
						animate/>
                }
                {
                    chartType === "Pie" &&
					<ResponsivePie
						data={graphData.map((r: any) => ({id: r.label, ...r}))}
						margin={{top: 50, right: 60, bottom: 50, left: 60}}
						startAngle={-180}
						sortByValue={true}
						cornerRadius={5}
						animate={true}
						theme={theme}
						layers={[
                            "arcLinkLabels",
                            "arcs",
                            "arcLabels",
                            "legends"
                        ]}
						colors={colorFunction}
						activeInnerRadiusOffset={5}
						activeOuterRadiusOffset={5}
						borderWidth={1}
						borderColor={{
                            from: 'color',
                            modifiers: [
                                [
                                    'darker',
                                    0.2
                                ]
                            ]
                        }}
						arcLinkLabelsSkipAngle={12}
						arcLinkLabelsThickness={2}
						arcLinkLabelsTextColor="#ffffff"
					/>
                }
            </div>
        </div>
    )
}