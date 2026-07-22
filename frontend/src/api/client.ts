async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    credentials: 'include',
    ...init,
    headers: { 'Content-Type': 'application/json', ...init?.headers },
  })
  if (!response.ok) {
    const retryAfter = response.headers.get('Retry-After')
    throw new Error(response.status === 429
      ? `Too many attempts. Try again in ${retryAfter ?? '60'} seconds.`
      : response.status === 503 ? 'Recruitment is not open or the formal pack is unavailable.'
      : 'The request could not be completed.')
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export const api = {
  post: <T>(path: string, body?: unknown, csrf?: string) => request<T>(path, {
    method: 'POST',
    body: body === undefined ? undefined : JSON.stringify(body),
    headers: csrf ? { 'X-CSRF-Token': csrf } : undefined,
  }),
  get: <T>(path: string) => request<T>(path),
  delete: (path: string, body?: unknown) => request<void>(path, {
    method: 'DELETE', body: body === undefined ? undefined : JSON.stringify(body),
  }),
}
