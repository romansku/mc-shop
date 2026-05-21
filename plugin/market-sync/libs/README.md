# AuthMe compile dependency

Place `AuthMe-5.7.0-FORK-Universal.jar` here for Maven `system` scope (not committed).

Local download:

```bash
mkdir -p libs
curl -fsSL -o libs/AuthMe-5.7.0-FORK-Universal.jar \
  "https://cdn.modrinth.com/data/3IEZ9vol/versions/oezVemzR/AuthMe-5.7.0-FORK-Universal.jar"
```

CI downloads the same jar with cache (see `.github/actions/setup-authme-lib`).

Source: https://github.com/HaHaWTH/AuthMeReReloaded
