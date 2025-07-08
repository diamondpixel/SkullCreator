# 💀 SkullCreator

![Java](https://img.shields.io/badge/language-Java%2021-orange)
![Minecraft](https://img.shields.io/badge/MC-1.12%2B-green)
![License](https://img.shields.io/github/license/diamondpixel/SkullCreator)

SkullCreator is a tiny library that makes creating custom player heads in CraftBukkit / Spigot / Paper plugins a breeze.  
Stop implementing **100MB** worth of independent plugins for **1MB** worth of code when you can just use this library 😎.

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
- Works on **Minecraft 1.12.2 and newer**
- Zero external dependencies – just one **Jar** dependency / (or a couple source files)

---

## Quick Start
1. **Add the dependency** (Maven example):

   ```xml
   <dependency>
       <groupId>com.github.diamondpixel</groupId>
       <artifactId>skullcreator</artifactId>
       <version>2.0.3</version>
       <scope>compile</scope> <!-- because the server doesn't have it -->
   </dependency>
   ```

   <details>
   <summary>Gradle (Kotlin DSL)</summary>

   ```kotlin
   compileOnly("com.github.diamondpixel:skullcreator:2.0.3")
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
$ mvn -pl SkullCreatorDemo -am clean package

# Drop the demo jar into your Paper / Spigot server's plugins/ folder
# Start the server and run the command in-game:
> /skulltest comprehensive
```
The command will run a comprehensive test of all features.


---

## Technology
- **Java 21** 
- **Spigot / Paper API**
- **Maven** build files supplied

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