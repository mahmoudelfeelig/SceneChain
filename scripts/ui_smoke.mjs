import { chromium } from '../frontend/node_modules/playwright/index.mjs'
import { strict as assert } from 'node:assert'
import { resolve } from 'node:path'

const base = process.env.SCENECHAIN_URL ?? 'http://host.docker.internal:8088'
const output = resolve(process.env.SCENECHAIN_SCREENSHOTS ?? 'docs/final-review-2026-07-22')
const browser = await chromium.launch({ headless: true,
  ...(process.env.CHROME_PATH ? { executablePath: process.env.CHROME_PATH } : {}) })

for (const viewport of [{ name: 'desktop', width: 1440, height: 1000 }, { name: 'laptop', width: 1024, height: 768 }, { name: 'phone-info-only', width: 390, height: 844 }]) {
  const page = await browser.newPage({ viewport })
  await page.goto(base, { waitUntil: 'domcontentloaded', timeout: 15_000 })
  await page.getByText('Approved CC0 research scene pack loaded.').waitFor()
  await assert.rejects(() => page.getByRole('button', { name: 'Recruitment not yet open' }).click({ timeout: 500 }))
  assert.equal(await page.getByRole('button', { name: 'Recruitment not yet open' }).isDisabled(), true)
  assert.match(await page.locator('body').innerText(), /Approved CC0 research scene pack loaded/)
  assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth), true)
  await page.screenshot({ path: `${output}/landing-${viewport.name}.png`, fullPage: true })
  await page.close()
}

const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } })
await page.goto(base, { waitUntil: 'domcontentloaded', timeout: 15_000 })
await page.getByText('Approved CC0 research scene pack loaded.').waitFor()
await page.getByRole('button', { name: 'Privacy and participant information' }).click()
assert.match(await page.getByRole('heading', { name: 'SceneChain research privacy notice' }).innerText(), /privacy notice/)
await page.screenshot({ path: `${output}/privacy-desktop.png`, fullPage: true })
await page.getByRole('button', { name: 'Exit' }).click()
assert.equal(await page.getByRole('heading', { name: /visual passphrase/i }).isVisible(), true)

await page.getByRole('button', { name: 'Try a practice chain' }).click()
assert.equal(await page.locator('[role="gridcell"][tabindex="0"]').count(), 1)
assert.equal(await page.locator('.recommended-cell').count(), 0)
const first = page.locator('[role="gridcell"]').first()
await first.focus()
await page.keyboard.press('ArrowRight')
assert.equal(await page.locator('[role="gridcell"]').nth(1).evaluate(element => element === document.activeElement), true)
await page.screenshot({ path: `${output}/practice-keyboard-grid.png`, fullPage: true })
await page.close()

await browser.close()
console.log('ui smoke passed')
