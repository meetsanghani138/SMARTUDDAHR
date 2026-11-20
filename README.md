# Smart Udhar – Smart Hisab, Easy Vyapar

A complete, production-quality native Android application designed specifically for Indian Pan Shops, Kirana stores, and retail business owners to manage customer Udhar (credit), receive payments, generate PDF receipts, send WhatsApp reminders, track inventory, and get business reports.

---

## 🌟 Key Features

### 1. Customer Khata & Ledger Management
- Add, search, and manage customer credit records.
- View real-time outstanding balances reconciled against financial transaction history.
- Soft archive protection so accounting records remain consistent.

### 2. Quick Udhar & Product Selection
- Single-tap quick amount entries (₹50, ₹100, ₹200, ₹500) or product-based entries.
- Pre-populated Pan Shop categories:
  - **Mava & Brands** (Vimal, Rajnigandha, RMD)
  - **Pan** (Sadu Pan, Mithu Pan)
  - **Snacks** (Balaji Wafers, Biscuits, Namkeen)
  - **Cold Drinks** (Cold Drink, Water Bottle)
- Plus/minus quantity selectors with live subtotal and projected new customer outstanding calculations.
- Immutable snapshot pricing in transaction history.

### 3. Payment Reception
- Record full or partial payments across **Cash**, **UPI**, and **Bank Transfer**.
- Auto-deducts from customer outstanding balance in an atomic database transaction.

### 4. Multilingual WhatsApp Reminders
- One-tap WhatsApp payment reminders with automatic phone number formatting.
- Built-in templates in **Gujarati**, **Hindi**, and **English**:
  - *Gujarati:* "નમસ્તે [Name], તમારી પાસે અમારાં દુકાનનું ₹[Amount] ઉધાર બાકી છે. કૃપા કરીને અનુકૂળ સમયે ચુકવણી કરશો. આભાર!"
  - *Hindi:* "नमस्ते [Name], आपकी दुकान पर ₹[Amount] बकाया है। कृपया सुविधानुसार भुगतान करें। धन्यवाद!"
  - *English:* "Hello [Name], your outstanding balance at [Shop] is ₹[Amount]. Please make the payment at your convenience. Thank you!"

### 5. PDF Statement & Receipt Generator
- Native PDF receipt generation using Android Canvas & PdfDocument APIs.
- Includes shop header, customer details, itemized table in INR ₹, subtotal, previous balance, new credit/payment, and final balance.
- Share via system share sheet or WhatsApp.

### 6. AI Shop Assistant & Voice Entry
- **AI Assistant:** Safe local query engine providing executive business insights (Pending Udhar, Today's Sales, Low Stock warnings, Top customers).
- **Voice Purchase Entry:** Speech-to-text input parsing spoken Gujarati, Hindi, or English phrases (e.g. *"એક વિમલ અને બે કોલ્ડ ડ્રિંક"* or *"1 Vimal and 2 Cold Drink"*).

### 7. SQLite Database Architecture
- 13 structured tables (`users`, `shops`, `customers`, `categories`, `products`, `transactions`, `transaction_items`, `payments`, `staff`, `reminders`, `settings`, `sync_queue`, `audit_logs`).
- Stores money in integer **paise** (`1 Rupee = 100 Paise`) to prevent floating-point rounding errors.
- Enforces foreign key constraints (`PRAGMA foreign_keys = ON;`), parameterized queries, and atomic database transactions (`beginTransaction()`).

### 8. Backup & Restore
- JSON database backup export and file sharing via FileProvider.
- Full offline-first capability.

---

## 🛠 Tech Stack

- **Language:** Kotlin
- **UI Framework:** Android XML Layouts, Material Design 3
- **Database:** SQLite (`SQLiteOpenHelper`) – *No Room, No Compose, No Flutter*
- **Architecture:** Repository & ViewBinding
- **Chart Component:** Custom Native Canvas BarChart (`CustomBarChartView`)
- **Speech Input:** Android `SpeechRecognizer`
- **Build System:** Gradle (AGP 9.2.1, compileSdk 37)

---

## 🚀 How to Build & Run

1. Open `E:/Radhe` in Android Studio.
2. Ensure Android SDK 37 is installed.
3. Sync Gradle project.
4. Run task `app:assembleDebug` to compile the application APK.
5. Run unit test suite: `app:testDebugUnitTest`.

### Demo Login Credentials
- **Username:** `admin`
- **Password:** `password123`
