# SkullCreator

![Java](https://img.shields.io/badge/language-Java%2021-orange)
![Minecraft](https://img.shields.io/badge/MC-1.12%2B-green)
![License](https://img.shields.io/github/license/diamondpixel/SkullCreator)

ATTENTION THIS LIBRARY IS UNDER CONSTRUCTION AND WON'T BE AVAILABLE THROUGH MAVEN REPOSITORY UNTIL I RELEASE IT.
CURRENTY I AM AWAY FROM HOME, AND I CAN'T WORK ON THIS. THE LIBRARY IS FUNCTIONAL UP TO 1.20.6 FULLY ONLY 1.21.x HAS
SOME PROBLEM WITH THE BLOCKS. I WILL CONTINUE THIS PROJECT IN THE FUTURE.

SkullCreator is a tiny library that makes creating custom player heads in CraftBukkit / Spigot / Paper plugins a breeze.  
Stop implementing **100MB** worth of independent plugins for **1MB** worth of code when you can just use this library .

> "Why spend minutes typing commands when you can spend _**EONS**_ writing code?" ~ **Probably You**

---

## Table of Contents
- [Features](#-features)
- [Quick Start](#-quick-start)
- [Usage](#-usage)
- [Running the Demo](#-running-the-demo)
- [Technology](#-technology)
- [Contributing](#-contributing)
- [License](#-license)
- [History](#-history)

---

## Features
- **Instant** skull generation from:
  - Player **name**
  - Player **UUID**
  - **Base64** texture string
  - Direct **Mojang texture URL**
- + Works on **Minecraft 1.8.x and newer** 
- - Zero external dependencies – just one **Jar** dependency / (or a couple source files)

### Compatibility Matrix

| Minecraft Version | Status | Platform |
|-------------------|:------:|:--------:|
| 1.21.5+           | ❓ |**Paper** (On others only the Item)
| 1.20.x            | ✅ |**Bukkit / Spigot / Paper**
| 1.19.x            | ✅ |**Bukkit / Spigot / Paper**
| 1.18.x            | ✅ |**Bukkit / Spigot / Paper**
| 1.17.x            | ✅ |**Bukkit / Spigot / Paper**
| 1.16.x            | ✅ |**Bukkit / Spigot / Paper**
| 1.12 – 1.8        | ✅ |**Bukkit / Spigot / Paper**
| < 1.8             | ❌ |**Bukkit / Spigot / Paper**

✅ = fully tested & working · ❓ = not yet tested · ❌ = not supported.

---

## Quick Start
1. **Add the dependency** (Gradle – Kotlin DSL):

   ```kotlin
   repositories {
       // add Paper repo for Spigot snapshots
       maven("https://repo.papermc.io/repository/maven-public/")
   }

   dependencies {
       compileOnly("io.github.diamondpixel:skullcreator:3.0.1")
   }
   ```

   <details>
   <summary>Maven (legacy)</summary>

   ```xml
   <dependency>
       <groupId>io.github.diamondpixel</groupId>
       <artifactId>skullcreator</artifactId>
       <version>3.0.1</version>
       <scope>compile</scope>
   </dependency>
   ```
   </details>

   Or just copy the latest **source code** into your plugin's `src/` folder.

2. **Create a skull**:

   ```java
   ItemStack skull = SkullCreator.itemFromName("diamondpixel");
   player.getInventory().addItem(skull);
   ```

---

## Usage
```java
// 1️⃣ From player name (changes when the player changes skin)
ItemStack nameHead = SkullCreator.itemFromName("diamondpixel");

// 2️⃣ From player UUID (also updates with skin changes)
UUID uuid = UUID.fromString("7cdf75c8-68dd-4aca-8706-d8137ad03d02");
ItemStack uuidHead = SkullCreator.itemFromUuid(uuid);

// 3️⃣ From Base64 texture string (ALWAYS the same texture)
String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0d..."; // shortened
ItemStack b64Head = SkullCreator.itemFromBase64(base64);

// 4️⃣ From Mojang texture URL (short & sweet)
String url = "http://textures.minecraft.net/texture/955d611a878e8...";
ItemStack urlHead = SkullCreator.itemFromUrl(url);
```

---

## Running the Demo
This repository ships with a small **demo plugin** showing the library in action.

```bash
# Clone & build the project
$ git clone https://github.com/diamondpixel/SkullCreator.git
$ cd SkullCreator
$ ./gradlew clean build                     # builds API + shaded demo jar
#   or only build the shaded demo jar
$ ./gradlew :SkullCreatorDemoPlugin:shadowJar

# Copy the resulting jar from
#   build/Demo/libs/SkullCreatorDemoPlugin-x.x.x-SNAPSHOT.jar
# into your Paper / Spigot server's plugins/ folder, then run:
> /skulltest comprehensive
```
The command will execute a comprehensive test of all features.


---

## Technology
- **Java 8**
- **Spigot / Paper API**
- **Gradle** build system (Kotlin DSL)

---

## Contributing
Contributions are welcome!  
If you have an idea, bug report, or pull request:
1. **Fork** the repository
2. Create a feature branch: `git checkout -b feat/my-awesome-feature`
3. Commit your changes with clear messages
4. Open a **pull request** – make sure CI passes
5. Wait for review (sometime)

Please follow the existing code style and include tests / demo updates where relevant.

---

## License
Distributed under the **MIT License** – see [`LICENSE`](LICENSE) for more information.

---

## History
Inspired by the awesome work done by **Deanveloper** in the original
[Deanveloper/SkullCreator](https://github.com/Deanveloper/SkullCreator).  
This fork takes the idea further with modern Java, updated APIs, and additional
features.

---

> Made with  &  by [@diamondpixel](https://github.com/diamondpixel)
