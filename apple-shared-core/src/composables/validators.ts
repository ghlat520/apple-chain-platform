/**
 * Pure validator functions — no UI dependencies.
 */

/** Chinese mobile phone (11 digits, starts with 1, second digit 3-9). */
export function isValidMobile(phone: string): boolean {
  return /^1[3-9]\d{9}$/.test(phone);
}

/** Trace code format per backend: TB + yyyyMMdd + 4-digit seq (loose). */
export function isValidTraceCode(code: string): boolean {
  return /^[A-Z]{2,4}\d{6,}$/i.test(code.trim());
}

/** GPS coord sanity check — very loose so off-shore logs fail fast. */
export function isValidLngLat(lng: number, lat: number): boolean {
  return lng >= -180 && lng <= 180 && lat >= -90 && lat <= 90;
}
