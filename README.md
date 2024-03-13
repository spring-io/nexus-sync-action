# Nexus Sync Action

A GitHub action for deploying to a Maven Central.

## Arguments

| Input                       | Description                                                                            | Usage    |
| --------------------------- | -------------------------------------------------------------------------------------- | -------- |
| `username`                  | Nexus username                                                                         | Required |
| `password`                  | Nexus password                                                                         | Required |
| `staging-profile-name`      | Nexus Staging Profile Name                                                             | Required |
| `dir`                       | Base directory for files to sync, defaults to "nexus"                                  | Optional |
| `create`                    | Automatically create repository, defaults to "false"                                   | Optional |
| `staging-repo-id`           | Nexus Staging Repo id                                                                  | Optional |
| `close`                     | Automatically close repository, defaults to "false".                                   | Optional |
| `drop-if-failure`           | Automatically drop repository, defaults to "false".                                    | Optional |
| `close-timeout`             | How long in seconds to wait slow nexus close operation, defaults to "600"              | Optional |
| `release`                   | Automatically release repository, defaults to "false".                                 | Optional |
| `release-auto-drop`         | Drop repo after release, defaults to "true".                                           | Optional |
| `release-timeout`           | How long in seconds to wait slow nexus release operation, defaults to "600"            | Optional |
| `generate-checksums`        | Generate checksums, defaults to "false"                                                | Optional |
| `generate-checksums-config` | Config to generate checksum files.                                                     | Optional |
| `url`                       | Base Nexus url, defaults to "https://oss.sonatype.org"                                 | Optional |
| `upload`                    | Upload files, defaults to "false".                                                     | Optional |
| `upload-parallel`           | How many files are uploaded parallel, defaults to "1"                                  | Optional |
| `nexus-timeout`             | How long in seconds to wait http requests to nexus, defaults to "0" meaning no timeout | Optional |

## Usage

See [action.yml](action.yml)

On default this action really does nothing unless needed configs are in place.

This normal example of an action does:

- Takes everything under base directory _nexus_
- Create _md5_ and _sha_ checksums
- Creates a stating repo
- Uploads everything under _nexus_ into newly create stating repo
- Closes it and wait closed state
- Releases it and wait a proper state
- After all this, you should have artifacts released

```yaml
name: Sync
on:
  workflow_dispatch:
jobs:
  sync:
    runs-on: ubuntu-latest
    - uses: jvalkeal/atestn@main
      with:
        username: ${{ secrets.NEXUS_USERNAME }}
        password: ${{ secrets.NEXUS_PASSWORD }}
        staging-profile-name: test
        create: true
        upload: true
        close: true
        release: true
        generate-checksums: true
```

## License

Nexus Sync Action is Open Source software released under the
[Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).
