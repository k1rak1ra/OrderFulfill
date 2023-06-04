# OrderFulfill
An app to help Canadian ecommerce stores that ship products via ChitChats create/pay for shipments, update tracking, and print shipping labels automatically.

# Purpose
ChitChats is an ultra low cost shipping provider for Canadian online businesses - it helps them provide globally competitive shipping rates.
Unfortunately, ChitChats does not seamlessly integrate with popular eCommerce platforms in a user friendly manner.
This app aims to be the "glue" in between several common eCommerce platforms and ChitChats.

# Supported eCommerce platforms
- Shopify
- eBay

# Recommended app usage steps
1. View all pending orders for a store
2. Select a pending order and pack the displayed order contents
3. Enter weight and select package size
4. Select shipping option, shipment is created/paid for and tracking is updated automatically
5. Print shipping label using a Zebra label printer

# Getting set up
Unfortunately, it is currently impossible to build this app as it relies on the open source Gap network library (Hoodies-Network) - release date is still TBD. 

The following environment variables are also required to make eBay work, they can be seen in your eBay developer dashboard:
- EBAY_CLIENT_ID
- EBAY_CLIENT_SECRET
- EBAY_BASE_URL
- EBAY_RU_NAME

# Future expansion
The architecture of this app is inherently modular and platform-agnostic, allowing for easy addition of future eCommerce platforms and shipping providers in the future.

The behaviors for a particular platform are implemented in a `PlatformActions` file that implements either the `ShippingPlatformActions` or `EcomPlatformActions` interface

# License
GPLv3