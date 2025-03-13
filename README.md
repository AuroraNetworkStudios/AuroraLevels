# AuroraLevels

Highly customizable and feature-rich global player leveling plugin for Paper servers.
Give your player XP to level up via commands and plugin integrations, and reward them with money, items, permissions, 
or even custom rewards.

You can view the full documentation [here](https://docs.auroramc.gg/auroralevels).

## Features:
- Template based leveling system, so you don't need to configure all day long
- Multiple reward types and integrations
- Customizable GUI menus
- Milestones (as custom levels)
- Automatic reward correction if you change your leveling config later on
- support math formulas in leveling requirements and in rewards
- built in money/command/permission/AuraSkills-stat/mythic stats/mmolib stat/item reward types
- Mythic(Mobs) custom mechanics and conditions so your mobs can give XP or levels. You can even check if a player has a certain aurora level or not with MythicCrucible.
- PlaceholderAPI support
- Multiple economy support
- WorldGuard flags to control who can enter a zone
- Leaderboard


## Developer API

### Maven

```xml
<repository>
    <id>auroramc</id>
    <url>https://repo.auroramc.gg/releases/</url>
</repository>
```

```xml
<dependency>
    <groupId>gg.auroramc</groupId>
    <artifactId>AuroraLevels</artifactId>
    <version>{VERSION}</version>
    <scope>provided</scope>
</dependency>
```
### Gradle

**Groovy DSL:**
```gradle
repositories {
    maven {
        url "https://repo.auroramc.gg/releases/"
    }
}

dependencies {
    compileOnly 'gg.auroramc:AuroraLevels:{VERSION}'
}
```

**Kotlin DSL:**
```Gradle Kotlin DSL
repositories { 
    maven("https://repo.auroramc.gg/releases/")
}

dependencies { 
    compileOnly("gg.auroramc:AuroraLevels:{VERSION}")
}
```