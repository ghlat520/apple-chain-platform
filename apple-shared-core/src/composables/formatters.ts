/**
 * Pure formatter functions — safe to use in any framework (Vue / React / mini-program).
 */

/** Format a number as CNY without currency symbol (safe for tabular display). */
export function formatMoney(
  value: number | string | null | undefined,
  digits = 2
): string {
  if (value == null || value === '') return '-';
  const n = typeof value === 'number' ? value : parseFloat(value);
  if (Number.isNaN(n)) return '-';
  return n.toLocaleString('zh-CN', {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits,
  });
}

/** Format a backend LocalDateTime string (yyyy-MM-dd HH:mm:ss) → yyyy-MM-dd HH:mm (UI) */
export function formatDateTime(
  value: string | null | undefined,
  withSeconds = false
): string {
  if (!value) return '-';
  // Backend already gives "yyyy-MM-dd HH:mm:ss" in Asia/Shanghai — trim to UI precision.
  return withSeconds ? value : value.slice(0, 16);
}

/** Format yyyy-MM-dd as-is, null-safe. */
export function formatDate(value: string | null | undefined): string {
  return value ? value.slice(0, 10) : '-';
}

/**
 * Compose a human-readable address.
 * Handles missing pieces so GPS-fail manual entry still renders cleanly.
 */
export function formatAddress(parts: {
  province?: string;
  city?: string;
  district?: string;
  detail?: string;
}): string {
  return [parts.province, parts.city, parts.district, parts.detail]
    .filter((p): p is string => !!p && p.trim() !== '')
    .join(' ');
}

/** Mask a Chinese phone number: 138****0000 */
export function maskPhone(phone: string | null | undefined): string {
  if (!phone) return '-';
  const digits = phone.replace(/\D/g, '');
  if (digits.length !== 11) return phone;
  return `${digits.slice(0, 3)}****${digits.slice(7)}`;
}
