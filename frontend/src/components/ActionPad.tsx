import { ACTIONS, ACTION_GLYPHS } from '../lib/protocol'

interface Props { selected?: number; onSelect: (action: number) => void }

export function ActionPad({ selected, onSelect }: Props) {
  return <fieldset className="action-pad">
    <legend>Choose the direction attached to this location</legend>
    <div className="action-grid" role="radiogroup">
      {ACTIONS.map((label, index) => <button
        type="button"
        key={label}
        className={selected === index ? 'selected' : ''}
        role="radio"
        aria-checked={selected === index}
        onClick={() => onSelect(index)}
      ><span aria-hidden="true">{ACTION_GLYPHS[index]}</span><small>{label}</small></button>)}
    </div>
  </fieldset>
}
