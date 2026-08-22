# 0019 Telemetry Privacy Rule
#type/decision #status/accepted #area/privacy #area/telemetry

Links: [[../DDS]], [[../areas/Telemetry Privacy]], [[../Mobile Companion]], [[0008 Desktop Full Spectrum Mobile Companion]]

## Decision

Telemetry should only be shared between users inside the same active session.

For the mobile companion, win/loss and game tracker data may be shared among
session participants when two or more players are intentionally using the app
together. It should not be sent to unrelated users, global analytics, or a
central telemetry system as part of normal app behavior.

## Requirements

- Session telemetry is visible only to users participating in that session.
- Win/loss recording for shared telemetry requires two or more participating
  users in the session.
- Session telemetry must not update player style for other users.
- Session telemetry must not be shared outside the session without a separate
  explicit export/share action.
- Any future server-backed telemetry requires a separate accepted decision
  covering privacy, consent, cost, and data retention.

## Consequences

- The mobile companion can support shared game tracking without becoming a
  surveillance or analytics product.
- Users can trust that tracker data stays in-session by default.
- Future telemetry expansion is blocked until explicitly designed and accepted.

