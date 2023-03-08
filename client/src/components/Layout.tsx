import React, {ReactNode} from "react";
import HelpIcon from "@mui/icons-material/Help";
import SettingsIcon from "@mui/icons-material/Settings";
import {ThemeProvider, createTheme} from "@mui/material/styles";
import CssBaseline from "@mui/material/CssBaseline";
import {Link} from "react-router-dom";

const darkTheme = createTheme({
    palette: {
        mode: "dark",
        primary: {
            main: "#5680fe",
        },
        secondary: {
            main: "#f50057",
        }
    },
    components: {
        MuiButton: {
            styleOverrides: {
                root: {
                    backgroundColor: "#4c7fff",
                    color: "#e8e8e8",
                    ":hover": {
                        backgroundColor: "#205efa"
                    }
                }
            }
        }
    }
});



function Header() {

    return (
        <div className="header flex h-14 justify-between items-center px-6">
            <div>
                <span className="headerText select-none ml-2 text-white">
                    <Link to="/">MetaInsight</Link>
                </span>
            </div>
            <div className="flex gap-2.5">
                {/*<div className="hover-cursor-pointer2"><HelpIcon/></div>*/}
                {/*<div className="hover-cursor-pointer2"><SettingsIcon/></div>*/}
            </div>
        </div>
    );
}


export default function Layout({children}: { children: ReactNode; }) {
    return (
        <>
            <ThemeProvider theme={darkTheme}>
                <CssBaseline/>
                <div className="p-2">
                    <Header/>
                </div>
                {children}
            </ThemeProvider>
        </>
    );
}

