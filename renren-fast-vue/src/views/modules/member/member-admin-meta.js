/** Admin display maps for ums_member */

export const GENDER_OPTIONS = [
  { value: 0, label: 'Unknown', tagType: 'info' },
  { value: 1, label: 'Male', tagType: 'primary' },
  { value: 2, label: 'Female', tagType: 'danger' }
]

export const MEMBER_SOURCE_OPTIONS = [
  { value: 0, label: 'PC', tagType: 'info' },
  { value: 1, label: 'App', tagType: 'primary' },
  { value: 2, label: 'Social', tagType: 'success' }
]

function lookupByCode (options, value) {
  if (value == null || value === '') return null
  const code = Number(value)
  if (Number.isNaN(code)) return null
  return options.find(o => o.value === code) || null
}

export function formatFromOptions (options, value) {
  const meta = lookupByCode(options, value)
  return meta ? meta.label : (value == null || value === '' ? '' : `Unknown (${value})`)
}

export function tagTypeFromOptions (options, value) {
  const meta = lookupByCode(options, value)
  return meta ? meta.tagType : 'warning'
}

export function formatGender (value) {
  return formatFromOptions(GENDER_OPTIONS, value)
}

export function genderTagType (value) {
  return tagTypeFromOptions(GENDER_OPTIONS, value)
}

export function formatMemberSource (value) {
  return formatFromOptions(MEMBER_SOURCE_OPTIONS, value)
}

export function memberSourceTagType (value) {
  return tagTypeFromOptions(MEMBER_SOURCE_OPTIONS, value)
}

/** Matches MemberServiceImpl.allocDistinctMobile — not a real phone. */
const SYNTHETIC_OAUTH_MOBILE = /^199[0-9a-f]{8}$/i

export function isSyntheticOAuthMobile (mobile) {
  if (mobile == null || mobile === '') return false
  return SYNTHETIC_OAUTH_MOBILE.test(String(mobile).trim())
}

/**
 * @returns {{ text: string, hint: string|null }}
 */
export function formatMemberPhone (mobile) {
  if (mobile == null || mobile === '') {
    return { text: '—', hint: null }
  }
  const raw = String(mobile).trim()
  if (isSyntheticOAuthMobile(raw)) {
    return {
      text: '—',
      hint: 'No phone on file (Google/OAuth sign-in uses an internal placeholder, not a real number).'
    }
  }
  return { text: raw, hint: null }
}
