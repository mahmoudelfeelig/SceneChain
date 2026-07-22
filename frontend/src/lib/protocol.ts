export const COLUMNS = 24
export const ROWS = 16
export const ACTIONS = ['North', 'East', 'South', 'West']
export const ACTION_GLYPHS = ['↑', '→', '↓', '←']
export const MARKERS = ['Circle', 'Triangle', 'Square', 'Diamond', 'Cross', 'Ring', 'Bars', 'Star']
export const MARKER_GLYPHS = ['●', '▲', '■', '◆', '✚', '○', '▤', '★']

export function cellFromPoint(x: number, y: number, width: number, height: number): number {
  const column = Math.min(COLUMNS - 1, Math.max(0, Math.floor((x / width) * COLUMNS)))
  const row = Math.min(ROWS - 1, Math.max(0, Math.floor((y / height) * ROWS)))
  return row * COLUMNS + column
}

export function tileForCell(cell: number): number {
  const row = Math.floor(cell / COLUMNS)
  const column = cell % COLUMNS
  return Math.floor(row / 2) * 12 + Math.floor(column / 2)
}

export function encodeCredential(stages: Array<{ sceneId: number; cellId: number; actionId: number }>): string {
  if (stages.length !== 5) throw new Error('A credential must contain five stages')
  const bytes = new Uint8Array(39)
  const view = new DataView(bytes.buffer)
  view.setUint8(0, 1); view.setUint16(1, 1); view.setUint8(3, 5)
  stages.forEach((stage, index) => {
    if (!Number.isInteger(stage.sceneId) || stage.sceneId < 1 || stage.sceneId > 0xffffffff
      || !Number.isInteger(stage.cellId) || stage.cellId < 0 || stage.cellId >= 384
      || !Number.isInteger(stage.actionId) || stage.actionId < 0 || stage.actionId >= 4) throw new Error('Invalid stage')
    const offset = 4 + index * 7
    view.setUint32(offset, stage.sceneId); view.setUint16(offset + 4, stage.cellId); view.setUint8(offset + 6, stage.actionId)
  })
  return Array.from(bytes, value => value.toString(16).padStart(2, '0')).join('')
}
