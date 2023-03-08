import axios from "axios";
import {useCredentials, useDruidSettingsStore, useMetaInsightsRequest} from "../store";

const API_URL= "http://localhost:5000/api";

export const getDatabases = async (): Promise<string[]> => {
    const {brokerUrl} = useDruidSettingsStore.getState();
    const headers = useCredentials();
    const res = await axios.get(`${API_URL}/databases`, {params: {broker: brokerUrl}, headers: headers});
    return res.data;
}

export const getDatabaseData = async (): Promise<any> => {
    const {brokerUrl} = useDruidSettingsStore.getState();
    const headers = useCredentials();
    const {database} = useMetaInsightsRequest.getState();
    const res = await axios.get(`${API_URL}/database-data`, {params: {broker: brokerUrl, database: database}, headers: headers});
    return res.data;
}

export const getResults = async (query: any): Promise<any> => {
    const {brokerUrl, routerUrl} = useDruidSettingsStore.getState();
    const headers = useCredentials();
    const res = await axios.post(`${API_URL}/get-results`, {query: query, broker: brokerUrl, router: routerUrl}, {headers: headers});
    return res.data;
}