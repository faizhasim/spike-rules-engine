import test from 'ava';
import got from 'got';

const BASE_URL = process.env.API_SERVER? `http://${process.env.API_SERVER}:9000` : 'http://localhost:9000';
console.log(`Using BASE_URL = ${BASE_URL}`);
/**
 echo ------
 curl -v -XPOST http://localhost:9000/customers/UNILEVER/checkout/items/Classic
 curl -v -XPOST http://localhost:9000/customers/UNILEVER/checkout/items/Classic
 curl -v -XPOST http://localhost:9000/customers/UNILEVER/checkout/items/Classic
 curl -v -XPOST http://localhost:9000/customers/UNILEVER/checkout/items/Premium
 curl -v http://localhost:9000/customers/UNILEVER/checkout/items
 curl -v http://localhost:9000/customers/UNILEVER/checkout/summary
 echo ------
 curl -v -XPOST http://localhost:9000/customers/APPLE/checkout/items/Standout
 curl -v -XPOST http://localhost:9000/customers/APPLE/checkout/items/Standout
 curl -v -XPOST http://localhost:9000/customers/APPLE/checkout/items/Standout
 curl -v -XPOST http://localhost:9000/customers/APPLE/checkout/items/Premium
 curl -v http://localhost:9000/customers/APPLE/checkout/items
 curl -v http://localhost:9000/customers/APPLE/checkout/summary
 echo ------
 curl -v -XPOST http://localhost:9000/customers/NIKE/checkout/items/Premium
 curl -v -XPOST http://localhost:9000/customers/NIKE/checkout/items/Premium
 curl -v -XPOST http://localhost:9000/customers/NIKE/checkout/items/Premium
 curl -v -XPOST http://localhost:9000/customers/NIKE/checkout/items/Premium
 curl -v http://localhost:9000/customers/NIKE/checkout/items
 curl -v http://localhost:9000/customers/NIKE/checkout/summary
 */

const addCheckoutItem = async (customerId, itemId) => {
  const response = await got.post(`${BASE_URL}/customers/${customerId}/checkout/items/${itemId}`);
  return +response.body;
}

const listCheckoutItemsByCustomerId = async customerId => {
  const response = await got(`${BASE_URL}/customers/${customerId}/checkout/items`, {json: true});
  return response.body;
}

const checkoutItemsSummaryByCustomerId = async customerId => {
  const response = await got(`${BASE_URL}/customers/${customerId}/checkout/summary`, {json: true});
  return response.body;
}

test(
  `Scenario #1
    Customer: default
    ID added: 'classic', 'standout', 'premium'
    Total Expected: $987.97
  `, async t => {
  try {
    const customerId = 'default';

    const checkoutItemId1 = await addCheckoutItem(customerId, 'Classic');
    const checkoutItemId2 = await addCheckoutItem(customerId, 'Standout');
    const checkoutItemId3 = await addCheckoutItem(customerId, 'Premium');

    const allCheckoutItems = await listCheckoutItemsByCustomerId(customerId);
    const expectedAllCheckoutItems = [
      { id: checkoutItemId1, customerId,productId: 'Classic' },
      { id: checkoutItemId2, customerId,productId: 'Standout' },
      { id: checkoutItemId3, customerId,productId: 'Premium' }
    ];

    t.deepEqual(
      allCheckoutItems,
      expectedAllCheckoutItems,
      "Listing checkout items by a customer should give the expected results"
    )

    const summary = await checkoutItemsSummaryByCustomerId(customerId);
    const expectedSummary = {
      countsByProductId: {
        Classic: 1,
        Standout: 1,
        Premium: 1
      },
      totalPrice: 987.97
    };
    t.deepEqual(
      summary,
      expectedSummary,
      "Summary (counts by product id and total price) on the checkout items for a customer should give the expected results."
    )
  } catch (error) {
    console.log(error.response.body);
  }
})

// test(
// `Scenario #2
//   Customer: UNILEVER
//   ID added: 'classic', 'classic', 'classic', 'premium'
//   Total Expected: $987.97
// `, async t => {
//   try {
//     const customerId = 'UNILEVER';
//
//     const checkoutItemId1 = await addCheckoutItem(customerId, 'Classic');
//     const checkoutItemId2 = await addCheckoutItem(customerId, 'Classic');
//     const checkoutItemId3 = await addCheckoutItem(customerId, 'Classic');
//     const checkoutItemId4 = await addCheckoutItem(customerId, 'Premium');
//
//     const allCheckoutItems = await listCheckoutItemsByCustomerId(customerId);
//     const expectedAllCheckoutItems = [
//       { id: checkoutItemId1, customerId,productId: 'Classic' },
//       { id: checkoutItemId2, customerId,productId: 'Classic' },
//       { id: checkoutItemId3, customerId,productId: 'Classic' },
//       { id: checkoutItemId4, customerId,productId: 'Premium' }
//     ];
//
//     t.deepEqual(
//       allCheckoutItems,
//       expectedAllCheckoutItems,
//       "Listing checkout items by a customer should give the expected results"
//     )
//
//     const summary = await checkoutItemsSummaryByCustomerId(customerId);
//     const expectedSummary = {
//       countsByProductId: {
//         Classic: 3,
//         Premium: 1
//       },
//       totalPrice: 809.97
//     };
//     t.deepEqual(
//       summary,
//       expectedSummary,
//       "Summary (counts by product id and total price) on the checkout items for a customer should give the expected results."
//     )
//   } catch (error) {
//     console.log(error.response.body);
//   }
// })