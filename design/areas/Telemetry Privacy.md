# Telemetry Privacy
#type/area #area/privacy #area/telemetry #status/active

Links: [[../DDS]], [[../Mobile Companion]], [[../decisions/0019 Telemetry Privacy Rule]]

## Purpose

Telemetry Privacy defines what gameplay/tracker data can be shared and where.

## Core Rule

Telemetry is session-local.

Telemetry should only be shared between users inside the same active session.
It should not become background global telemetry, central analytics, or public
user tracking.

## Applies To

- mobile game tracker
- life counter session state
- win/loss recording when two or more players are using the app together
- session participant state

## Out Of Scope For Normal Telemetry

- global analytics
- cross-user tracking outside the session
- selling/monetizing player behavior
- sharing player style data with other users
- uploading session data to a maintained server without a separate accepted
  decision

