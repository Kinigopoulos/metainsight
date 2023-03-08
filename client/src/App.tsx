import "./styles/global.scss";

import Layout from "./components/Layout";
import {BrowserRouter, Route, Routes} from "react-router-dom";
import Home from "./pages/Home";
import New from "./pages/New";
import Results from "./pages/Results";

function App() {

    return (
        <BrowserRouter>
            <Layout>
                <Routes>
                    <Route path="/" element={<Home/>}/>
                    <Route path="/new" element={<New/>}/>
                    <Route path="/results" element={<Results/>}/>
                </Routes>
            </Layout>
        </BrowserRouter>
    )
}

export default App
