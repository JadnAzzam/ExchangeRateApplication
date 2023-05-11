import "./App.css";
import { useState, useCallback } from "react";
import { useEffect } from "react";
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  Snackbar,
  Alert,
  InputLabel,
  TextField,
  Select,
  MenuItem,
} from "@mui/material";
import { DataGrid } from "@mui/x-data-grid";
import UserCredentialsDialog from "./UserCredentialsDialog/UserCredentialsDialog";
import { getUserToken, saveUserToken, clearUserToken } from "./localStorage";
import {
  LineChart,
  ResponsiveContainer,
  Legend,
  Tooltip,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Label,
} from "recharts";

var SERVER_URL = "http://127.0.0.1:5000";

const States = {
  PENDING: "PENDING",
  USER_CREATION: "USER_CREATION",
  USER_LOG_IN: "USER_LOG_IN",
  USER_AUTHENTICATED: "USER_AUTHENTICATED",
};

function App() {
  let [userToken, setUserToken] = useState(getUserToken());
  let [userInformation, setUserInformation] = useState(false);
  let [authState, setAuthState] = useState(States.PENDING);

  let [buyUsdRate, setBuyUsdRate] = useState(null);
  let [sellUsdRate, setSellUsdRate] = useState(null);
  let [lbpInput, setLbpInput] = useState("");
  let [usdInput, setUsdInput] = useState("");
  let [transactionType, setTransactionType] = useState("usd-to-lbp");

  let [lbpCalcInp, setLbpCalcInput] = useState("");
  let [usdCalcInp, setUsdCalcInput] = useState("");
  let [transTypeCalc, settransTypeCalc] = useState("usd-to-lbp");
  let [userTransactions, setUserTransactions] = useState([]);

  let [pendingSentUserTransactions, setPendingSentUserTransactions] = useState(
    []
  );
  let [pendingReceivedUserTransactions, setPendingReceivedUserTransactions] =
    useState([]);
  let [acceptedUserTransactions, setAcceptedUserTransactions] = useState([]);
  let [rejectedUserTransactions, setRejectedUserTransactions] = useState([]);
  let [pendingID, setPendingID] = useState([]);
  let [selectedPendingID, setSelectedPendingID] = useState("");
  let [selectedPendingState, setSelectedPendingState] = useState("");

  let [userUsdBalance, setUserUsdBalance] = useState("");
  let [userLbpBalance, setUserLbpBalance] = useState("");
  let [sendingReceiverID, setSendingReceiverID] = useState([]);
  let [selectedSendingReceiverID, setSelectedSendingReceiverID] = useState("");
  let [userSentLbpAmount, setUserSentLbpAmount] = useState("");
  let [userSentUsdAmount, setUserSentUsdAmount] = useState("");
  let [userSentUsdToLbp, setUserSentUsdToLbp] = useState("");

  let [graphDataSell, setGraphDataSell] = useState("");
  let [graphDataBuy, setGraphDataBuy] = useState("");
  let [sellMaxRate, setSellMaxRate] = useState(null);
  let [sellMinRate, setSellMinRate] = useState(null);
  let [buyMaxRate, setBuyMaxRate] = useState(null);
  let [buyMinRate, setBuyMinRate] = useState(null);
  let [sellTransactions, setSellTransactions] = useState(null);
  let [buyTransactions, setBuyTransactions] = useState(null);
  let [sellStdDev, setSellStdDev] = useState(null);
  let [buyStdDev, setBuyStdDev] = useState(null);

  function fetchRates() {
    fetch("http://127.0.0.1:5000/exchangeRate")
      .then((response) => response.json())
      .then((data) => {
        console.log(data);
        setBuyUsdRate(data["lbp_to_usd"]);
        setSellUsdRate(data["usd_to_lbp"]);
      });
  }

  useEffect(fetchRates, []);

  function fetchGraphData() {
    fetch("http://127.0.0.1:5000/exchangeRateOverTime")
      .then((response) => response.json())
      .then((data) => {
        console.log(data);
        // Getting sell sata for graph
        const graphDataRawSell = [];
        const rawSellRates = data["usd_to_lbp_ratesOverTime"];
        const rawSellRatesDate = data["usd_to_lbp_ratesOverTime_dates"];
        for (let index = 0; index < rawSellRates.length; index++) {
          graphDataRawSell.push({
            value: rawSellRates[index],
            date: rawSellRatesDate[index],
          });
        }
        setGraphDataSell(graphDataRawSell);

        // Getting buy data for graph
        const graphDataRawBuy = [];
        const rawBuyRates = data["lbp_to_usd_ratesOverTime"];
        const rawBuyRatesDate = data["lbp_to_usd_ratesOverTime_dates"];
        for (let index = 0; index < rawBuyRates.length; index++) {
          graphDataRawBuy.push({
            value: rawBuyRates[index],
            date: rawBuyRatesDate[index],
          });
        }
        setGraphDataBuy(graphDataRawBuy);

        // Setting statistics
        setBuyMaxRate(data["buy_usd_max_rate"]);
        setSellMaxRate(data["sell_usd_max_rate"]);
        setBuyMinRate(data["buy_usd_min_rate"]);
        setSellMinRate(data["sell_usd_min_rate"]);
        setBuyTransactions(data["num_of_buy_usd_transactions"]);
        setSellTransactions(data["num_of_sell_usd_transactions"]);
        setSellStdDev(data["std_dev_sell_usd"]);
        setBuyStdDev(data["std_dev_buy_usd"]);

      });
  }
  useEffect(fetchGraphData, []);

  async function addItem() {
    var data;
    if (lbpInput == "" || usdInput == "") {
      window.alert("Please fill all the required parameters");
    } else {
      if (transactionType == "usd-to-lbp") {
        data = { usd_amount: usdInput, lbp_amount: lbpInput, usd_to_lbp: true };
        console.log(data);
      } else {
        data = {
          usd_amount: usdInput,
          lbp_amount: lbpInput,
          usd_to_lbp: false,
        };
      }
      console.log(userToken);
      await fetch("http://127.0.0.1:5000/transaction", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `bearer ${userToken}`,
        },
        body: JSON.stringify(data),
      })
        .then((response) => response.json())
        .then((data) => {
          console.log("Success:", data);
        })
        .catch((error) => {
          console.error("Error:", error);
        });
      fetchRates();
      setLbpInput("");
      setUsdInput("");
    }
  }

  function login(username, password) {
    return fetch(`${SERVER_URL}/authentication`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        user_name: username,
        password: password,
      }),
    })
      .then((response) => response.json())
      .then((body) => {
        setAuthState(States.USER_AUTHENTICATED);
        setUserToken(body.token);
        saveUserToken(body.token);
      });
  }

  function createUser(username, password) {
    return fetch(`${SERVER_URL}/user`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        user_name: username,
        password: password,
      }),
    }).then((response) => login(username, password));
  }

  function logout() {
    setUserInformation(false);
    setUserToken(null);
    clearUserToken();
  }

  function displayPersonalInformation() {
    setUserInformation(true);
  }
  function displayHomePage() {
    setUserInformation(false);
  }

  const fetchUserTransactions = useCallback(() => {
    fetch(`${SERVER_URL}/transaction`, {
      headers: {
        Authorization: `bearer ${userToken}`,
      },
    })
      .then((response) => response.json())
      .then((transactions) => setUserTransactions(transactions));
  }, [userToken]);
  useEffect(() => {
    if (userToken) {
      fetchUserTransactions();
    }
  }, [fetchUserTransactions, userToken]);

  const fetchUserBalance = useCallback(() => {
    fetch(`${SERVER_URL}/userBalance`, {
      headers: {
        Authorization: `bearer ${userToken}`,
      },
    })
      .then((response) => response.json())
      .then((data) => {
        setUserLbpBalance(data["user_lbp_amount_owned"]);
        setUserUsdBalance(data["user_usd_amount_owned"]);
      });
  }, [userToken]);
  useEffect(() => {
    if (userToken) {
      fetchUserBalance();
    }
  }, [fetchUserBalance, userToken]);

  const fetchUserTransactionRequests = useCallback(() => {
    fetch(`${SERVER_URL}/fetchTransactionRequests`, {
      headers: {
        Authorization: `bearer ${userToken}`,
      },
    })
      .then((response) => response.json())
      .then((data) => {
        console.log("Success:", data);
        //Get ID of pending requests
        const pendingID = [];
        const allPendingRequests = data["pendingReceivedRequests"];
        for (let index = 0; index < allPendingRequests.length; index++) {
          pendingID.push(allPendingRequests[index]["id"]);
        }

        setPendingID(pendingID);
        setAcceptedUserTransactions(data["acceptedSentRequests"]);
        setRejectedUserTransactions(data["rejectedSentRequests"]);
        setPendingSentUserTransactions(data["pendingSentRequests"]);
        setPendingReceivedUserTransactions(data["pendingReceivedRequests"]);
      });
  }, [userToken]);
  useEffect(() => {
    if (userToken) {
      fetchUserTransactionRequests();
    }
  }, [fetchUserTransactionRequests, userToken]);

  function respondToRequest() {
    console.log(selectedPendingID);
    return fetch(`${SERVER_URL}/respondToTransactionRequest`, {
      method: "POST",
      headers: {
        Authorization: `bearer ${userToken}`,
      },
      body: JSON.stringify({
        accepted: selectedPendingState,
        transaction_id: selectedPendingID,
      }),
    })
      .then((response) => response.json())
      .then((data) => {
        setUserLbpBalance(data["receiver_new_lbp_owned"]);
        setUserUsdBalance(data["receiver_new_usd_owned"]);
      });
  }

  function postRequest() {
    return fetch(`${SERVER_URL}/postTransactionRequest`, {
      method: "POST",
      headers: {
        Authorization: `bearer ${userToken}`,
      },
      body: JSON.stringify({
        receiver_id: selectedSendingReceiverID,
        usd_amount: userSentUsdAmount,
        lbp_amount: userSentLbpAmount,
        usd_to_lbp: userSentUsdToLbp,
      }),
    })
      .then((response) => response.json())
      .then((data) => {
        setUserSentUsdAmount("");
        setUserSentLbpAmount("");
        console.log("success post");
      });
      
  }

  const fetchAllUserID = useCallback(() => {
    {
      fetch("http://127.0.0.1:5000/allUserIDs", {
        headers: {
          Authorization: `bearer ${userToken}`,
        },
      })
        .then((response) => response.json())
        .then((data) => {
          console.log(data);
          setSendingReceiverID(data);
        });
    }
  }, [userToken]);
  useEffect(() => {
    if (userToken) {
      fetchAllUserID();
    }
  }, [fetchAllUserID, userToken]);

  return (
    <div className="App">
      <title>LBP Exchange Tracker</title>

      <AppBar position="static" style={{ background: "#0093d5" }}>
        <Toolbar classes={{ root: "nav" }}>
          <Typography variant="h5" className="wrapper">
            Exchange Rate{" "}
          </Typography>

          <div>
            {userToken !== null ? (
              <div>
                {userInformation == true ? (
                  <Button color="inherit" onClick={displayHomePage}>
                    Home Page
                  </Button>
                ) : (
                  <Button color="inherit" onClick={displayPersonalInformation}>
                    Personal Information
                  </Button>
                )}

                <Button color="inherit" onClick={logout}>
                  Logout
                </Button>
              </div>
            ) : (
              <div>
                <Button
                  color="inherit"
                  onClick={() => setAuthState(States.USER_CREATION)}
                >
                  Register
                </Button>
                <Button
                  color="inherit"
                  onClick={() => setAuthState(States.USER_LOG_IN)}
                >
                  Login
                </Button>
              </div>
            )}
          </div>
        </Toolbar>
      </AppBar>

      <Snackbar
        elevation={6}
        variant="filled"
        open={authState === States.USER_AUTHENTICATED}
        autoHideDuration={2000}
        onClose={() => setAuthState(States.PENDING)}
      >
        <Alert severity="success">Success</Alert>
      </Snackbar>

      <div>
        {!userInformation && (
          <div>
            <div className="wrapper">
              <h1>Today's Exchange Rate</h1>
              <p>LBP to USD Exchange Rate</p>
              <h3>
                Buy USD: <span id="buy-usd-rate">{buyUsdRate}</span>
              </h3>
              <h3>
                Sell USD: <span id="sell-usd-rate">{sellUsdRate}</span>
              </h3>

              <hr />

              <h1>Calculator</h1>
              <div className="calculator">
                <div className="amount-input">
                  <label htmlFor="lbp-amount">LBP Amount</label>
                  <TextField
                    className="calculator-input"
                    id="calc-lbp"
                    variant="filled"
                    type="number"
                    min="0"
                    value={lbpCalcInp}
                    onChange={(val) => {
                      if (val.target.value<0)
                        {alert("number cannot be negative!");
                        val.target.value=0;}
                      if (transTypeCalc === "usd-to-lbp") {
                        setLbpCalcInput(val.target.value);
                        setUsdCalcInput(val.target.value / sellUsdRate);
                      } else {
                        setLbpCalcInput(val.target.value);
                        setUsdCalcInput(val.target.value / buyUsdRate);
                      }
                    }}
                  />
                </div>

                <div className="amount-input">
                  <label htmlFor="usd-amount">USD Amount</label>
                  <TextField
                    id="calc-usd"
                    className="calculator-input"
                    variant="filled"
                    type="number"
                    min="0"
                    value={usdCalcInp}
                    onChange={(val) => {
                      if (val.target.value<0)
                        {alert("number cannot be negative!");
                        val.target.value=0;}
                      if (transTypeCalc === "usd-to-lbp") {
                        setUsdCalcInput(val.target.value);
                        setLbpCalcInput(val.target.value * sellUsdRate);
                      } else {
                        setUsdCalcInput(val.target.value);
                        setLbpCalcInput(val.target.value * buyUsdRate);
                      }
                    }}
                  />
                </div>

                <Select
                  className="menu-item"
                  onChange={(val) => settransTypeCalc(val.target.value)}
                  value={transTypeCalc}
                >
                  <MenuItem value="usd-to-lbp">USD to LBP</MenuItem>
                  <MenuItem value="lbp-to-usd">LBP to USD</MenuItem>
                </Select>
              </div>
            </div>

            <div className="wrapper">
              <h1>Record a recent transaction</h1>
              <form name="transaction-entry">
                <div className="amount-input">
                  <label htmlFor="lbp-amount">LBP Amount</label>
                  <input
                    id="lbp-amount"
                    type="number"
                    min="0"
                    value={lbpInput}
                    onChange={(e) => {
                      if (e.target.value<0)
                        {alert("number cannot be negative!");
                        e.target.value=0;}
                      setLbpInput(e.target.value)
                    }}
                  />
                </div>

                <div className="amount-input">
                  <label htmlFor="usd-amount">USD Amount</label>
                  <input
                    id="usd-amount"
                    type="number"
                    min="0"
                    value={usdInput}
                    onChange={(e) => {
                      if (e.target.value<0)
                        {alert("number cannot be negative!");
                          e.target.value=0;}
                      setUsdInput(e.target.value)
                    
                    }}
                  />
                </div>

                <Select
                  id="transaction-type"
                  className="menu-item"
                  value={transactionType}
                  onChange={(val) => setTransactionType(val.target.value)}
                >
                  <MenuItem value="usd-to-lbp">USD to LBP</MenuItem>
                  <MenuItem value="lbp-to-usd">LBP to USD</MenuItem>
                </Select>
                <br></br>
                <button
                  id="add-button"
                  className="button"
                  type="button"
                  onClick={addItem}
                >
                  Add
                </button>
              </form>
            </div>
          </div>
        )}
      </div>

      <div>
        {userToken && userInformation && (
          <div>
            <div className="wrapper">
              <h1>Your Balance</h1>
              <hr />
              <Typography variant="h6">
                LBP Balance: {userLbpBalance}
              </Typography>
              <Typography variant="h6">
                USD Balance: {userUsdBalance}
              </Typography>
            </div>
            <div className="wrapper">
              <h1>Send New Transaction</h1>
              <Typography variant="h6">LBP Amount</Typography>
              <input
                id="lbp-amount"
                type="number"
                value={userSentLbpAmount}
                onChange={(e) => {
                  if (e.target.value<0)
                        {alert("number cannot be negative!");
                        e.target.value=0;}
                        setUserSentLbpAmount(e.target.value)}}
              />
              <Typography variant="h6">USD Amount</Typography>
              <input
                id="usd-amount"
                type="number"
                value={userSentUsdAmount}
                onChange={(e) => {if (e.target.value<0)
                  {alert("number cannot be negative!");
                  e.target.value=0;}setUserSentUsdAmount(e.target.value)}}
              />
              <Typography variant="h6">Receiver ID</Typography>
              <Select
                id="sendingReceiverID"
                onChange={(val) =>
                  setSelectedSendingReceiverID(val.target.value)
                }
                value={selectedSendingReceiverID}
              >
                {sendingReceiverID.map((id, index) => (
                  <MenuItem value={sendingReceiverID[index]}>
                    {sendingReceiverID[index]}
                  </MenuItem>
                ))}
              </Select>

              <Typography variant="h6">Transaction Type</Typography>

              <Select
                className="menu-item"
                onChange={(val) => setUserSentUsdToLbp(val.target.value)}
                value={userSentUsdToLbp}
              >
                <MenuItem value="usd-to-lbp">USD to LBP</MenuItem>
                <MenuItem value="lbp-to-usd">LBP to USD</MenuItem>
              </Select>
              <br></br>
              <button
                id="add-button"
                className="button"
                type="button"
                onClick={postRequest}
              >
                Submit
              </button>
            </div>
            <div className="wrapper">
              <h1>Your Transactions</h1>
              <DataGrid
                sx={{ color: "white" }}
                columns={[
                  {
                    field: "added_date",
                    headerName: "Date added",
                    width: 250,
                    headerAlign: "center",
                  },
                  {
                    field: "lbp_amount",
                    headerName: "LBP Amount",
                    flex: 1,
                    headerAlign: "center",
                  },
                  {
                    field: "usd_amount",
                    headerName: "USD Amount",
                    flex: 1,
                    headerAlign: "center",
                  },
                  {
                    field: "usd_to_lbp",
                    headerName: "USD to LBP",
                    flex: 1,
                    headerAlign: "center",
                  },
                ]}
                rows={userTransactions}
                autoHeight
              />
            </div>
            <div className="wrapper">
              <h1>Pending Received Transactions</h1>
              <DataGrid
                sx={{ color: "white" }}
                columns={[
                  {
                    field: "id",
                    headerName: "Transaction",
                    flex: 1,
                    headerAlign: "center",
                  },
                  {
                    field: "lbp_amount",
                    headerName: "LBP Amount",
                    flex: 1,
                    headerAlign: "center",
                  },
                  {
                    field: "receiver_id",
                    headerName: "Your ID",
                    flex: 1,
                    headerAlign: "center",
                  },
                  {
                    field: "sender_id",
                    headerName: "Sender ID",
                    flex: 1,
                    headerAlign: "center",
                  },
                  {
                    field: "usd_amount",
                    headerName: "USD Amount",
                    flex: 1,
                    headerAlign: "center",
                  },
                  {
                    field: "usd_to_lbp",
                    headerName: "USD to LBP",
                    width: 150,
                    headerAlign: "center",
                  },
                ]}
                rows={pendingReceivedUserTransactions}
                autoHeight
              />
              <h1>Select pending transaction</h1>
              <Typography variant="h6">ID:</Typography>
              <Select
                id="selectedPendingID"
                onChange={(val) => setSelectedPendingID(val.target.value)}
                value={selectedPendingID}
              >
                {pendingID.map((id, index) => (
                  <MenuItem value={pendingID[index]}>
                    {pendingID[index]}
                  </MenuItem>
                ))}
              </Select>
              <Typography variant="h6">State:</Typography>
              <Select
                id="selectedPendingState"
                onChange={(val) => setSelectedPendingState(val.target.value)}
                value={selectedPendingState}
              >
                <MenuItem value={"accepted"}>Accepted</MenuItem>
                <MenuItem value={"rejected"}>Rejected</MenuItem>
              </Select>
              <br></br>
              <button
                id="add-button"
                className="button"
                type="button"
                onClick={respondToRequest}
              >
                Submit
              </button>
            </div>
            <div className="wrapper">
              <h1>Pending Sent Transactions</h1>
              <DataGrid
                sx={{ color: "white" }}
                columns={[
                  {
                    field: "id",
                    headerName: "Transaction",
                    flex: 1,
                    headerAlign: "center",
                  },
                  {
                    field: "lbp_amount",
                    headerName: "LBP Amount",
                    flex: 1,
                    headerAlign: "center",
                  },
                  {
                    field: "receiver_id",
                    headerName: "Your ID",
                    flex: 1,
                    headerAlign: "center",
                  },
                  {
                    field: "sender_id",
                    headerName: "Sender ID",
                    flex: 1,
                    headerAlign: "center",
                  },
                  {
                    field: "usd_amount",
                    headerName: "USD Amount",
                    flex: 1,
                    headerAlign: "center",
                  },
                  {
                    field: "usd_to_lbp",
                    headerName: "USD to LBP",
                    width: 150,
                    headerAlign: "center",
                  },
                ]}
                rows={pendingSentUserTransactions}
                autoHeight
              />
            </div>

            <div className="wrapper">
              <h1>Accepted Transactions Notification</h1>
              <DataGrid sx={{ color: "white" }}
                columns={[
                  {
                    field: "exchange_state",
                    headerName: "Exchange State",
                    width: 150,
                  },
                  { field: "id", headerName: "Transaction ID", flex: 1,  headerAlign: "center"},
                  { field: "lbp_amount", headerName: "LBP Amount", flex: 1,  headerAlign: "center", },
                  { field: "receiver_id", headerName: "Your ID", flex: 1,  headerAlign: "center", },
                  { field: "sender_id", headerName: "Sender ID", flex: 1,  headerAlign: "center", },
                  { field: "usd_amount", headerName: "USD Amount", flex: 1,  headerAlign: "center", },
                ]}
                rows={acceptedUserTransactions}
                autoHeight
              />
            </div>
            <div className="wrapper">
              <h1>Rejected Transactions Notification</h1>
              <DataGrid sx={{ color: "white" }}
                columns={[
                  {
                    field: "exchange_state",
                    headerName: "Exchange State",
                    width: 150,
                  },
                  { field: "id", headerName: "Transaction ID", flex: 1,  headerAlign: "center", },
                  { field: "lbp_amount", headerName: "LBP Amount", flex: 1,  headerAlign: "center", },
                  { field: "receiver_id", headerName: "Your ID", flex: 1,  headerAlign: "center", },
                  { field: "sender_id", headerName: "Sender ID", flex: 1,  headerAlign: "center", },
                  { field: "usd_amount", headerName: "USD Amount", flex: 1,  headerAlign: "center", },
                ]}
                rows={rejectedUserTransactions}
                autoHeight
              />
            </div>
          </div>
        )}
      </div>

      <div>
        {!userInformation && (
          <div>
            <div className="wrapper-graph">
              <h1>Sell Rate</h1>
              <ResponsiveContainer width="100%" aspect={3}>
                <LineChart data={graphDataSell} margin={{ right: 300 }}>
                  <CartesianGrid />
                  <XAxis dataKey={"date"} stroke="#fff" />
                  <YAxis stroke="#fff"></YAxis>
                  <Legend />
                  <Tooltip />
                  <Line
                    dataKey={"value"}
                    stroke="rgb(7, 28, 72)"
                    activeDot={{ r: 8 }}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>

            <div className="wrapper-graph">
              <h1>Buy Rate</h1>
              <ResponsiveContainer width="100%" aspect={3}>
                <LineChart data={graphDataBuy} margin={{ right: 300 }}>
                  <CartesianGrid />
                  <XAxis dataKey={"date"} stroke="#fff" />
                  <YAxis stroke="#fff"></YAxis>
                  <Legend />
                  <Tooltip />
                  <Line
                    dataKey={"value"}
                    stroke="rgb(7, 28, 72)"
                    activeDot={{ r: 8 }}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>

            <div className="wrapper-statistics">
              <h1>Statistics And Insights</h1>

              <hr />
              <div className="wrapper-statistics-rows">
                <Typography variant="h6">
                  Buy max rate: <span id="buy-max-rate">{buyMaxRate}</span>
                </Typography>

                <Typography variant="h6">
                  Sell max rate: <span id="sell-max-rate">{sellMaxRate}</span>
                </Typography>
              </div>

              <div className="wrapper-statistics-rows">
                <Typography variant="h6">
                  Buy min rate: <span id="buy-min-rate">{buyMinRate}</span>
                </Typography>
                <Typography variant="h6">
                  Sell min rate: <span id="sell-min-rate">{sellMinRate}</span>
                </Typography>
              </div>

              <div className="wrapper-statistics-rows">
                <Typography variant="h6">
                  Buy transactions:{" "}
                  <span id="buy-transactions">{buyTransactions}</span>
                </Typography>
                <Typography variant="h6">
                  Sell transactions:{" "}
                  <span id="sell-transactions">{sellTransactions}</span>
                </Typography>
              </div>
              
              <div className="wrapper-statistics-rows">
                <Typography variant="h6">
                  Buy Std Deviation:{" "}
                  <span id="buy-std-dev">{Math.round(buyStdDev)}</span>
                </Typography>
                <Typography variant="h6">
                  Sell Std Deviation:{" "}
                  <span id="sell-std-dev">{Math.round(sellStdDev)}</span>
                </Typography>
              </div>

            </div>
          </div>
        )}
      </div>

      <UserCredentialsDialog
        open={
          true ? authState == States.USER_CREATION : authState == States.PENDING
        }
        title="Welcome"
        submitText={"Register"}
        onClose={() => setAuthState(States.PENDING)}
        onSubmit={(username, password) => createUser(username, password)}
      />

      <UserCredentialsDialog
        open={
          true ? authState == States.USER_LOG_IN : authState == States.PENDING
        }
        title="Welcome back!"
        submitText={"Log In"}
        onClose={() => setAuthState(States.PENDING)}
        onSubmit={(username, password) => login(username, password)}
      />
    </div>
  );
}

export default App;
