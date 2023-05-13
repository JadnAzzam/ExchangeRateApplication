### High Level Architecture

#### Frontend

- FXML Files to structure the User Interface
- CSS Stylesheets to style the User Interface

#### Models

9 different models that allow full (RESTful) communication between frontend and backend. These models include a User model to represent current platform users, a Transaction or PeerTransaction model to represent transactions between users.

#### Communication with the Backend

- Retrofit library to turn HTTP API into a Java Interface.
- Managing dependencies (libraries) in using Maven dependencies. This means that we specified in our project which libraries we want to use and they will be downloaded from a remote repository that hosts many such libraries. We used our IDE to help download these libraries and link them to our project.
- Every FXML file has a controller class that dictates how the page behaves on user input.
- Every controller class has methods that make use of the backend API to either POST or GET database information needed in order to respond to a user query, or display information on the current page. 

#### In More Details

- In order to register or login, our code makes use of the following API functions:

  ```java
  @POST("/user")
  Call<User> addUser(@Body User user);
  
  @POST("/authentication")
  Call<Token> authenticate(@Body User user);
  ```


- One more interesting function is the following:

  ```java
  @GET("/exchangeRateOverTime")
  Call<FluctuationStatistics> getExchangeRateOverTime();
  ```

  This is used in order to facilitate the process of graphing the fluctuation of the exchange rate over the past months. 
  

- When it comes to sending transactions between users. The API used is the following:

  ```java
  @POST("/postTransactionRequest")
  Call<Object> transfer(@Body PeerTransaction peer_transaction, @Header("Authorization") String authorization);
  
  @GET("/fetchTransactionRequests")
  Call<TransactionRequests> getTransactionRequests(@Header("Authorization") String authorization);
  
  @POST("/respondToTransactionRequest")
  Call<Object> reply(@Body RequestReply reply, @Header("Authorization") String authorization);
  ```



- Finally, a user can also see his balance. The API used is the following: 

  ```java
  @GET("/userBalance")
  Call<Balance> getBalance(@Header("Authorization") String authorization);
  ```

