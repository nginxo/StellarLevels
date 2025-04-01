
# 📊 StellarLevels Plugin

![License](https://img.shields.io/badge/license-MIT-green)
![Version](https://img.shields.io/badge/version-1.0-blue)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21%2B-orange)
![PlaceholderAPI](https://img.shields.io/badge/Supports-PlaceholderAPI-brightgreen)
![Vault](https://img.shields.io/badge/Depends-Vault-yellow)

Un plugin per Minecraft che gestisce i livelli e l'esperienza dei giocatori. Supporta ricompense personalizzate, comandi amministrativi e integrazione con Discord per notifiche. Perfetto per server RPG e survival!

## 📋 Caratteristiche

- **Sistema di Livelli Personalizzabile**: Gestisci i livelli dei giocatori con esperienza personalizzabile.
- **Ricompense Basate sul Livello**: Concedi ricompense come denaro, oggetti, permessi, e altro.
- **Integrazione Discord**: Notifiche automatiche su Discord per tenere traccia delle azioni amministrative.
- **PlaceholderAPI Supportato**: Mostra informazioni sui livelli dei giocatori con placeholder personalizzati.
- **Comandi Amministrativi**: Imposta, aggiungi, rimuovi o resetta livelli ed esperienza con comandi facili.

## ⚙️ Configurazione

### `config.yml`

```yaml
max-level: 85
base-exp: 100.0

messages:
  level-up: "&aComplimenti! Sei salito al livello %level%!"
  max-level-reached: "&cHai raggiunto il livello massimo!"
  current-level: "&eIl tuo livello attuale è: %level% (EXP: %current_exp%/%exp_required%)"
  exp-gained: "&bHai guadagnato %exp% esperienza. (Totale: %current_exp%)"
  player-not-found: "&cGiocatore non trovato!"
  non-player-command: "&cSolo i giocatori possono usare questo comando!"
  invalid-exp-amount: "&cQuantità di EXP non valida."
  command-usage-exp: "&eUsa: /levelsystem <level/exp> <set/add/take/reset> <amount> <player>"

database:
  use: "h2" # "h2" per H2, "mysql" per MySQL/MariaDB.
  mysql:
    host: "localhost"
    port: 3306
    database: "levelsystem"
    username: "root"
    password: "password"

discord:
  webhook-url: "https://discord.com/api/webhooks/your-webhook-id/your-webhook-token"
```

### `rewards.yml`

```yaml
rewards:
  money:
    type: money
    value: 100.0
    level: 1
  perm:
    type: permission
    value: "plugin.permission.use"
    level: 5
  item:
    type: item
    value: "DIAMOND"
    amount: 5
    level: 10
  group:
    type: group
    value: "VIP"
    level: 15
  command:
    type: command
    value: "say hello"
    level: 2
```

## 🔧 Comandi

- **`/livello`**: Visualizza il livello e l'esperienza attuale del giocatore.
- **`/levelsystem`**: Comandi per amministratori per gestire i livelli e l'esperienza.
  - **Usage**: `/levelsystem <level/exp> <set/add/take/reset> <amount> <player>`
  - **Aliases**: `/ls`
  - **Permission**: `levelsystem.admin`
- **`/reloadls`**: Ricarica i file di configurazione.
  - **Aliases**: `/rls`
  - **Permission**: `levelsystem.admin`

## 🚀 Setup e Installazione

1. Scarica il file `.jar` del plugin LevelSystem.
2. Metti il file `.jar` nella cartella `plugins` del tuo server Minecraft.
3. Riavvia il server per generare i file di configurazione.
4. Modifica `config.yml` e `rewards.yml` come desideri.
5. Usa il comando `/reloadls` per applicare i cambiamenti.

## 🧩 PlaceholderAPI Integration

Supporta i seguenti placeholder:

- **`%levelSystem_level%`**: Mostra il livello attuale del giocatore.
- **`%levelSystem_exp%`**: Mostra l'esperienza attuale e quella necessaria per il prossimo livello.

## 🤝 Supporto

Per supporto, suggerimenti o per segnalare problemi, contatta l'autore del plugin, VersyLiones, o visita la pagina di supporto del plugin.

Divertiti a gestire i livelli dei giocatori con LevelSystem!
