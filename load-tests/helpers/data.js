/**
 * Synthetic Test Data Generator for FashionPin Load Testing
 */

// Common search query terms for fashion commerce
export const SEARCH_KEYWORDS = [
  'dress',
  'evening gown',
  'blazer',
  'silk shirt',
  'trousers',
  'leather jacket',
  'handbag',
  'cashmere sweater',
  'boots',
  'sneakers',
  'heels',
  'trench coat',
  'denim',
  'skirt',
  'accessories',
];

// Product categories matching the FashionPin catalog taxonomy
export const CATEGORIES = [
  'WOMEN',
  'MEN',
  'DRESSES',
  'OUTERWEAR',
  'SHOES',
  'BAGS',
  'ACCESSORIES',
  'JEWELRY',
];

// Aesthetic styles supported by Fashion Discovery
export const FASHION_STYLES = [
  'MINIMALIST',
  'OLD_MONEY',
  'STREETWEAR',
  'AVANT_GARDE',
  'QUIET_LUXURY',
  'BOHEMIAN',
  'Y2K',
  'CLASSIC',
];

// Occasions for Discovery feed
export const OCCASIONS = [
  'COCKTAIL',
  'CASUAL_CHIC',
  'GALA',
  'BUSINESS_CASUAL',
  'SUMMER_GETAWAY',
  'DATE_NIGHT',
];

/**
 * Returns a random element from an array
 */
export function randomItem(array) {
  return array[Math.floor(Math.random() * array.length)];
}

/**
 * Returns a random integer between min and max (inclusive)
 */
export function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

/**
 * Generates a unique user payload for testing registration
 */
export function generateUser() {
  const timestamp = Date.now();
  const randomSuffix = Math.floor(Math.random() * 100000);
  return {
    email: `loadtest_${timestamp}_${randomSuffix}@fashionpin-test.com`,
    password: 'Password123!Secure',
    firstName: 'TestUser',
    lastName: `VU_${randomSuffix}`,
  };
}

/**
 * Generates a unique guest session token for anonymous cart operations
 */
export function generateGuestToken() {
  return 'guest-token-' + Math.random().toString(36).substring(2, 15) + '-' + Date.now();
}
