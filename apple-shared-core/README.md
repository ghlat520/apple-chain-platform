# @apple/shared-core

Framework-agnostic shared layer for the apple-chain-platform frontend ecosystem.

## Philosophy

This package contains **pure TypeScript** — no Vue, no React, no mini-program SDK. It is consumed by:

- `apple-h5-ui/` (Vue 3 + Vant, mobile H5 for consumers/farmers/drivers)
- `apple-miniapp/` (future WeChat mini-program)
- Any future React / Taro / uni-app port

## Contents

- `src/api/` — Axios-based HTTP client + typed API clients (planting / trace / logistics / auth)
- `src/types/` — Type definitions hand-copied from backend Entity classes (IRON RULE: never guess)
- `src/composables/` — Pure functions (formatters, validators) that any framework can wrap

## Field-mapping IRON RULE

Every type in `src/types/*.ts` must be traceable to a specific backend class:

- `Orchard` → `apple-module-planting/entity/Orchard.java`
- `GrowthRecord` → `apple-module-planting/entity/GrowthRecord.java`
- `TraceBatch` → `apple-module-trace/entity/TraceBatch.java`
- `Delivery` → `apple-module-coldchain/entity/Delivery.java`

**Do not add fields that do not exist in the backend.**

## Adapter pattern for mini-program

`src/api/request.ts` assumes `axios` (browser). For a WeChat mini-program port, implement a thin adapter that fulfils the same `HttpClient` interface using `wx.request`, and inject it via `setHttpClient()`.
