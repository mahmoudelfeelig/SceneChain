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
  await page.getByRole('heading', { name: /Remember a route/i }).waitFor()
  assert.equal(await page.getByRole('button', { name: /Join study/i }).isDisabled(), true)
  assert.equal(await page.locator('.journey-step img').count(), 5)
  assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth), true)
  await page.screenshot({ path: `${output}/landing-${viewport.name}.png`, fullPage: true })
  await page.goto(`${base}/how-it-works`, { waitUntil: 'domcontentloaded', timeout: 15_000 })
  await page.getByRole('heading', { name: /A visual route/i }).waitFor()
  assert.equal(await page.locator('.how-demo img').count(), 5)
  assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth), true)
  await page.screenshot({ path: `${output}/how-it-works-${viewport.name}.png`, fullPage: true })
  await page.close()
}

const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } })
await page.goto(base, { waitUntil: 'domcontentloaded', timeout: 15_000 })
await page.getByRole('heading', { name: /Remember a route/i }).waitFor()
await page.getByRole('button', { name: /Participant information/i }).click()
assert.match(await page.getByRole('heading', { name: 'SceneChain research privacy notice' }).innerText(), /privacy notice/)
assert.equal(new URL(page.url()).pathname, '/privacy')
await page.screenshot({ path: `${output}/privacy-desktop.png`, fullPage: true })
await page.goBack()
assert.equal(await page.getByRole('heading', { name: /Remember a route/i }).isVisible(), true)

await page.getByRole('button', { name: 'How it works' }).click()
assert.equal(new URL(page.url()).pathname, '/how-it-works')
assert.match(await page.getByRole('heading', { name: /A visual route/i }).innerText(), /visual route/i)
assert.equal(await page.locator('.how-demo img').count(), 5)
await page.getByRole('button', { name: /Start practice/i }).click()
assert.equal(new URL(page.url()).pathname, '/practice')
await page.getByRole('button', { name: 'Exit flow' }).click()
assert.equal(new URL(page.url()).pathname, '/')

await page.getByRole('button', { name: /Explore the practice flow/i }).click()
assert.equal(new URL(page.url()).pathname, '/practice')
assert.equal(await page.locator('[role="gridcell"][tabindex="0"]').count(), 1)
assert.equal(await page.locator('.recommended-cell').count(), 0)
const first = page.locator('[role="gridcell"]').first()
await first.focus()
await page.keyboard.press('ArrowRight')
assert.equal(await page.locator('[role="gridcell"]').nth(1).evaluate(element => element === document.activeElement), true)
await page.screenshot({ path: `${output}/practice-keyboard-grid.png`, fullPage: true })
await page.getByRole('button', { name: 'Exit flow' }).click()
assert.equal(new URL(page.url()).pathname, '/')
await page.close()

await browser.close()
console.log('ui smoke passed')
