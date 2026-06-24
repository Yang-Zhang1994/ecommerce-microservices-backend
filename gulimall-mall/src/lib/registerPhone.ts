/** Phone validation for register SMS (matches legacy public/auth/register). */

export type PhoneCountry = 'nanp' | 'china';

export function isValidRegisterMobile(mobile: string, country: PhoneCountry): boolean {
  const s = mobile.trim();
  if (country === 'china') {
    return /^1[3-9]\d{9}$/.test(s);
  }
  let d = s.replace(/\D/g, '');
  if (d.length === 11 && d.startsWith('1')) {
    d = d.slice(1);
  }
  if (d.length !== 10) {
    return false;
  }
  return /^[2-9]\d{2}[2-9]\d{2}\d{4}$/.test(d);
}

export function phonePlaceholder(country: PhoneCountry): string {
  return country === 'china' ? 'e.g. 13800138000' : 'e.g. 8259493401';
}
