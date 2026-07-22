# Implemented-architecture threat review

Status: post-implementation review, 2026-07-11.

The implementation retains the protocol's most important security boundary:
authentication never branches on stage correctness. Attempt state is opaque,
cookie-bound, expiring, single-use, and protected by a submitted CSRF token.
Unknown handles receive stable synthetic cues and execute dummy Argon2 work.

Shielded mode decrypts canonical metadata only at final verification and folds
all stage differences into one result. It still reveals the direction action,
as documented by protocol version 1, and repeated observations continue to
reduce location uncertainty. Direct mode remains recordable.

The formal-pack loader fails closed to a visibly labelled development pack. It
requires explicit approval, exactly 48 CC0 scenes, unique IDs, six scenes in
each of eight families, bounded eligible cells, and valid per-window density.
The operator reviewer binds only to loopback and is not included in the public
web application.

Browser state-changing requests are protected by same-site cookies, graphical
attempt CSRF tokens, Origin comparison, and Fetch Metadata rejection. Production
deployment must preserve the external host and scheme through trusted proxy
headers. Nginx forwards the original host including its port.

Residual risks remain phishing, malware or camera capture, repeated-observation
leakage, denial of service, a compromised application host with access to keys,
and research-operator misuse. Passkeys are intentionally a separate stronger
authenticator rather than a property of the graphical protocol.
