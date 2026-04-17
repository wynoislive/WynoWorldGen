# Installation Guide

Setting up **WynoWorldGen** is straightforward. Follow these steps to ensure a professional deployment.

## Requirements
- **Java Version**: 21 or higher.
- **Server Software**: Paper, Spigot, or Folia (v1.20.1+ supported).
- **Database**: 
    - **SQLite** (Default, zero config required).
    - **MySQL v8.0+** (Recommended for high-performance setups).

## Step-by-Step Setup

1. **Download**: Obtain the latest `wynogen-x.x.x.jar` from the [GitHub Releases](https://github.com/wynoislive/WynoWorldGen/releases).
2. **First Run**: Place the JAR in your server's `plugins/` folder and start the server.
3. **Configuration**:
    - The plugin will generate a default `config.yml` and `messages.yml`.
    - Stop the server and configure your database settings if you plan to use MySQL.
4. **Permissions**: Assign necessary permissions to your administrative staff (see [[Commands and Permissions]]).
5. **Start**: Restart the server and enjoy!

## Database Setup (MySQL)
If you are moving from a small SMP to an enterprise-grade network, we recommend initializing a MySQL database.

```yaml
database:
  type: "MYSQL"
  DB_HOST: "localhost"
  DB_PORT: 3306
  DB_USER: "wyno_user"
  DB_PASSWORD: "secret_password"
  DB_NAME: "wyno_gen_db"
```

> [!NOTE]
> The plugin uses **HikariCP** for high-performance connection pooling, ensuring minimal impact on game ticks.

---
[[Home]] | [[Commands and Permissions]]