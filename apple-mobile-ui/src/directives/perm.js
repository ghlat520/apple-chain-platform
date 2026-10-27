/**
 * v-perm directive — hides (removes) the bound DOM node when the current user
 * does not own the required permission code(s).
 *
 * Usage:
 *   <van-button v-perm="'trade:write'">Create Order</van-button>
 *   <van-button v-perm="['trade:write','trade:approve']">...</van-button>      // AND (default)
 *   <van-button v-perm:any="['trade:read','trade:write']">...</van-button>    // OR
 *
 * Why remove instead of disable? It mirrors the backend's authoritative model
 * (the API will reject anyway) and prevents leaking the existence of features
 * the user is not entitled to see — see spec § 1.7 acceptance criteria.
 */
import { useAuthStore } from '@/store/auth.js'

function evaluate(binding) {
  const auth = useAuthStore()
  const codes = Array.isArray(binding.value) ? binding.value : [binding.value]
  if (codes.length === 0 || codes.every((c) => !c)) return true
  return binding.arg === 'any' ? auth.hasAnyPerm(codes) : auth.hasAllPerms(codes)
}

function apply(el, binding) {
  if (!evaluate(binding)) {
    if (el.parentNode) {
      el.parentNode.removeChild(el)
    } else {
      // Fallback when parent has not yet been attached.
      el.style.display = 'none'
    }
  }
}

export const permDirective = {
  mounted: apply,
  updated: apply
}

export function installPermDirective(app) {
  app.directive('perm', permDirective)
}
