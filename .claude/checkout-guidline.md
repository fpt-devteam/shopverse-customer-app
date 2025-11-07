🧾 Task: Implement Payment Page (Checkout Screen)

Goal:
Build a payment page for the user to confirm order and make payment.

UI Layout Overview

📍 Shipping Address Section (Top)

Display current shipping address (short version, e.g., “123 Le Loi, District 1”).

The address is clickable → opens a modal or new page to insert/update address.

🛍️ Product List Section

List all products being paid for.

Each product row shows:

Product image

Name

Quantity × Unit price

Total per item

💳 Payment Method Section (Static for now)

Hardcode: “Payment Method: Ship (Pay on Delivery)”.

UI should look fixed / non-editable for this version.

🧮 Bill Summary Section

Show breakdown of costs:

Subtotal: sum of all product totals

Shipping Cost: hardcoded 20,000 VND

Total: subtotal + shipping cost

🟢 Buy Button (Bottom)

Fixed at bottom of screen.

Label: “Buy Now” or “Place Order”.

On click → trigger createOrder() API call or navigation to PayOS checkout link (if available).

Logic Notes

Fetch selected cart items from DB or local cart.

Calculate total dynamically.

Address and product data should come from Supabase (e.g., users.address, cart_items).

Payment method is fixed for this phase (no selection).

UI/UX Suggestions

Use clean card-style layout for each section.

Add a light divider between address, product list, and summary.

Keep Buy button sticky at bottom for good UX.