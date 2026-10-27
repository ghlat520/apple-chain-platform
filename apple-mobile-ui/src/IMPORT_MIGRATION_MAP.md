# Import Migration Map — @/api/* to @apple/shared-core

## Old -> New

| Old Path | Imported Symbol | New Path | Status |
|----------|----------------|----------|--------|
| @/api/auth.js | authApi | @apple/shared-core (authApi namespace) | Signature mismatch: local=object methods, shared-core=individual functions |
| @/api/auth.js | userApi | NOT in shared-core | MISSING in shared-core |
| @/api/auth.js | rbacApi | NOT in shared-core | MISSING in shared-core |
| @/api/bigdata.js | assetApi | NOT in shared-core | MISSING (shared-core has only generic bigdataApi skeleton) |
| @/api/bigdata.js | sourceApi | NOT in shared-core | MISSING |
| @/api/bigdata.js | dqApi | NOT in shared-core | MISSING |
| @/api/bigdata.js | jobApi | NOT in shared-core | MISSING |
| @/api/bigdata.js | lineageApi | NOT in shared-core | MISSING |
| @/api/bigdata.js | metricApi | NOT in shared-core | MISSING |
| @/api/bigdata.js | reportApi | NOT in shared-core | MISSING |
| @/api/bigdata.js | auditApi | NOT in shared-core | MISSING |
| @/api/bigdata.js | openApi | NOT in shared-core | MISSING |
| @/api/bigdata.js | screenApi | NOT in shared-core | MISSING |
| @/api/coldchain.js | coldchainApi | @apple/shared-core (coldchainApi namespace) | Signature mismatch: local=object methods, shared-core=individual functions (skeleton) |
| @/api/cultivation.js | cultivationApi | NOT in shared-core | MISSING (no cultivation.ts in shared-core) |
| @/api/dashboard.js | (dashboardApi) | NOT in shared-core (separate module) | Not imported in any Vue file currently |
| @/api/farm.js | farmApi | NOT in shared-core | MISSING (no farm.ts in shared-core) |
| @/api/finance.js | financeApi | @apple/shared-core (financeApi namespace) | Signature mismatch: local=object methods, shared-core=individual functions (skeleton) |
| @/api/input.js | inputApi | @apple/shared-core (inputApi namespace) | Signature mismatch: local=object methods, shared-core=individual functions (skeleton) |
| @/api/iot.js | (individual functions) | @apple/shared-core (iotApi namespace) | Signature mismatch: local=individual exports, shared-core=individual functions (skeleton) |
| @/api/maturity.js | maturityApi | NOT in shared-core | MISSING (no maturity.ts in shared-core) |
| @/api/orchardGeo.js | orchardGeoApi, amapConfigApi | NOT in shared-core | MISSING (no orchardGeo.ts in shared-core) |
| @/api/planting.js | plantingApi | @apple/shared-core (plantingApi namespace) | Signature mismatch: local=object methods, shared-core=individual functions |
| @/api/request.js | request (default), downloadFile | @apple/shared-core (configureApi, rawClient, httpGet, etc.) | Structural change: no default export in shared-core |
| @/api/stats.js | statsApi | NOT in shared-core | MISSING (no stats.ts in shared-core) |
| @/api/trace.js | traceApi | @apple/shared-core (traceApi namespace) | Signature mismatch: local=object methods, shared-core=individual functions |
| @/api/trade.js | tradeApi + individual functions | @apple/shared-core (tradeApi namespace) | Signature mismatch: local=object methods+individual exports, shared-core=individual functions (skeleton) |
| @/api/warehouse.js | warehouseApi | @apple/shared-core (warehouseApi namespace) | Signature mismatch: local=object methods, shared-core=individual functions (skeleton) |

## Statistics
- Affected files: 65 Vue/JS files
- Affected import statements: 65
- Missing in shared-core (no corresponding module): userApi, rbacApi, cultivationApi, farmApi, maturityApi, orchardGeoApi, amapConfigApi, statsApi, assetApi, sourceApi, dqApi, jobApi, lineageApi, metricApi, reportApi, auditApi, openApi, screenApi
- Signature mismatch (namespace object vs individual functions): authApi, coldchainApi, financeApi, inputApi, iotApi, plantingApi, traceApi, tradeApi, warehouseApi

## Import Pattern Change

Local code uses **namespace object pattern**:
```js
// Old
import { authApi } from '@/api/auth.js'
authApi.login(data)        // method call on object
authApi.getMe()            // method call on object
```

Shared-core uses **individual function exports** re-exported as namespace:
```js
// New (if shared-core had full coverage)
import { authApi } from '@apple/shared-core'
authApi.login(data)        // still works via namespace re-export
authApi.getCurrentUser()   // different method name!
```

## Critical Gaps for shared-core Agent

1. **Missing modules**: cultivation, farm, maturity, orchardGeo, stats (5 modules entirely absent)
2. **Missing sub-exports in bigdata**: assetApi, sourceApi, dqApi, jobApi, lineageApi, metricApi, reportApi, auditApi, openApi, screenApi (10 sub-namespaces not exported)
3. **Missing auth sub-exports**: userApi, rbacApi (admin user/role management)
4. **Method name mismatches** in existing modules (e.g., `getMe` vs `getCurrentUser`, `getProfile` vs nothing)
5. **Missing downloadFile utility**: local request.js exports downloadFile, shared-core does not
6. **Skeleton-only modules**: bigdata, coldchain, finance, input, iot, trade, warehouse have only generic CRUD, not the actual domain-specific endpoints
