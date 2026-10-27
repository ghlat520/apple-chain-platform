/**
 * Generate TypeScript types from the backend OpenAPI schema.
 *
 * Usage:
 *   1. Start the backend locally (apple-web on :8080).
 *   2. Run `npm run gen:types` (or `pnpm gen:types`).
 *
 * Output: src/types/generated.ts
 *
 * NOTE — This file is a skeleton. It uses `openapi-typescript` programmatic API
 * and writes the generated types to disk. Re-run after backend contract changes.
 */
import { writeFile, mkdir } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import openapiTS, { astToString } from 'openapi-typescript';

const OPENAPI_URL =
  process.env.OPENAPI_URL ?? 'http://localhost:8080/v3/api-docs';

const __dirname = dirname(fileURLToPath(import.meta.url));
const OUTPUT_PATH = resolve(__dirname, '..', 'src', 'types', 'generated.ts');

async function main(): Promise<void> {
  console.log(`[gen-types] fetching OpenAPI from ${OPENAPI_URL}`);
  const ast = await openapiTS(new URL(OPENAPI_URL));
  const contents = astToString(ast);

  const header =
    '/* eslint-disable */\n' +
    '/**\n' +
    ' * AUTO-GENERATED from backend OpenAPI schema.\n' +
    ' * DO NOT EDIT manually. Re-run `npm run gen:types` instead.\n' +
    ` * Source: ${OPENAPI_URL}\n` +
    ' */\n\n';

  await mkdir(dirname(OUTPUT_PATH), { recursive: true });
  await writeFile(OUTPUT_PATH, header + contents, 'utf8');
  console.log(`[gen-types] wrote ${OUTPUT_PATH}`);
}

main().catch((err: unknown) => {
  console.error('[gen-types] failed:', err);
  process.exit(1);
});
