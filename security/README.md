# Nestwave's Device to Cloud Bridge Sensitive Data

This folder is a placeholder for sensitive data files that are required for proper working of Nestwave's D2CBridge.

> [!caution]
> Files, in this folder shall never be committed to the GIT repository, or should be encrypted using `git-crypt`.

## Sensitive Data

The file `sensitive-config.ini` defines environment variables used in `application.yml`.

An example of such file is:

```ini
JDBC_USERNAME=<username>
JDBC_PASSWORD=<password>
NSW_USERNAME=<mail@company.com>
NSW_PASSWORD=<password>
KEY_STORE_PASSWORD=<password>
```

The following variables are supported (*mandatory):

**NestWave Credentials (now Sequans)** 

NestCloud (now Sequans Location Service) is used by the D2CBridge to compute GNSS (GPS/Galileo) positions, in the place of the device.
Client should create its own account on <https://www.nw.do/register>.

> [!note]
> These parameters are optionals, as you will be prompted for them during compilation process if omitted.

- `NSW_USERNAME`: Your NestCloud email. 
- `NSW_PASSWORD`: Your NestCloud password.

**Combain Positioning Solutions (optional)**

Combain provides cellular, Wi-Fi, and bluetooth positioning, to complement GNSS indoor.
Client should contact Combain for more details.

- `COMBAIN_TOKEN`: Combain API token.

**Tracking Database:**

The tracking database is a PostgreSQL instance accessible to the D2CBridge that is used to store device positions and telemetry.
Client should deploy its own instance.

> [!note]
> The database is mandatory due to way Spring® handles datasource declarations,
> removing it is possible but requires to gut a lot of stuff from the project.

- `JDBC_HOST`: Database hostname.
- *`JDBC_USERNAME`: Database username.
- *`JDBC_PASSWORD`: Database password.

**Basic Partner Interface**

Basic Partner is a D2CBridge module that will send devices positions and telemetry to the specified URL.

- `BASIC_PARTNER_URL`: URL

**Traxmate (optional)**

Traxmate is a popular visualization platform for tracker devices.
Client should contact Traxmate or affiliate for an account creation.

> [!note]
> If omitted, then the Traxmate plugin is disabled.
> Traxmate is an example of a plugin done with an external partner and is not mandatory.

- `TRAXMATE_TOKEN`: Traxmate API token.

**HTTPS**

- *`KEY_STORE_PASSWORD`: password to decrypt SSL certificate from keystore.
