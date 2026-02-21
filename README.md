# TranzSign: A High-Precision ETH Wallet Prototype

This project is a high-precision Ethereum withdrawal implementation developed as a technical showcase. The goal was to solve the core challenges of DeFi mobile engineering: managing 18-decimal precision (Wei), ensuring atomic transaction lifecycles, and delivering a localized, high-trust financial UI.
## 🧠 Architectural Thinking

**Basic Withdrawal Flow**

![Basic Withdrawal Flow](./docs/basic_withdrawal_happy_path.png)

### The "Atomic" Transaction Problem
One of the biggest friction points in wallet UX is when a transaction is signed but fails to broadcast, leaving the user in limbo. I designed the `SignTransactionUseCase` to be an atomic unit of work. It handles the signing and the network submission as one continuous lifecycle. This ensures the UI state from "Signing" to "Success" comes from a single source of truth, preventing "orphaned" signatures and keeping the local state perfectly in sync with the chain.

### Why I chose MVI (Unidirectional Data Flow)
In a withdrawal flow, everything is interdependent: your balance, your input, the gas fee, and the withdrawal limit. I used a reactive `combine` strategy in the ViewModel to ensure these inputs are evaluated together in real-time. This means the "Confirm" button and error states react instantly to every keystroke, eliminating the "laggy" validation common in many crypto apps.

**Basic Architectural Overview High-level**

![Basic Architectural Overview High-level](./docs/basic_architectural_overview.png)

### Financial Accuracy & UI Stability
To avoid the precision loss inherent in floating-point math, all internal logic uses `BigInteger` (Wei). This also models how we expect amounts from the smart contract to be represented, ensuring our app's logic is as close to the protocol as possible. The UI layer then formats these values for display, ensuring we never show a balance the user doesn't actually have.
* **Truncation Logic:** I used `RoundingMode.DOWN` for all displays. Rounding up a balance can lead to "Insufficient Funds" errors at the protocol level; truncation ensures we never show a balance the user doesn't actually have.
* **Small Value Handling:** To keep the UI clean, I used a 4-decimal fixed display. For non-zero values smaller than `0.0001`, the app renders `< 0.0001 ETH`. This prevents the UI from showing `0.000` for active balances, which helps avoid user confusion and unnecessary support queries.

### Global-Ready Formatting
The formatting engine respects the User's OS Locale for decimal separators and intelligently places the "ETH" symbol based on regional standards (e.g., `1.2 ETH` vs `ETH 1,2`), ensuring the app feels native to a global audience.

## 🛠 Tech Choices
* **Jetpack Compose:** For a completely declarative UI that stays in sync with the ViewModel state.
* **BigInteger (Wei)/BigDecimal:** All internal math is done in Wei to avoid the precision loss inherent in `Double` or `Float`.
* **Kotlin Coroutines/Flow:** For handling the asynchronous nature of blockchain simulations and state updates.
* **Turbine:** Used in unit tests to verify the flow of the MVI state machine.

## 🛑 Scope & Assumptions
To keep the focus on the core architectural and mathematical challenges of this assignment, I have omitted standard "production-ready" boilerplate:
* **CI/CD & Linting:** I've skipped custom linting rules, Ktlint, and CI pipeline configurations.
* **Analytics & Crashlytics:** No tracking or telemetry has been integrated.
* **Security:** This is a prototype; a production version would utilize the Android Keystore for private key management and hardware-backed security.
* **Backend:** Network calls are abstracted via an `InMemoryBackendService` to focus on the UI and domain logic.
