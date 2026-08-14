import { mkdir } from 'node:fs/promises'
import { fileURLToPath } from 'node:url'
import { chromium } from 'playwright'

const outputDirectory = new URL('../../artifacts/screenshots/', import.meta.url)
await mkdir(outputDirectory, { recursive: true })

const browser = await chromium.launch({ channel: 'msedge', headless: true })
const findings = []

async function captureErrors(page, surface) {
  page.on('console', (message) => {
    if (message.type() === 'error') findings.push(`${surface} console: ${message.text()}`)
  })
  page.on('pageerror', (error) => findings.push(`${surface} pageerror: ${error.message}`))
}

try {
  const mobile = await browser.newContext({ viewport: { width: 390, height: 844 }, deviceScaleFactor: 1 })
  const h5 = await mobile.newPage()
  await captureErrors(h5, 'H5')
  await h5.goto('http://localhost:5173', { waitUntil: 'networkidle' })
  await h5.getByText('以租用人小林登录').click()
  await h5.getByText('附近空闲车位').waitFor({ timeout: 15_000 })
  const spaceLink = h5.getByText('查看并预约').first()
  await spaceLink.waitFor({ timeout: 10_000 })
  await h5.screenshot({ path: fileURLToPath(new URL('h5-home.png', outputDirectory)), fullPage: true })
  await spaceLink.click()
  await h5.getByText('确认免费预约').waitFor({ timeout: 10_000 })
  await h5.screenshot({ path: fileURLToPath(new URL('h5-space-detail.png', outputDirectory)), fullPage: true })
  await mobile.close()

  const desktop = await browser.newContext({ viewport: { width: 1440, height: 900 }, deviceScaleFactor: 1 })
  const admin = await desktop.newPage()
  await captureErrors(admin, 'Admin')
  await admin.goto('http://localhost:5174', { waitUntil: 'networkidle' })
  await admin.getByText('进入本地演示后台').click()
  await admin.getByText('运营概览', { exact: true }).first().waitFor({ timeout: 15_000 })
  await admin.waitForTimeout(1_000)
  await admin.screenshot({ path: fileURLToPath(new URL('admin-dashboard.png', outputDirectory)), fullPage: true })
  await admin.getByText('小区管理', { exact: true }).click()
  await admin.getByText('运营小区', { exact: true }).waitFor()
  await admin.screenshot({ path: fileURLToPath(new URL('admin-communities.png', outputDirectory)), fullPage: true })
  await desktop.close()
} finally {
  await browser.close()
}

const ignoredPatterns = ['Failed to load resource: net::ERR_BLOCKED_BY_CLIENT']
const relevantFindings = findings.filter((finding) => !ignoredPatterns.some((pattern) => finding.includes(pattern)))
console.log(JSON.stringify({ ok: relevantFindings.length === 0, findings: relevantFindings }, null, 2))
if (relevantFindings.length) process.exitCode = 1
