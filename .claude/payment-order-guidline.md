🧾 Instruction: Implement Payment Flow on Checkout Page
1. Overview

We need to implement a payment flow on the Checkout page.
When the user clicks the “Pay” button, the system should immediately:

Create a new order record in the database.

Insert all products currently in the checkout page into the order_items table, referencing the newly created order.

After the order is created, call the Supabase Edge Function to generate a payment link.

Once a payment link is received, open it so the user can complete the payment process.

2. Database Schema Reference

Orders

CREATE TABLE orders (
order_id        BIGSERIAL PRIMARY KEY,
user_id         BIGINT NOT NULL REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE RESTRICT,
total_discount  NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (total_discount >= 0),
total_price     NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (total_price >= 0),
status          order_status NOT NULL DEFAULT 'pending',
order_date      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
address         TEXT
);

CREATE INDEX idx_orders_user_date ON orders(user_id, order_date DESC);


Order Items

CREATE TABLE order_items (
order_id     BIGINT NOT NULL REFERENCES orders(order_id) ON UPDATE CASCADE ON DELETE CASCADE,
product_id   BIGINT NOT NULL REFERENCES products(product_id) ON UPDATE CASCADE ON DELETE RESTRICT,
discount_id  BIGINT REFERENCES discounts(discount_id) ON UPDATE CASCADE ON DELETE SET NULL,
quantity     INTEGER NOT NULL CHECK (quantity > 0),
unit_price   NUMERIC(18,2) NOT NULL CHECK (unit_price >= 0),
PRIMARY KEY (order_id, product_id)
);

3. Implementation Steps
   Step 1: Create a new order

When the user clicks “Pay” on the Checkout page:

Collect all the cart/checkout information:

user_id

total_price

total_discount

address

Insert a new record into the orders table.

Get the newly created order_id from the response.

Step 2: Insert order items

For each product in the checkout list, insert a row into order_items:

order_id → the ID from Step 1

product_id, quantity, and unit_price from the checkout data

Use Supabase’s insert method with order_id as the foreign key.

Step 3: Create payment link

After the order and order items are successfully created:

Call the following Edge Function via POST request:

https://uehonyhpopuxynbzshyo.supabase.co/functions/v1/create-payment


The request body should contain:

{ "order_id": <created_order_id> }


The API will respond with a JSON object containing:

{ "payment_url": "<payment-link>" }

Step 4: Redirect user to payment

Once the payment_url is received, immediately open it in a browser window or webview so the user can complete the payment.

4. Example Flow (Pseudo-code)
   async function handlePayment() {
   // Step 1: Create order
   const { data: order, error: orderError } = await supabase
   .from('orders')
   .insert({
   user_id: currentUser.id,
   total_price: cartTotal,
   total_discount: cartDiscount,
   address: shippingAddress
   })
   .select('order_id')
   .single();

if (orderError) throw orderError;

// Step 2: Insert order items
const orderItems = cart.map(item => ({
order_id: order.order_id,
product_id: item.id,
quantity: item.quantity,
unit_price: item.price
}));

await supabase.from('order_items').insert(orderItems);

// Step 3: Create payment link
const response = await fetch(
"https://uehonyhpopuxynbzshyo.supabase.co/functions/v1/create-payment",
{
method: "POST",
headers: { "Content-Type": "application/json" },
body: JSON.stringify({ order_id: order.order_id })
}
);

const { payment_url } = await response.json();

// Step 4: Redirect to payment page
window.open(payment_url, "_blank");
}

✅ Expected Outcome

Clicking Pay creates a new order and its related order items.

A payment link is generated through the Supabase Edge Function.

The user is redirected to the payment page to complete the transaction.