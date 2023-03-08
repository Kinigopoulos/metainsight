import {create} from "zustand";
import {persist} from "zustand/middleware";

interface UserInterface {
    isLoggedIn: boolean;
    username: string | undefined;
    password: string | undefined;
    logIn: (username: string, password: string) => void;
    logOut: () => void;
}

export const useUserStore = create<UserInterface>()(
    persist(
        (set) => ({
            isLoggedIn: false,
            username: undefined,
            password: undefined,
            logIn: async (username, password) => {
                try {
                    set((() => ({isLoggedIn: true, username: username, password: password})));
                } catch (e) {

                }
            },
            logOut: () => {
                set(() => ({isLoggedIn: false, username: undefined, password: undefined}));
            }
        }),
        {name: "user-info"}
    )
);
export const useCredentials = () => {
    const {username, password} = useUserStore.getState();
    return {username, password};
};

interface DruidSettings {
    brokerUrl: string;
    setBrokerUrl: (brokerUrl: string) => void;
    routerUrl: string;
    setRouterUrl: (routerUrl: string) => void;
}

export const useDruidSettingsStore = create<DruidSettings>()(
    persist(
        (set) => ({
            brokerUrl: "http://localhost:8082",
            setBrokerUrl: (brokerUrl: string) => {
                set(() => ({brokerUrl: brokerUrl}));
            },
            routerUrl: "http://localhost:8888",
            setRouterUrl: (routerUrl: string) => {
                set(() => ({routerUrl: routerUrl}))
            }
        }),
        {name: "druid-settings"}
    )
);

export const categoryTypes = ["-", "Categorical", "Temporal", "Numerical"] as const;
export type CategoryType = typeof categoryTypes[number];
export const aggregationFunctionTypes = ["SUM", "AVG", "COUNT", "MIN", "MAX"] as const;
export type AggregationFunctionType = typeof aggregationFunctionTypes[number];
export interface Dimension {
    id: number;
    dimensionName: string;
    categoryType: CategoryType;
    aggregationFunctions: AggregationFunctionType[];
}

export interface BalancingParameters {
    t: number;
    r: number;
    k: number;
    g: number;
}
export type BalancingParameterKeys = keyof BalancingParameters;
export const extenders = ["SubspaceExtender", "DimensionExtender", "MeasureExtender"] as const;
export type Extender = typeof extenders[number];
export const patternTypes = ["Outstanding First", "Outstanding Last", "Evenness", "Outstanding Top 2", "Outstanding Last 2", "Trend", "Outlier", "Seasonality", "Change Point", "Unimodality", "Attribution"] as const;
export type PatternType = typeof patternTypes[number];
export const patternTypeResultFunctions = ["PatternTypeResultFunctionTop", "PatternTypeResultFunctionThreshold", "PatternTypeResultFunctionAll"] as const;
export type PatternTypeResultFunction = typeof patternTypeResultFunctions[number];
export const patternTypeResultFunctionsMap: Record<PatternTypeResultFunction, any> = {
    PatternTypeResultFunctionTop: {
        name: "TOP SCORES",
        defaultValue: 3,
        min: 1,
        max: patternTypes.length
    },
    PatternTypeResultFunctionThreshold: {
        name: "MINIMUM SCORE",
        defaultValue: 0.5,
        min: 0.01,
        max: 1
    },
    PatternTypeResultFunctionAll: {
        name: "NO FILTERING",
        defaultValue: 3
    }
};
export interface AdvancedSettings {
    balancingParameters: BalancingParameters,
    extenders: Extender[],
    patternTypes: PatternType[],
    patternTypeResultFunction: PatternTypeResultFunction,
    patternTypeResultFunctionValue: number
}

interface MetaInsightsRequest {
    database: string | undefined;
    setDatabase: (database: string) => void;
    dimensions: Dimension[];
    setDimensions: (dimensions: Dimension[]) => void;
    desiredTime: number;
    setDesiredTime: (desiredTime: number) => void;
    advancedSettings: AdvancedSettings;
    setAdvancedSettings: (balancingParameters?: BalancingParameters,
                          extenders?: Extender[],
                          patternTypes?: PatternType[],
                          patternTypeResultFunction?: PatternTypeResultFunction,
                          patternTypeResultFunctionValue?: number) => void;
}

export const useMetaInsightsRequest = create<MetaInsightsRequest>()(
    (set) => ({
        database: undefined,
        setDatabase: (database: string) => set(() => ({database: database})),
        dimensions: [],
        setDimensions: (dimensions: Dimension[]) => set(() => ({dimensions: dimensions.filter(dimension => dimension.categoryType !== "-")})),
        desiredTime: .0,
        setDesiredTime: (desiredTime: number) => set(() => ({desiredTime: desiredTime})),
        advancedSettings: {
            balancingParameters: {
                t: 0.3,
                r: 1,
                k: 3,
                g: 0.1
            },
            extenders: [...extenders],
            patternTypes: [...patternTypes],
            patternTypeResultFunction: "PatternTypeResultFunctionTop",
            patternTypeResultFunctionValue: 3
        },
        setAdvancedSettings: (balancingParameters, extenders, patternTypes, patternTypeResultFunction, patternTypeResultFunctionValue) => (set((previous) => ({
            advancedSettings: {
                balancingParameters: balancingParameters || previous.advancedSettings.balancingParameters,
                extenders: extenders || previous.advancedSettings.extenders,
                patternTypes: patternTypes || previous.advancedSettings.patternTypes,
                patternTypeResultFunction: patternTypeResultFunction || previous.advancedSettings.patternTypeResultFunction,
                patternTypeResultFunctionValue: patternTypeResultFunctionValue || previous.advancedSettings.patternTypeResultFunctionValue
            }
        }))),
    })
);