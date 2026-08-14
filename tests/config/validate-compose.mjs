import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import { parse } from 'yaml'

const source = await readFile(new URL('../../deploy/docker-compose.yml', import.meta.url), 'utf8')
const compose = parse(source)
const expectedServices = ['mysql', 'server', 'h5', 'admin']

for (const service of expectedServices) {
  assert.ok(compose.services?.[service], `缺少 Compose 服务：${service}`)
}
assert.equal(compose.services.server.environment.SPRING_PROFILES_ACTIVE, 'mysql,demo')
assert.match(compose.services.server.environment.APP_JWT_SECRET, /^\$\{/)
assert.match(compose.services.mysql.environment.MYSQL_ROOT_PASSWORD, /^\$\{/)
assert.ok(Object.hasOwn(compose.volumes || {}, 'mysql-data'))

console.log('Docker Compose 配置结构与敏感变量占位检查通过。')
