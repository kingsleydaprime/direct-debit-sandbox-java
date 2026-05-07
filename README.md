# Direct Debit Sandbox

A local sandbox server that mimics the ITC Direct Debit API. Point your integration at `http://localhost:8080` instead of the real gateway and get realistic, deterministic responses — no live bank connections required.

## Prerequisites

- Java 23+
- Nothing else — Gradle downloads itself on first run

## Running

```bash
./gradlew bootRun
```

The server starts on `http://localhost:8080`.

## Scenario cheat sheet

The last three digits of `debitAccount` control the outcome of every debit:

| Suffix | Outcome              | Response code |
|--------|----------------------|---------------|
| `001`  | Success              | `01`          |
| `101`  | Insufficient funds   | `101`         |
| `104`  | Duplicate            | `104`         |
| `004`  | Pre-approval pending | `04`          |
| `131`  | Timeout              | `131`         |
| `111`  | Inconclusive         | `111`         |
| `100`  | General failure      | `100`         |

## Endpoints

| # | Method | Path | Description |
|---|--------|------|-------------|
| 1 | POST | `/provision` | Register a merchant callback URL |
| 2 | POST | `/subscription/subscribe` | Create a recurring subscription |
| 3 | POST | `/subscription/update` | Update an existing subscription |
| 4 | POST | `/subscription/cancel` | Cancel a subscription |
| 5 | POST | `/subscription/customer-subscriptions` | List subscriptions for an account |
| 6 | POST | `/subscription/pause` | Pause a subscription |
| 7 | POST | `/subscription/resume-debit` | Resume a paused subscription |
| 8 | POST | `/subscription/trigger-debit` | Manually trigger a debit |
| 9 | POST | `/subscription/schedule-debit` | Schedule a one-time debit |
| 10 | POST | `/subscription/retrieve/details/thirdpartyreferenceno` | Look up subscription by reference |
| 11 | POST | `/transaction/check-status` | Check debit transaction status |

All requests require headers:
```
x-transflowId: <any non-empty value>
x-key:         <any non-empty value>
x-country:     <any non-empty value>   (not required for /provision)
```

## Callbacks

Subscribe fires two async callbacks to the URL registered via `/provision`:

1. **Preapproval callback** — fires ~2 seconds after subscribe (confirming the mandate was created)
2. **Transaction callback** — fires ~7 seconds after subscribe (the actual debit result)

Use [webhook.site](https://webhook.site) to inspect callbacks during development.

## Sample requests

See `requests.http` for ready-to-run examples covering all endpoints and error scenarios. Open it in IntelliJ or install the VS Code REST Client extension.

## Data storage

All data is in-memory. It resets when the server restarts. This is intentional — the sandbox is for short-lived integration testing, not persistence.
