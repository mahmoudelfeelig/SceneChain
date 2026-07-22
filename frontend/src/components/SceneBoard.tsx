import { useEffect, useRef, useState } from 'react'
import type { Scene } from '../types'
import { MARKERS, MARKER_GLYPHS } from '../lib/protocol'

interface Props {
  scene: Scene
  selectedCell?: number
  selectedMarker?: number
  mode: 'enrollment' | 'direct' | 'shielded'
  onCell?: (cell: number) => void
  onMarker?: (marker: number) => void
}

export function SceneBoard({ scene, selectedCell, selectedMarker, mode, onCell, onMarker }: Props) {
  const [focusedCell, setFocusedCell] = useState(selectedCell ?? 0)
  const cellRefs = useRef<Array<HTMLButtonElement | null>>([])
  useEffect(() => { if (selectedCell !== undefined) setFocusedCell(selectedCell) }, [selectedCell])
  const move = (cell: number, key: string) => {
    const row = Math.floor(cell / 24)
    const column = cell % 24
    const next = key === 'ArrowLeft' ? row * 24 + Math.max(0, column - 1)
      : key === 'ArrowRight' ? row * 24 + Math.min(23, column + 1)
      : key === 'ArrowUp' ? Math.max(0, row - 1) * 24 + column
      : key === 'ArrowDown' ? Math.min(15, row + 1) * 24 + column : cell
    setFocusedCell(next)
    cellRefs.current[next]?.focus()
  }

  return <div className="board-wrap" aria-label="Complete scene grid">
    <p className="board-scroll-hint">On smaller screens, scroll horizontally to reach the complete image.</p>
    <div className={`scene-board ${mode}`} aria-label={`${scene.title} interaction grid`}>
      <img src={scene.asset} alt="" draggable="false" />
      {mode !== 'shielded' && <div className="credential-grid" role="grid" aria-rowcount={16} aria-colcount={24}
        aria-label={`Choose a cell in ${scene.title}. Use arrow keys to move and Enter or Space to select.`}>
        {Array.from({ length: 384 }, (_, cell) => <button
          type="button"
          key={cell}
          ref={element => { cellRefs.current[cell] = element }}
          role="gridcell"
          aria-rowindex={Math.floor(cell / 24) + 1}
          aria-colindex={(cell % 24) + 1}
          aria-selected={selectedCell === cell}
          aria-label={`Row ${Math.floor(cell / 24) + 1}, column ${(cell % 24) + 1}${selectedCell === cell ? ', selected' : ''}`}
          tabIndex={focusedCell === cell ? 0 : -1}
          className={selectedCell === cell ? 'selected-cell' : ''}
          onFocus={() => setFocusedCell(cell)}
          onKeyDown={event => {
            if (event.key.startsWith('Arrow')) { event.preventDefault(); move(cell, event.key) }
          }}
          onClick={() => onCell?.(cell)}
        />)}
      </div>}
      {mode === 'shielded' && <div className="marker-overlay" aria-hidden="true">
        {(scene.overlay ?? []).map((marker, tile) => <span key={tile}>{MARKER_GLYPHS[marker]}</span>)}
      </div>}
    </div>
    {mode === 'shielded' && <fieldset className="marker-response">
      <legend>Select the marker covering your secret location</legend>
      <div>{MARKERS.map((marker, index) => <button type="button" key={marker}
        className={selectedMarker === index ? 'selected' : ''}
        aria-pressed={selectedMarker === index}
        onClick={() => onMarker?.(index)}><span aria-hidden="true">{MARKER_GLYPHS[index]}</span>{marker}</button>)}</div>
    </fieldset>}
  </div>
}
