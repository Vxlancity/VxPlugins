# 🪝 VXHook

Smooth, physics-driven grappling hooks built for modern performance and zero server lag on **Paper**, **Purpur**, **Folia** and **Spigot** across **Minecraft 1.16.5 bis 1.21.11+**.

---

## ⚡ Features

- **True Vector Physics**: Smooth, natural pulls without teleportation glitches or rubberbanding. Reach up to 120 blocks with high velocity and vertical lift.
- **Smart Fall-Damage Check**: Dynamically dampens or cancels fall damage right after a hook launch until safe landing.
- **Fully Tunable Mechanics**: Adjust pull velocity, max reach, cooldown timers, and durability per hook (default: 100 uses).
- **Mending & Anvil Repair**: Repariere den Hook mit XP-Orbs oder im Amboss – die 100 Nutzungen und die Durability-Bar füllen sich nahtlos wieder auf!
- **Audio & Visual Polish**: Built-in support for custom particle trails (`CRIT`), sound pitches, and impact effects.
- **Folia, Paper & Purpur Native**: Asynchronous handling designed specifically for high-TPS, heavy-load servers (`folia-supported: true`).
- **Custom Model Data Ready**: Compatible with custom resource packs (`CustomModelData: 10001`) and custom crafting recipes.

---

## 🌐 Kompatibilität (1x .jar Datei für alle Versionen)

Die Datei `VXHook.jar` funktioniert auf:

| Minecraft Version | Paper / Purpur | Folia | Spigot / Bukkit | Java |
| :--- | :---: | :---: | :---: | :---: |
| **1.21.11 / 1.21.x** | ✅ | ✅ | ✅ | Java 21+ |
| **1.21 / 1.21.1 - 1.21.4** | ✅ | ✅ | ✅ | Java 21+ |
| **1.20.5 / 1.20.6** | ✅ | ✅ | ✅ | Java 21+ |
| **1.20 / 1.20.1 - 1.20.4** | ✅ | ✅ | ✅ | Java 17+ |
| **1.19 / 1.19.1 - 1.19.4** | ✅ | ✅ (ab 1.19.4) | ✅ | Java 17+ |
| **1.18 / 1.18.1 - 1.18.2** | ✅ | – | ✅ | Java 17+ |
| **1.17 / 1.17.1** | ✅ | – | ✅ | Java 17+ |
| **1.16.5** | ✅ | – | ✅ | Java 17+ |

---

## 🛠️ Commands & Permissions

| Command | Alias | Permission | Standard | Description |
| :--- | :--- | :--- | :--- | :--- |
| `/vxhook give <player> [amount]` | `/vxh give` | `vxhook.admin.give` | op | Gives a ready-to-use hook to a player |
| `/vxhook reload` | `/vxh reload` | `vxhook.admin.reload` | op | Reloads config.yml and recipes |
| *(Passive)* | – | `vxhook.use` | **true** (everyone) | Allows launching and using grappling hooks |
| *(Passive)* | – | `vxhook.bypass.cooldown` | op | Ignores configured hook cooldowns |

---

## 🔨 Crafting-Rezept

Formloses Rezept (Shapeless) im 2x2 Inventar oder 3x3 Werkbank:

```
1x Angel (FISHING_ROD) + 1x Schleimball (SLIME_BALL) ➔ 1x VXHook
```

*(In `config.yml` anpassbar und an-/abschaltbar)*

---

## 📦 Installation & Setup

1. Kopiere `VXHook.jar` in den Ordner `/plugins/` deines Servers.
2. Starte oder reloade den Server, um die Standardkonfiguration zu erzeugen.
3. Passe Physik, Cooldowns und Nachrichten nach Wunsch in `/plugins/VXHook/config.yml` an.
4. Für die Texturen liegt die fertige `VXHook-ResourcePack.zip` bereit (SHA-1: `4e89bada08501da2056cf19a38288c45f7657faa`).


