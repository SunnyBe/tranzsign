# TranzSign: A High-Precision ETH Wallet Prototype

I built TranzSign to explore how we can bridge the gap between complex blockchain data and a smooth, "human-friendly" mobile experience. This project demonstrates a production-grade approach to handling Ethereum’s 18-decimal precision while maintaining a clean, localized UI.

## 🧠 Architectural Thinking

**Basic Withdrawal Flow**

[Basic Withdrawal Flow](docs/basic_withdrawal_happy_path.png)

### The "Atomic" Transaction Problem
One of the biggest friction points in wallet UX is when a transaction is signed but fails to broadcast, leaving the user in limbo. I designed the `SignTransactionUseCase` to be an atomic unit of work. It handles the signing and the network submission as one continuous lifecycle. This ensures the UI state—from "Signing" to "Success"—comes from a single source of truth, preventing "orphaned" signatures and keeping the local state perfectly in sync with the chain.

### Why I chose MVI (Unidirectional Data Flow)
In a withdrawal flow, everything is interdependent: your balance, your input, the gas fee, and the withdrawal limit. I used a reactive `combine` strategy in the ViewModel to ensure these inputs are evaluated together in real-time. This means the "Confirm" button and error states react instantly to every keystroke, eliminating the "laggy" validation common in many crypto apps.

**Basic Architectural Overview High-level**

[Basic Architectural Overview High-level](docs/basic_architectural_overview_high_level.png)

### Handling "The Dust" & Precision
Standard rounding is dangerous in DeFi. If the app rounds up a balance of `0.0049` to `0.01`, the user’s transaction will fail with an "Insufficient Funds" error.
* **The Rule:** I standardized on `RoundingMode.DOWN` (Truncation). It’s better to be conservative about what a user can spend than to "over-promise" a balance they don't actually have.
* **The UX:** I implemented a "Standard" 4-decimal view for the main UI to keep it scannable. For values smaller than `0.0001`, I use a `< 0.0001` threshold. This acknowledges the "dust" exists (avoiding unnecessary support calls) without cluttering the screen with 18 digits of visual noise.

### Global-Ready Formatting
Crypto is global, so the formatting engine respects the User's OS Locale. Whether they use a comma or a dot as a decimal separator, the app adapts. I also built the logic to intelligently place the "ETH" symbol based on regional standards (e.g., `1.5 ETH` vs `ETH 1.5`) so the app feels like a native tool, not a port.

## 🛠 Tech Choices
* **Jetpack Compose:** For a completely declarative UI that stays in sync with the ViewModel state.
* **BigInteger (Wei):** All internal math is done in Wei to avoid the precision loss inherent in `Double` or `Float`.
* **Kotlin Coroutines/Flow:** For handling the asynchronous nature of blockchain simulations and state updates.
* **Turbine:** Used in unit tests to verify the flow of the MVI state machine.

## 🛑 Scope & Assumptions
To keep the focus on the core architectural and mathematical challenges of this assignment, I have omitted standard "production-ready" boilerplate:
* **CI/CD & Linting:** I've skipped custom linting rules, Ktlint, and CI pipeline configurations.
* **Analytics & Crashlytics:** No tracking or telemetry has been integrated.
* **Security:** This is a prototype; a production version would utilize the Android Keystore for private key management and hardware-backed security.
* **Backend:** Network calls are abstracted via an `InMemoryBackendService` to focus on the UI and domain logic.