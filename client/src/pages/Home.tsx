import {Button, List, ListItem, ListItemButton, ListItemIcon, ListItemText, TextField} from "@mui/material";
import {useUserStore} from "../store";
import {ChangeEvent, useState} from "react";
import AddIcon from "@mui/icons-material/Add";
import AssessmentIcon from "@mui/icons-material/Assessment";
import {Link} from "react-router-dom";
import "../styles/_home.scss";

function SignIn() {
    const {isLoggedIn, logIn} = useUserStore();
    const [userData, setUserData] = useState({
        username: "",
        password: ""
    });

    function updateUserData(event: ChangeEvent<HTMLInputElement>) {
        setUserData((state) => ({...state, [event.target.name]: event.target.value}));
    }

    async function submitLogin() {
        await logIn(userData.username, userData.password);
    }

    const latestQueries = [
        {name: "Test for car sales", database: "Car Sales", k: 10, t: 3},
        {name: "Generate carrots", database: "Car Sales", k: 10, t: 3}
    ];

    return (
        <div className="fade-in">
            {
                isLoggedIn
                    ?
                    <div className="flex gap-5 flex-wrap justify-center">
                        {/*<div className="background-color2 py-2 px-4 default-radius" style={{width: "30rem"}}>*/}
                        {/*    <h3 className="m-1">Latest queries</h3>*/}
                        {/*    <List>*/}
                        {/*        {latestQueries.map((latestQuery, key) => {*/}
                        {/*            return (*/}
                        {/*                <ListItem disablePadding key={key} sx={{padding: "0.2rem 0"}}>*/}
                        {/*                    <ListItemButton sx={{borderRadius: "0.5rem"}}>*/}
                        {/*                        <ListItemIcon>*/}
                        {/*                            <AssessmentIcon/>*/}
                        {/*                        </ListItemIcon>*/}
                        {/*                        <ListItemText>*/}
                        {/*                            <span className="block text-xl">{latestQuery.name}</span>*/}
                        {/*                            <span>{latestQuery.database}</span>*/}
                        {/*                        </ListItemText>*/}
                        {/*                    </ListItemButton>*/}
                        {/*                </ListItem>*/}
                        {/*            );*/}
                        {/*        })}*/}
                        {/*    </List>*/}
                        {/*</div>*/}
                        <div className="flex flex-col gap-5">
                            <Link to="/new">
                                <div
                                    className="background-color2 py-8 px-16 default-radius text-center hover-cursor-pointer">
                                    <AddIcon fontSize="large"/>
                                    <h2 className="m-0">Create new</h2>
                                </div>
                            </Link>
                            {/*<div className="background-color2 py-2 px-4 default-radius" style={{width: "20rem"}}>*/}
                            {/*    <h2 className="m-1">Data tables</h2>*/}
                            {/*    <div>*/}
                            {/*        <h3 className="m-0">Car Sales</h3>*/}
                            {/*        <h6 className="m-0">Local druid</h6>*/}
                            {/*    </div>*/}

                            {/*</div>*/}
                        </div>
                    </div>
                    :
                    <>
                        <div className="username-text-field">
                            <div className="my-1.5">
                                <TextField fullWidth id="username" name="username" label="Username" variant="outlined"
                                           onChange={updateUserData}/>
                            </div>

                            <div className="my-1.5">
                            <TextField fullWidth id="password" name="password" label="Password" variant="outlined"
                                       type="password" onChange={updateUserData}/>
                            </div>
                        </div>
                        <div className="flex justify-center gap-5">
                            <Button variant="contained" className="my-1.5" size="large" onClick={submitLogin}>
                                Register
                            </Button>
                            <Button variant="contained" className="my-1.5" size="large" onClick={submitLogin}>
                                Log in
                            </Button>
                        </div>
                    </>
            }
        </div>
    );
}

export default function Home() {
    return (
        <div className="flex items-center flex-col">
            <div className="pt-20 pb-10">
                <h1 className="mb-2 text-6xl select-none">
                    MetaInsight
                </h1>
                <h4 className="m-0 color-2">
                    Extract insightful information from your data
                </h4>
            </div>
            <SignIn/>
        </div>
    )
}
