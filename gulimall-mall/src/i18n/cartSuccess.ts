/**
 * English copy for `/cart/success` (SSR).
 * Replace or extend later if you add real i18n (e.g. next-intl).
 */
export const cartSuccessMessages = {
  titleAdded: 'Added to cart',
  qty: (n: number) => `Quantity: ${n}`,
  viewItem: 'View item details',
  proceedCart: 'Proceed to cart',
  home: 'Home',
  cart: 'Cart',
  searchPlaceholder: 'Phones, computers, digital',
  errInvalidSku: 'Unable to add to cart',
  errInvalidSkuDetail: 'Invalid SKU. Please choose again from the product page.',
  errMissingSku: 'Invalid link',
  errMissingSkuDetail: 'Missing product ID. Please add to cart again from the product page.',
  errVerifyFailed: 'Unable to verify cart',
  errVerifyFailedDetail:
    'Cannot load your cart from the server (network or session). Please use add-to-cart from the product page.',
  errNotInCart: 'Item not in cart',
  errNotInCartDetail:
    'This link does not match your cart (for example, the SKU was edited manually). Use the normal add-to-cart flow.',
  metaTitle: 'Added to cart · GrainMart',
};
