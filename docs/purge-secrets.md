# Removing the committed keys from history

The repository's history still contains the server profiles and their WireGuard
private keys. Removing the files in a new commit does not help: every earlier
commit is still fetchable, and GitHub keeps unreferenced blobs reachable through
the API for some time. History has to be rewritten, and that cannot be done
through the GitHub API - only by force-pushing from a clone.

## Order matters

Regenerate the keys **first**. Anything that has been public should be assumed
copied, so the rewrite is damage limitation, not a fix.

1. Rotate the Surfshark credentials: generate a new key pair in the Surfshark
   dashboard, which invalidates the old one.
2. On your own server, generate a fresh key pair and update the peer:

   ```bash
   wg genkey | tee privatekey | wg pubkey > publickey
   ```

3. Rebuild `secrets/locations.json` with the new keys. It stays out of git.

## Rewrite

```bash
# git-filter-repo is the tool the Git project recommends; filter-branch is
# deprecated and much slower.
pip install git-filter-repo

git clone https://github.com/sheshocked/SurfShield SurfShield-clean
cd SurfShield-clean

git filter-repo \
  --path app/src/main/assets \
  --path core \
  --path-glob '*.conf' \
  --invert-paths

git push --force origin --all
git push --force origin --tags
```

This drops the whole `assets` directory - `locations.json` and all 29 leftover
`.conf` files - from every commit, and the unused empty `core` Rust crate with
it. The build no longer reads any of them: profiles come from the encrypted
vault instead.

## Afterwards

- Ask GitHub Support to garbage-collect unreachable objects, otherwise old blobs
  stay reachable by SHA for a while.
- Any fork or existing clone keeps the old history. There is nothing you can do
  about that, which is why rotating the keys comes first.
- Set the `LOCATIONS_JSON_B64` repository secret from the new file:

  ```bash
  base64 -w0 secrets/locations.json
  ```

- If the repository does not need to be public, making it private is the single
  most effective step here.
