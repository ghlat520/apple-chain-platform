#!/usr/bin/env node
/**
 * contract-lint.mjs -- Contract-Frontend consistency checker
 *
 * Validates that endpoints defined in contract/modules/*.md are
 * implemented in apple-web-ui/src/api/*.js (and optionally frontend/src/api/*.js).
 *
 * Modes:
 *   node scripts/contract-lint.mjs              # Full scan (human-readable)
 *   node scripts/contract-lint.mjs --module coldchain  # Single module
 *   node scripts/contract-lint.mjs --ci                 # JSON output, exit 0/1
 *   node scripts/contract-lint.mjs --frontend-only      # Skip contract, only list frontend endpoints
 *
 * Exit codes:
 *   0 = all contract endpoints found in frontend (or no missing)
 *   1 = missing endpoints detected
 */

import { readFileSync, readdirSync, existsSync } from 'fs'
import { resolve, join, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const ROOT = resolve(__dirname, '..')
const CONTRACT_DIR = join(ROOT, 'contract', 'modules')
const API_DIRS = [
  join(ROOT, 'apple-web-ui', 'src', 'api'),
  join(ROOT, 'frontend', 'src', 'api'),
]

// --- CLI args ---
const args = process.argv.slice(2)
const ciMode = args.includes('--ci')
const frontendOnlyMode = args.includes('--frontend-only')
const moduleIdx = args.indexOf('--module')
const targetModule = moduleIdx !== -1 ? args[moduleIdx + 1] : null

// --- Colors (disabled in CI) ---
const c = {
  ok: ciMode ? '' : '\x1b[32m',
  fail: ciMode ? '' : '\x1b[31m',
  warn: ciMode ? '' : '\x1b[33m',
  dim: ciMode ? '' : '\x1b[2m',
  bold: ciMode ? '' : '\x1b[1m',
  reset: ciMode ? '' : '\x1b[0m',
}

// =====================================================================
// 1. Parse contract/modules/*.md  -> extract endpoint table rows
// =====================================================================

/**
 * Parse a single contract .md file and extract all endpoint entries.
 * Looks for markdown table rows matching:
 *   | N | name | `/api/...` | GET | ...
 * Returns array of { method, url, name, section, module }
 */
function parseContractFile(filePath, moduleName) {
  const src = readFileSync(filePath, 'utf-8')
  const endpoints = []
  const lines = src.split('\n')
  let currentSection = ''
  let inTable = false
  let tableHeaderSeen = false
  let skipTable = false

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]

    // Track section headings for location reference
    const hMatch = line.match(/^##+\s+(.+)/)
    if (hMatch) {
      currentSection = hMatch[1].trim()
      inTable = false
      tableHeaderSeen = false
      // Skip tables in architecture/intro sections (section 0 or non-endpoint sections)
      // Endpoints are typically in sections starting with "1." or higher, or "接口索引"
      const isArchSection = /^\d*\.?\s*(架构|说明|简介|概述|待规划|基础设施|错误码|质量|checklist)/i.test(currentSection)
        || /对比|对比表|差异|参考|映射/.test(currentSection)
      skipTable = isArchSection
      continue
    }

    // Detect table start (first data-looking row with | separators)
    if (!inTable && line.match(/^\|.*\|.*\|.*\|/) && !line.match(/^\|[-:\s|]+\|$/)) {
      inTable = true
      tableHeaderSeen = false
      continue  // skip header row
    }

    // Detect table separator (|---|---|) -- marks end of header
    if (inTable && line.match(/^\|[-:\s|]+\|$/)) {
      tableHeaderSeen = true
      continue
    }

    // Detect table end (non-table line)
    if (inTable && !line.match(/^\|/)) {
      inTable = false
      tableHeaderSeen = false
      continue
    }

    // Parse table row (only after separator, i.e., data rows)
    if (inTable && tableHeaderSeen && !skipTable) {
      const cells = line.split('|').slice(1, -1).map((s) => s.trim())
      if (cells.length < 3) continue

      // Find the cell containing URL (starts with /api/ or `/api/`)
      let urlCellIdx = -1
      let methodCellIdx = -1
      let nameCellIdx = 0 // first cell is usually the name or number

      for (let ci = 0; ci < cells.length; ci++) {
        const cell = cells[ci].replace(/`/g, '').trim()
        if (cell.match(/^\/api\//)) {
          urlCellIdx = ci
        }
        if (['GET', 'POST', 'PUT', 'DELETE', 'PATCH'].includes(cell)) {
          methodCellIdx = ci
        }
      }

      if (urlCellIdx === -1) continue

      const rawUrl = cells[urlCellIdx].replace(/`/g, '').trim()

      // Filter: reject URLs that are multi-path references or contain Chinese chars
      // e.g., "/api/user/login、/api/user/logout" is a reference, not a real endpoint
      if (rawUrl.includes('\u3001') || rawUrl.includes('，') || rawUrl.includes('、')) continue
      // Reject URLs that look like examples (e.g., "如 /api/finance/loans/export")
      if (rawUrl.startsWith('\u5982 ')) continue
      // Reject URLs with wildcards (e.g., "/api/user/roles/*")
      if (rawUrl.includes('*')) continue

      const method = methodCellIdx !== -1
        ? cells[methodCellIdx].replace(/`/g, '').trim().toUpperCase()
        : 'GET'
      const name = cells[nameCellIdx].replace(/\[.*?\]\(.*?\)/g, '').trim() || cells[nameCellIdx]

      // Strip query params from URL for display and matching
      const urlWithoutQuery = rawUrl.split('?')[0]

      // Normalize URL: strip path params for matching (e.g., {id} -> :param)
      const normalizedUrl = urlWithoutQuery.replace(/\{[^}]+\}/g, ':param')

      // Extract section number reference (e.g., "planting.md §2.1")
      const sectionRef = currentSection
        ? `${moduleName}.md ${currentSection}`
        : `${moduleName}.md`

      endpoints.push({
        method,
        url: urlWithoutQuery,
        normalizedUrl,
        name,
        section: sectionRef,
        module: moduleName,
      })
    }
  }

  return endpoints
}

/**
 * Scan all contract .md files (or single module) and return endpoints.
 */
function scanContracts() {
  const files = targetModule
    ? [join(CONTRACT_DIR, `${targetModule}.md`)]
    : readdirSync(CONTRACT_DIR).filter((f) => f.endsWith('.md')).map((f) => join(CONTRACT_DIR, f))

  const endpoints = []
  for (const f of files) {
    if (!existsSync(f)) {
      if (targetModule) {
        process.stderr.write(`Contract file not found: ${f}\n`)
        process.exit(1)
      }
      continue
    }
    const moduleName = f.replace(/\.md$/, '').split('/').pop()
    const eps = parseContractFile(f, moduleName)
    endpoints.push(...eps)
  }
  return endpoints
}

// =====================================================================
// 2. Parse apple-web-ui/src/api/*.js -> extract request.* calls
// =====================================================================

/**
 * Extract API endpoints from a JS file.
 * Matches patterns:
 *   request.get('/path', ...)
 *   request.post('/path', ...)
 *   request.put('/path', ...)
 *   request.delete('/path', ...)
 *   downloadFile('/api/path', ...)
 * Also handles template literals: `/path/${id}`
 *
 * Returns array of { method, url, normalizedUrl, name, file }
 */
function parseApiFile(filePath) {
  const src = readFileSync(filePath, 'utf-8')
  const endpoints = []
  const lines = src.split('\n')
  const fileName = filePath.split('/').pop()

  // Match request.get/post/put/delete/patch calls
  // Also match downloadFile calls (always GET semantically)
  const requestRe = /(?:request|downloadFile)\.(get|post|put|delete|patch)\s*\(\s*(?:`([^`]+)`|'([^']+)'|"([^"]+)")/g
  // Match downloadFile separately (it takes the full /api/... path)
  const downloadRe = /downloadFile\s*\(\s*(?:`([^`]+)`|'([^']+)'|"([^"]+)")/g

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]

    // Extract function name from export line (e.g., getOrchards)
    let apiFuncName = ''
    const funcMatch = line.match(/^\s+(?:\w+)\s*:\s*\(([^)]*)\)\s*=>/)
    if (funcMatch) {
      // Look at the line content to guess the name
      apiFuncName = line.trim().split(':')[0].trim()
    }

    // Try request.* pattern
    let match
    while ((match = requestRe.exec(line)) !== null) {
      const method = match[1].toUpperCase()
      const rawUrl = match[2] || match[3] || match[4] || ''
      if (!rawUrl.startsWith('/')) continue

      // For downloadFile, method from request.* is irrelevant; downloadFile is GET
      const effectiveMethod = match[0].includes('downloadFile') ? 'GET' : method

      // Normalize: strip template literal interpolations and query params
      const normalizedUrl = rawUrl
        .split('?')[0]
        .replace(/\$\{[^}]+\}/g, ':param')
        .replace(/\/api\//, '/')  // baseURL is /api, so strip it for matching

      // Full URL (with /api prefix for display)
      const fullUrl = normalizedUrl.startsWith('/api/') ? normalizedUrl : `/api${normalizedUrl}`

      endpoints.push({
        method: effectiveMethod,
        url: fullUrl,
        normalizedUrl,
        rawUrl,
        name: apiFuncName || '',
        file: fileName,
      })
    }

    // Try downloadFile pattern (full /api/... URL)
    while ((match = downloadRe.exec(line)) !== null) {
      const rawUrl = match[1] || match[2] || match[3] || ''
      if (!rawUrl.startsWith('/api/')) continue

      const urlWithoutQuery = rawUrl.split('?')[0]
      const normalizedUrl = urlWithoutQuery.replace(/\$\{[^}]+\}/g, ':param')

      endpoints.push({
        method: 'GET',
        url: urlWithoutQuery,
        normalizedUrl,
        rawUrl,
        name: apiFuncName || '',
        file: fileName,
      })
    }

    // Reset regex lastIndex for next line
    requestRe.lastIndex = 0
    downloadRe.lastIndex = 0
  }

  // Deduplicate by normalizedUrl+method
  const seen = new Set()
  return endpoints.filter((ep) => {
    const key = `${ep.method}::${ep.normalizedUrl}`
    if (seen.has(key)) return false
    seen.add(key)
    return true
  })
}

/**
 * Scan all API directories and return endpoints.
 */
function scanFrontendApis() {
  const endpoints = []
  const seen = new Set()
  for (const dir of API_DIRS) {
    if (!existsSync(dir)) continue
    const files = readdirSync(dir).filter((f) => f.endsWith('.js') && f !== 'request.js')
    for (const f of files) {
      const eps = parseApiFile(join(dir, f))
      for (const ep of eps) {
        const key = `${ep.method}::${ep.normalizedUrl}`
        if (!seen.has(key)) {
          seen.add(key)
          endpoints.push(ep)
        }
      }
    }
  }
  return endpoints
}

// =====================================================================
// 3. Matching logic
// =====================================================================

/**
 * Normalize a contract URL for comparison with frontend URLs.
 * Contract URLs have /api/ prefix and {id} params.
 * Frontend URLs (after stripping /api/) have :param placeholders.
 */
function normalizeForMatch(url) {
  return url
    .split('?')[0]         // strip query params
    .replace(/\/api\//, '/')
    .replace(/\{[^}]+\}/g, ':param')
    .replace(/\/$/, '')
    .toLowerCase()
}

/**
 * Check if a contract endpoint matches any frontend endpoint.
 * Match on: normalized URL path + HTTP method.
 */
function findMatch(contractEp, frontendEndpoints) {
  const contractKey = normalizeForMatch(contractEp.url)

  for (const fep of frontendEndpoints) {
    const frontendKey = normalizeForMatch(fep.url)
    if (contractKey === frontendKey && contractEp.method === fep.method) {
      return fep
    }
  }
  return null
}

/**
 * Check if a frontend endpoint matches any contract endpoint.
 */
function findContractMatch(frontendEp, contractEndpoints) {
  const frontendKey = normalizeForMatch(frontendEp.url)

  for (const cep of contractEndpoints) {
    const contractKey = normalizeForMatch(cep.url)
    if (contractKey === frontendKey && cep.method === frontendEp.method) {
      return cep
    }
  }
  return null
}

// =====================================================================
// 4. Output
// =====================================================================

function humanReport(contractEndpoints, frontendEndpoints) {
  const matched = []
  const missing = []
  const extra = []

  for (const cep of contractEndpoints) {
    const match = findMatch(cep, frontendEndpoints)
    if (match) {
      matched.push({ contract: cep, frontend: match })
    } else {
      missing.push(cep)
    }
  }

  for (const fep of frontendEndpoints) {
    const match = findContractMatch(fep, contractEndpoints)
    if (!match) {
      extra.push(fep)
    }
  }

  const totalContract = contractEndpoints.length
  const totalFrontend = frontendEndpoints.length
  const totalMatched = matched.length
  const totalMissing = missing.length
  const totalExtra = extra.length

  // Header
  console.log(`${c.bold}Contract Lint Report${c.reset}`)
  console.log('====================')
  console.log(`Modules scanned:     ${new Set(contractEndpoints.map((e) => e.module)).size}`)
  console.log(`Endpoints in contracts: ${totalContract}`)
  console.log(`Endpoints in frontend:  ${totalFrontend}`)
  console.log(`Matched:               ${totalMatched}`)
  console.log(`Missing in frontend:   ${totalMissing}`)
  console.log(`Extra in frontend:     ${totalExtra}`)
  console.log()

  // Missing
  if (missing.length > 0) {
    console.log(`${c.fail}Missing (contract has, frontend lacks):${c.reset}`)
    for (const ep of missing) {
      console.log(`  ${c.fail}x${c.reset} ${ep.method.padEnd(7)} ${ep.url}  ${c.dim}-- ${ep.section}${c.reset}`)
    }
    console.log()
  }

  // Extra
  if (extra.length > 0) {
    console.log(`${c.warn}Extra (frontend has, contract lacks):${c.reset}`)
    for (const ep of extra) {
      console.log(`  ${c.warn}?${c.reset} ${ep.method.padEnd(7)} ${ep.url}  ${c.dim}-- ${ep.file}${c.reset}`)
    }
    console.log()
  }

  // Result
  const pass = totalMissing === 0
  if (pass) {
    console.log(`${c.ok}Result: PASS${c.reset} (all contract endpoints implemented)`)
  } else {
    console.log(`${c.fail}Result: FAIL${c.reset} (${totalMissing} missing, ${totalExtra} extra)`)
  }

  return { pass, matched, missing, extra, totalContract, totalFrontend }
}

function ciReport(contractEndpoints, frontendEndpoints) {
  const matched = []
  const missing = []
  const extra = []

  for (const cep of contractEndpoints) {
    const match = findMatch(cep, frontendEndpoints)
    if (match) {
      matched.push({ contract: cep, frontend: match })
    } else {
      missing.push(cep)
    }
  }

  for (const fep of frontendEndpoints) {
    const match = findContractMatch(fep, contractEndpoints)
    if (!match) {
      extra.push(fep)
    }
  }

  const report = {
    modulesScanned: [...new Set(contractEndpoints.map((e) => e.module))].length,
    contractEndpoints: contractEndpoints.length,
    frontendEndpoints: frontendEndpoints.length,
    matched: matched.length,
    missing: missing.map((ep) => ({
      method: ep.method,
      url: ep.url,
      section: ep.section,
      module: ep.module,
    })),
    extra: extra.map((ep) => ({
      method: ep.method,
      url: ep.url,
      file: ep.file,
    })),
    pass: missing.length === 0,
  }

  console.log(JSON.stringify(report, null, 2))
  return report
}

function frontendOnlyReport(frontendEndpoints) {
  console.log(`${c.bold}Frontend API Endpoints${c.reset}`)
  console.log('========================')
  console.log(`Total endpoints: ${frontendEndpoints.length}`)
  console.log()

  // Group by module (directory prefix)
  const grouped = {}
  for (const ep of frontendEndpoints) {
    const parts = ep.url.replace('/api/', '').split('/')
    const module = parts[0] || 'unknown'
    if (!grouped[module]) grouped[module] = []
    grouped[module].push(ep)
  }

  for (const [mod, eps] of Object.entries(grouped).sort()) {
    console.log(`${c.bold}${mod}${c.reset} (${eps.length})`)
    for (const ep of eps) {
      const nameStr = ep.name ? `  ${c.dim}(${ep.name})${c.reset}` : ''
      console.log(`  ${ep.method.padEnd(7)} ${ep.url}${nameStr}`)
    }
    console.log()
  }
}

// =====================================================================
// 5. Main
// =====================================================================

function main() {
  const frontendEndpoints = scanFrontendApis()

  if (frontendOnlyMode) {
    frontendOnlyReport(frontendEndpoints)
    process.exit(0)
  }

  const contractEndpoints = scanContracts()

  if (contractEndpoints.length === 0) {
    console.log('No contract endpoints found. Check contract/modules/*.md files.')
    process.exit(1)
  }

  let report
  if (ciMode) {
    report = ciReport(contractEndpoints, frontendEndpoints)
  } else {
    report = humanReport(contractEndpoints, frontendEndpoints)
  }

  process.exit(report.pass ? 0 : 1)
}

main()
