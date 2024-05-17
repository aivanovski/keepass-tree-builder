# keepass-tree-builder
[![](https://jitpack.io/v/aivanovski/keepass-tree-builder.svg)](https://jitpack.io/#aivanovski/keepass-tree-builder) ![Coverage](.github/badges/jacoco.svg)</br>
The library offers DSL for generatig KeePass database files

# Installation
This library is available in [Jitpack](https://jitpack.io/#aivanovski/keepass-tree-builder) repository
```gradle
repositories {
    maven {
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation "com.github.aivanovski:keepass-tree-builder:X.X.X"
}
```

# Usage
```kotlin
DatabaseBuilderDsl.newBuilder(KotpassDatabaseConverter())
    .key(DatabaseKey.PasswordKey("123456"))
    .content(newGroupFrom("Root Group")) {
        group(newGroupFrom("Group A")) {
            group(newGroupFrom("Group B"))
            entry(newEntryFrom("Entry 1"))
            entry(newEntryFrom("Entry 2"))
        }
        entry(newEntryFrom("Entry 3"))
    }
    .build()
    .toByteArray()
```
