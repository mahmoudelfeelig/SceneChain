# Third-party notices

SceneChain includes and is built with third-party open-source software. This
file records the principal components and license families used by the
application. Copyright in each component remains with its respective authors.

The complete frontend dependency versions and declared SPDX licenses are
recorded in `frontend/package-lock.json`. Java dependencies retain their
upstream license and notice metadata within their Maven artifacts and the
packaged application. The relevant license terms continue to apply even where
components are combined with SceneChain under AGPL-3.0-only.

## Frontend runtime

| Component | Version | License |
| --- | ---: | --- |
| React | 19.1.0 | MIT |
| React DOM | 19.1.0 | MIT |
| Lucide React | 0.525.0 | ISC |

React and React DOM are Copyright © Meta Platforms, Inc. and affiliates.
Lucide is Copyright © Lucide Contributors.

The MIT-licensed components are provided under the following terms:

> Permission is hereby granted, free of charge, to any person obtaining a copy
> of this software and associated documentation files (the "Software"), to
> deal in the Software without restriction, including without limitation the
> rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
> sell copies of the Software, and to permit persons to whom the Software is
> furnished to do so, subject to the following conditions:
>
> The above copyright notice and this permission notice shall be included in
> all copies or substantial portions of the Software.
>
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
> IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
> FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
> AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
> LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
> OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
> SOFTWARE.

Lucide is provided under the ISC license:

> Permission to use, copy, modify, and/or distribute this software for any
> purpose with or without fee is hereby granted, provided that the above
> copyright notice and this permission notice appear in all copies.
>
> THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
> WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
> MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
> SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
> WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION
> OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
> CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.

## Frontend build and test tooling

Vite, the Vite React plugin, Vitest, ESLint, TypeScript, Playwright, Testing
Library, jsdom, and their transitive dependencies are used to build, test, and
verify SceneChain. Their exact versions and declared licenses are recorded in
`frontend/package-lock.json`. These packages use permissive licenses including
MIT, ISC, BSD, Apache-2.0, MIT-0, BlueOak-1.0.0, and CC-BY-4.0.
In particular, `minimatch` is available under BlueOak-1.0.0 and
`caniuse-lite` includes data available under CC-BY-4.0.

## Backend runtime

| Component family | License |
| --- | --- |
| Spring Boot and Spring Framework | Apache License 2.0 |
| Apache Tomcat | Apache License 2.0 |
| Jackson | Apache License 2.0 |
| Flyway | Apache License 2.0 |
| Netty | Apache License 2.0 |
| Lettuce | Apache License 2.0 |
| PostgreSQL JDBC Driver | BSD 2-Clause |
| Bouncy Castle Java APIs | MIT |
| Logback | EPL-1.0 or LGPL-2.1-or-later |

Testcontainers and the Spring testing libraries are development and test
dependencies. Their upstream licenses and notices remain applicable.

## Container images

Production builds use the official Node, NGINX, Maven, Eclipse Temurin,
PostgreSQL, and Redis container images. Each image contains separately licensed
operating-system and runtime packages. Their package-level copyright and
license notices remain available within the corresponding image.

## License texts

- MIT: <https://opensource.org/license/mit>
- ISC: <https://opensource.org/license/isc-license-txt>
- BSD 2-Clause: <https://opensource.org/license/bsd-2-clause>
- BSD 3-Clause: <https://opensource.org/license/bsd-3-clause>
- Apache License 2.0: <https://www.apache.org/licenses/LICENSE-2.0>
- MIT No Attribution: <https://opensource.org/license/mit-0>
- Blue Oak Model License 1.0.0:
  <https://blueoakcouncil.org/license/1.0.0>
- Creative Commons Attribution 4.0:
  <https://creativecommons.org/licenses/by/4.0/legalcode>
- Eclipse Public License 1.0:
  <https://www.eclipse.org/legal/epl-v10.html>
- GNU Lesser General Public License 2.1:
  <https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html>

The 48 research scene images are not covered by this dependency notice. Their
individual CC0-1.0 provenance records are in
`scene-pack/v1/manifest.json`. The SceneChain elephant logo is proprietary,
trademark-reserved artwork as described in `README.md`.
