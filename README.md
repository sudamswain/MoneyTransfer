# MoneyTransfer
transfer curl details
curl --location 'http://localhost:8080/v1/accounts/transfer' \
--header 'Content-Type: application/json' \
--data '{
    "fromAccountId": "001",
    "toAccountId": "002",
    "amount":234
}'

Note:- before transfer need to add from and to account no.
This maven project.