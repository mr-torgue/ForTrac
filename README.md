# ForTrac
ForTrac is a forward traceability solution based on smart contracts and path constraints.
More info in the paper "ForTrac: a Secure Forward Traceability Protocol for Static Supply Chains using Path Constraints and Smart Contracts".

# Components
## Front-end
The front-end consists of an Android app that uses web3j to communicate with the back-end Ethereum blockchain.
It contains the following functionality:
* read and write NFC tags
* create products on the blockchain (by means of a smart contract)
* update a product on the blockchain (by executing the smart contract)

## Back-end
The backend is a Solidity smart contract. For each unique product a smart contract is created.
We use ganache-cli to simulate an Ethereum network.

# Example
There are two main actions in the app. Adding a product and updating a product.

## Create
* Select a participant
* Click create product
* Specify a name and a path, which is a list of participants
* Submit and sign
* App should now display the new product

## Update
* Click on a product
* Select a participant
* Click update
* provide data
* Submit and sign
* App should return success if verify holds, error otherwise

# Installation and Setup
The front-end android App is provided as an APK file. This can be simply installed on a phone by allowing installation from APK files.
The app requires that the phone has NFC capabilities.

The back-end is provided as a Truffle box. This can be simply installed by using Truffle.
We require the server to have ganache-cli installed.
To work properly, you need to open a IP:port so that the client can connect.

Connecting to the back-end can be done by providing the right IP:port combo in the App settings.

Disclaimer: unauthenticated connection, use IP restriction, VPN, or authentication to connect securely.
