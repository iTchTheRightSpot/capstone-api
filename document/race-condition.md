# Document explains how to prevent over selling an item

## Frontend
* On click of continue to payment (UI), make call to server to reserve items temporarily. Note for Flutterwave payment.
* UI to display or to move past checkout page, backend has to respond with status OK.

## Backend (order_reservation)
1. Since payload contains each item sku and user choice of qty, we need to validate payload qty is not greater than
that in db.
2. Temporarily subtract inventory in db with payload qty.
3. Set expiry time (15 mins) and status to pending
4. Now we wait for Flutterwave webhook to update status (note: session in Flutterwave is set to 10 mins)
5. If we receive a webhook saying payment was successful, we update table status to confirmed. Retrieve cart cookie
from webhook and then perform necessary actions like delete user cart, etc. Else
6. If we receive request from Flutterwave and payment status is failed, do nothing
7. If we do not receive webhook from Flutterwave after 15 mins update all ProductSKU inventory
(current inventory + payload_table qty) whose ProductSKU.sku equal payload_table.sku and status is pending 
8. Finally, update current time in UTC is after expiry in payload_table.