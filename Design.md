Here is the comprehensive, production-ready `design.md` file. It has been updated to reflect your structural changes: the removal of the three-dots "More" menu from the header, the shifting of all operational modes exclusively into the Hamburger Navigation Drawer, and the addition of the new widgets and theme controls.

---

### `design.md`

```markdown
# Engineering Design & Interface Architecture: Multi-Mode Calculator

This document establishes the precise user interface layouts, spatial positioning, structural wireframes, and component maps for the Android application. 

---

## 🎨 Design Language Principles & System States

### 1. Minimalist Constraints
* **Immutable Keypad Footprint:** The absolute geometric dimensions and touch boundaries of the `0-9` numeric keys and the decimal point remain physically locked in place across every screen mode to leverage muscle memory.
* **Transient Dynamic Overlays:** Complex controls, mode shifts, and theme modifications are strictly tucked away, appearing only via the Hamburger Navigation Drawer.

### 2. Global Palette Specification
The application tracks three standard structural theme profiles. 

| Element Class | Dark Palette (Default) | Light Palette | System Auto-Match |
| :--- | :--- | :--- | :--- |
| **App Canvas Background** | `#121212` (Matte Black) | `#FFFFFF` (Pure White) | Dynamically bounded |
| **Primary Display Area** | `#1A1A1A` | `#F5F7FA` | to runtime system |
| **Standard Key Targets** | `#242424` (Soft Charcoal) | `#E2E8F0` (Light Ash) | configuration tokens |
| **Arithmetic Operators** | `#2A5C91` (Muted Blue) | `#3182CE` (Brand Blue) | (`UI_MODE_NIGHT`) |
| **Text & Numbers** | `#E0E0E0` (Off-White) | `#1A202C` (Slate Black) | |

---

## 📱 Global Application Layouts

### 1. The Anchor Layout: Basic Arithmetic Screen
The primary calculator interface is intentionally sparse. The top bar contains only the interactive hamburger selector and the history stack access key. The "three-dots" options button is entirely omitted.

```text
+---------------------------------------------------------+
|  [≡ Menu]                              [History 🕒]      | <-- Top Action Header
+---------------------------------------------------------+
|                                                         |
|                                         250 × 4         | <-- Expression String (16pt Muted)
|                                                         |
|                                           1,000         | <-- Active Output String (36pt Bold)
+---------------------------------------------------------+
|   [ C ]          [ ( ) ]          [ % ]          [ ÷ ]  | <-- Row 1: Functional Utilities
|                                                         |
|   [ 7 ]          [ 8 ]            [ 9 ]          [ × ]  | <-- Row 2: Operator Core
|                                                         |
|   [ 4 ]          [ 5 ]            [ 6 ]          [ - ]  | <-- Row 3: Operator Core
|                                                         |
|   [ 1 ]          [ 2 ]            [ 3 ]          [ + ]  | <-- Row 4: Operator Core
|                                                         |
|   [ +/- ]        [ 0 ]            [ . ]          [ = ]  | <-- Row 5: Action Base
+---------------------------------------------------------+

```

### 2. The Hamburger Navigation Drawer (Expanded Content)

Tapping `[≡ Menu]` slides out a side sheet overlay from the left edge covering 75% of the screen horizontal space. It encapsulates all sub-modes and houses the global configuration dials at its foot.

```text
+-----------------------------------------+---------------+
| 🧮 CALCULATOR PRO                      |               |
| ─────────────────────────────────────── |               |
|                                         |               |
|  [🏠]  Standard Arithmetic Mode         |               |
|                                         |               |
|  [📐]  Engineering / Scientific         |               |
|                                         |               |
|  [💱]  Real-Time Currency Converter     |               |
|                                         |               |
|  [⚖️]  Metric Conversion System         |               |
|                                         |               |
|  [📊]  Financial EMI Terminal           |               |
|                                         |               |
| ─────────────────────────────────────── |  Dismiss Zone |
|                                         |  (Tapping     |
| ⚙️ Theme Selection                       |   here        |
|  ( ) Dark    ( ) Light    (*) System    |   collapses   |
|                                         |   drawer)     |
|                                         |               |
| v1.0.0 (NeuralMesh Private Limited)    |               |
+-----------------------------------------+---------------+

```

### 3. Engineering / Scientific Screen Layout

When initialized, the top calculation canvas expands to accept double-line parenthetical entries. A persistent scientific utility row is injected right above the standard keypad footprint.

```text
+---------------------------------------------------------+
|  [≡ Menu]                              [DEG/RAD]        |
+---------------------------------------------------------+
|                                                         |
|                                      sin(30) + log(100) |
|                                                         |
|                                             2.5         |
+---------------------------------------------------------+
|  [ 2nd ]   [ deg ]   [ sin ]   [ cos ]   [ tan ]   [ √ ] | <-- Injected Sci-Modifiers
|  [ ln  ]   [ log ]   [ ^   ]   [ π   ]   [ e   ]   [ ! ] |
+---------------------------------------------------------+
|   [ C ]          [ ( ) ]          [ % ]          [ ÷ ]  |
|   [ 7 ]          [ 8 ]            [ 9 ]          [ × ]  |
|   [ 4 ]          [ 5 ]            [ 6 ]          [ - ]  | <-- Anchor Base Layout Preserved
|   [ 1 ]          [ 2 ]            [ 3 ]          [ + ]  |
|   [ +/- ]        [ 0 ]            [ . ]          [ = ]  |
+---------------------------------------------------------+

```

### 4. Dual-Field Variable Layouts (Metric & Currency Converters)

For metric system tracking or international cash flows, the display transforms into structured entry fields. The classic operator row adapts into text modification triggers (`Clear`, `Backspace`, `Swap Unit Direction`).

```text
+---------------------------------------------------------+
|  [≡ Mode Surface]                     [▼ Selector Item] | <-- Category Control Spinner
+---------------------------------------------------------+
|   Input Source:  [ Pounds (lbs)                 ▼ ]     |
|                  550                                    | <-- Focused Input State Indicator
|                                                         |
|   Output Target: [ Kilograms (kg)               ▼ ]     |
|                  249.47                                 | <-- Real-Time Reciprocal Evaluation
+---------------------------------------------------------+
|   [ 7 ]          [ 8 ]            [ 9 ]          [ ⌫ ]  | <-- Restricted Numeric Input Matrix
|                                                         |
|   [ 4 ]          [ 5 ]            [ 6 ]          [ AC ] | <-- Clear Matrix Action
|                                                         |
|   [ 1 ]          [ 2 ]            [ 3 ]          [ ⇄ ]  | <-- Instant Vector Reverse Tweak
|                                                         |
|   [ +/- ]        [ 0 ]            [ . ]          [Done] | <-- State Dismiss Entry Button
+---------------------------------------------------------+

```

### 5. Financial EMI Terminal Layout

Optimized for calculation inputs regarding loan principal amounts, terms, and interest allocations. Fields move progressively as data values update.

```text
+---------------------------------------------------------+
|  [≡ Financial Mode]                     [▼ Loan EMI]    |
+---------------------------------------------------------+
|  [ Principal Loan ($)     ] ➔  150,000                 |
|  [ Target Interest (%)     ] ➔  6.5                      |
|  [ Repayment Term (Months) ] ➔  240                      |
+---------------------------------------------------------+
|  >> Est. Monthly Amortized Installment:   $1,118.39     | <-- Computed Snapshot Output Component
+---------------------------------------------------------+
|   [ 7 ]          [ 8 ]            [ 9 ]          [Next] | <-- Incremental Field Focus Jumper
|   [ 4 ]          [ 5 ]            [ 6 ]          [ ⌫ ]  |
|   [ 1 ]          [ 2 ]            [ 3 ]          [ C ]  |
|                  [ 0 ]            [ . ]          [Calc] |
+---------------------------------------------------------+

```

---

## 🧩 Standalone Home Screen App Widget Architecture

These structures are built explicitly over the `androidx.glance` layout engine framework, running independently of the core application foreground instances.

### Widget 1: Function Visualizer (2x2 Canvas)

```text
+-----------------------------------+
| f(x) Math Curve Canvas   [Random] | <-- Regenerates variable scaling algorithms
| ───────────────────────────────── |
|                 /\                |
|                /  \               |
|  ______ ______/____\______ ______ | <-- Pure code procedural coordinate plotting 
|       /      /      \      \      |     (Requires 0 local database resources)
|      /      /        \      \     |
+-----------------------------------+

```

### Widget 2: Price Calculator (4x1 Strip)

```text
+---------------------------------------------------------+
| Base ($): 120.00 | Tax (+18%) | Disc (-10%) | Total:127.44 |
| [ + Enter Value ]                                       | <-- Launches immediate widget input entry overlay
+---------------------------------------------------------+

```

### Widget 3: Currency Snapshot (4x2 Mini-Graph)

```text
+---------------------------------------------------------+
| 💱 USD ➔ INR Trend Line                  [🔄 Sync CDN] | <-- Forces background Ktor update
| 🟢 ₹83.42 (+0.14% Day Scale)                            |
|     /\     /\                                           |
| ___/  \___/  \_________________________________________ | <-- 7-day historic trend snapshot graph
+---------------------------------------------------------+

```

### Widget 4: The Daily Budget Burn (4x1 Micro-Ledger)

```text
+---------------------------------------------------------+
| 💳 Cap Balance: [██████████░░░░░░░░░] $85.00 / $200 Left |
| Quick Deduct Amount: [ $___ ]             [✓ Apply]     | <-- Subtracts balance via native BroadcastReceiver
+---------------------------------------------------------+

```

### Widget 5: Custom Unit Conversion Quick Card (2x2 Modular Variant)

```text
+-----------------------------------+
| ⚖️ Fixed Conversion   [ ⇄ Reverse] |
| ───────────────────────────────── |
|  Input Weight:    [ 180 ] lbs     |
|                                   |
|  Target Metric:   [ 81.64 ] kg    | <-- Instant field computation without opening the full application
+-----------------------------------+

```

```
***

### Implementation Notes for the Development Team:
1. **Drawer State:** Ensure your Compose structure ties the open state of the Drawer directly to the `BackHandler` so that pressing the physical system back button collapses the drawer smoothly before attempting to minimize the app.
2. **Glance Layout Invalidation:** Because glance widgets use native Android RemoteViews, ensure the widget broadcast intents pass primitive values explicitly to avoid heavy configuration serialization errors during home screen interactions.

```