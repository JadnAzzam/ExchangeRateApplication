### Front-end Project Structure and General Architecture

- This folder inlcudes an integrated React JSX app frontend with the backend server.
- The website hosted can  read and write data from the central server store.
- Visual Studio Code was used for development and Google Chrome for the testing of the application.

The web front-end displays 2 unique pages: the home page and the personal information page done by setting states depending on
the user token (logged in or guest) and on the personal infprmation token (displays relevant content and hides the rest). The functions displayHomePage() and displayPersonalInformation() handle the transition.

### The Home page functions:

- function fetchRates(): fetches the returned fetch rates from the backend route and displays them appropriately in the react app. Uses the "/exchangeRate" route. 

- function fetchGraphData(): fetches the returned buy and sell graph data from the backend route and graphs them using the recharts library appropriately. The same backend route also returns the statistics and insights displayed. Uses the "/exchangeRateOverTime" route.

- function addItem(): adds a recent user transaction to the database through a backend route with the header containing an authorization token (nullable for guest transactions).  Uses the "/transaction" POST route. 

-functions login(), logout(), and createUser(): handles the login, logout, and register functioonalities via unique user tokens.  Use the "/authentication" and "/user"  routes to log in and register respectively. 

### The Personal Information page functions:

- const fetchUserTransactions: uses callBack when a user logs in to fetch the user's individually done transactions on the website and displays them in a table appropriately.  Uses the "/transagtion" GET route. 

- const fetchUserBalance: uses callBack when a user logs in to fetch the user's USD and LBP balance and displays them appropriately.  Uses the "/userBalance" route.

- const fetchUserTransactionRequests: uses callBack when a user logs in to fetch the user's pending sent transactions, pending received transactions, accepted transactions, and rejected transactions, and displays them in a table appropriately. Uses the "/fetchTransactionRequests" route.

- function respondToRequest: takes in from the user a pendng received transaction ID and a state(accepted or rejected), and uses the appropriate backend route to post it to the database upon submit. Uses the "/respondToTransactionRequest" route.

- function postRequest:  takes in from the user the receiver's ID, usd amount, lbp amount, and usd_to_lbp value, and uses the appropriate backend route to post it to the database upon submit. Uses the "/postTransactionRequest" route.

- const fetchAllUserID: uses callBack when a user logs in to fetch all the registered users' IDs for ease of sending a transaction request and for minimizing errors. Uses the "/allUserIDs" route.


