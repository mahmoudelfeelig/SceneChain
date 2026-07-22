export type Mode = 'direct' | 'shielded'

export interface Scene {
  id: number
  version: number
  family: string
  title: string
  asset: string
  thumbnail?: string
  license: string
  overlay?: number[]
  eligibleCells?: number[]
}

export interface Stage {
  sceneId: number
  cellId?: number
  actionId: number
  markerId?: number
}

export interface AttemptResponse {
  csrfToken: string
  mode: Mode
  scenes: Scene[]
}

export interface EnrollmentResponse {
  csrfToken: string
  handle: string
  scenes: Scene[]
}
