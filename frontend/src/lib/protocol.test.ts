import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { cellFromPoint, encodeCredential, tileForCell } from './protocol'

describe('protocol coordinates', () => {
  it('maps canonical edges into the 24 by 16 grid', () => {
    expect(cellFromPoint(0, 0, 1152, 768)).toBe(0)
    expect(cellFromPoint(1152, 768, 1152, 768)).toBe(383)
    expect(cellFromPoint(576, 384, 1152, 768)).toBe(204)
  })

  it('maps four credential cells to one shielded tile', () => {
    expect(tileForCell(0)).toBe(0)
    expect(tileForCell(1)).toBe(0)
    expect(tileForCell(24)).toBe(0)
    expect(tileForCell(25)).toBe(0)
    expect(tileForCell(383)).toBe(95)
  })

  it('consumes the repository canonical credential vector', () => {
    const vector = JSON.parse(readFileSync(resolve('../protocol/test-vectors/credential-v1.json'), 'utf8'))
    expect(encodeCredential(vector.stages)).toBe(vector.encodedHex)
    expect(vector.encodedLength).toBe(39)
  })
})
