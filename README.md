Toolshed
========

*The shed where the self-hosted tools live.*

An offline Android reference for the services people run on their own hardware.

Fifty-one entries, each answering the same five questions: what is it for, how
hard is it to install, what does it cost in memory and disk, what does a working
compose file look like, and what will go wrong.

Brought to you by Kituwa IT — <https://kituwa.com>

---

## What it is

Toolshed is documentation that fits in your pocket, for the moment when a
container will not start and you would rather not read a forum.

Most self-hosted guides tell you the features. This one is built around the
failures instead, because the features are on the project's own website and the
failures are the part that costs you an evening. Every entry has a **Notes**
section for the things that go wrong and why they go wrong:

- the read-only volume mount that removes an entire category of data loss
- the Postgres image you must not swap for a plain one
- the UDP port that makes video calls connect and then carry no audio
- the one-line change that takes an OIDC login from a redirect loop to working
- why a Docker socket mounted read-only is still root on the host

## Features

- **51 services** across photos and media, files and documents, productivity,
  chat, automation, monitoring, identity and security, networking, databases,
  business, and home
- **A working compose file for each one**, with the values that cause first-run
  trouble left as environment variables and a note on how to generate each
- **Installation guides** written as ordered steps, because the order matters
  more often than the content
- **Resource estimates** per service: minimum and typical RAM, disk, and cores
- **5 starter stacks** — complete multi-service setups for a small VPS, a photo
  vault, a media box, remote access, and family paperwork
- **A resource planner** that totals a shortlist and compares it against a
  Raspberry Pi 5, a mini PC, a NAS, or a small VPS
- **Search and category filters** across names, summaries, and descriptions
- **Shortlist and running marks**, stored locally, feeding the planner

## Privacy

There is nothing to opt out of, because there is nothing happening.

- **No permissions of its own.** The manifest declares none: no `INTERNET`, no
  storage, no files, no location, no identifiers. Nothing can be granted because
  nothing is requested, and Android enforces that rather than the author
  promising it. (The merged manifest does contain one AndroidX-internal
  signature-level permission for the profile installer, which grants nothing and
  is surfaced nowhere.)
- **No analytics, no crash reporting, no account, no telemetry**
- Every guide, compose file, and stack is bundled in the APK
- The only two things the app can do are open a link in your browser and put
  text on your clipboard
- Your shortlist and running marks are three DataStore preferences on the device

The app does not need internet access, and the absence of the permission is
enforced by the system rather than promised in a privacy policy. That is the
difference between a claim and a guarantee.

## A note on licences

Several popular services in the catalogue are **not** open source: n8n, Outline,
Invoice Ninja, NocoDB, HashiCorp Vault, and Redis use source-available licences
(Sustainable Use, BSL, Elastic, BUSL, RSALv2/SSPL). They are listed because they
are genuinely good at what they do, and each of those entries states which
licence it is and what that means in practice.

Toolshed does not bundle any of them — it documents them as services you run
yourself, which is the same way it documents every other entry. Toolshed itself
is **GPL-3.0-or-later**.

The resource figures are planning estimates, not vendor guarantees. The About
screen says so too.

## Building

Requires a JDK 17 or newer and the Android SDK (compileSdk 35).

```
./gradlew assembleDebug
./gradlew test
./gradlew lint
```

Point the SDK location at `local.properties`:

```
sdk.dir=/path/to/android-sdk
```

A clean checkout builds without `local.properties`; AGP picks the SDK up from
`ANDROID_HOME` instead, which is how F-Droid builds it.

### On the release build

Release builds run R8 with resource shrinking. That is not a default setting and
it is worth knowing why:

| | APK | dex |
|---|---|---|
| Extended icon library, no shrinking | 11.2 MB | 42.4 MB |
| Local icon drawables, no shrinking | 7.0 MB | 20.1 MB |
| Local icon drawables, R8 | **1.48 MB** | 2.94 MB |

`material-icons-extended` ships roughly two thousand icons as generated Kotlin.
The app used fourteen. With minification off, none of the rest was removed, so
an offline reference whose entire content is a few hundred kilobytes of text was
shipping 42 MB of dex. The six icons that are not in `material-icons-core` now
live in `res/drawable` as ordinary vector drawables, and R8 strips the rest of
what Compose brings in.

Two consequences to be aware of:

- The minified build is the one that gets verified on a device, because
  obfuscation is exactly the kind of change that breaks reflective code.
  DataStore is the risk here, so the shortlist is marked, the process is killed,
  and the mark is checked again after a cold start.
- `app/proguard-rules.pro` keeps `SourceFile` and `LineNumberTable` so a stack
  trace from a release build is still readable.
- Three lint checks are switched off in `app/build.gradle.kts`, with the
  reasoning written next to them: `GradleDependency`, `AndroidGradlePluginVersion`,
  and `OldTargetApi` only ever report that a newer version exists. Versions are
  pinned on purpose, because F-Droid builds from this file and an unattended
  dependency bump is a build risk rather than a feature.

The verification that matters is the minified one. Obfuscation is exactly the
kind of change that breaks reflective code, and DataStore is the reflective
surface here, so the release build is signed, installed, a service is
shortlisted, the process is killed outright, and the mark is checked again after
a cold start. R8 kept 562 DataStore classes including the protobuf types, and
the round trip works.

### Regenerating the icon

`metadata/icon-source.svg` is the editable source. `res/drawable/ic_launcher_*`
are the Android vector drawables, kept in step with it by hand. The F-Droid
icon is rendered from the SVG:

```
inkscape metadata/icon-source.svg -w 512 -h 512 \
  -o metadata/android/en-US/images/icon/icon.png
```


## Tests

56 unit tests, no device required.

The interesting ones are the integrity tests, because the catalogue is data
compiled into the app and the ways it can be wrong are structural rather than
obvious:

- every `id` is unique, and every cross-reference resolves — a service
  recommending an alternative that is not in the catalogue, or a stack naming a
  service that does not exist, fails the build
- every compose file passes a structural YAML lint: no tabs, no duplicate keys in
  the same mapping, no unterminated quoted value, no entry with a missing key
- **every service's advertised port appears in its own compose file**, and no two
  services inside one starter stack bind the same host port
- resource figures are plausible: typical RAM is never below the stated minimum,
  cores are in a believable range, ports are valid
- links are absolute HTTPS URLs, licences are named, and no category is empty
- the F-Droid metadata exists, fits the length limits, and `fdroid/versions.yml`
  still matches the version being built

The port check exists because of a real bug it found. Six entries advertised a
port in their detail header that their own bundled compose file did not publish:
Pi-hole said 80 and shipped 8080, Nextcloud, Seafile, Zammad, EspoCRM, and
Akaunting all had the same disagreement. A reader follows the compose file,
goes to the address the app told them to use, and finds nothing listening. Six
entries were wrong in the same direction, which is what happens when a field is
filled in from memory instead of from the file it describes.

The YAML lint is worth a note. It is not a parser; it catches the four mistakes
that actually occur when a compose file is written by hand, and the duplicate-key
check earned its place immediately by finding a repeated `container_name` in a
draft.

The tokenizer behind the syntax highlighting has a round-trip property test:
concatenating every token's text must reproduce the input exactly. That test
caught two real bugs — `String.lines()` yielding a trailing empty element for
text ending in a newline, and a trailing comment being swallowed because the
space before the `#` was never consumed.

## Licence

GPL-3.0-or-later. See [LICENSE](LICENSE).

You are free to inspect it, change it, and run your own copy.

## F-Droid

The build metadata for the submission lives in a fork of
`gitlab.com/fdroid/fdroiddata`, not in this repository. What belongs here is
the source plus both copies of the store metadata, because F-Droid reads the
listing text from Fastlane:

- `fastlane/metadata/android/en-US/` — what F-Droid shows in the client and on
  the website (`short_description.txt`, `full_description.txt`, changelogs,
  icon, screenshots)
- `metadata/android/en-US/` — the same content, for Play-style tooling and for
  anyone who looks in the conventional place

F-Droid's builder clones this repository anonymously and runs
`./gradlew assembleRelease`, so the source must be public and the build must
work with no `local.properties`. Both are verified in CI-style before each
submission. The APK is intentionally unsigned: F-Droid signs it with its own
key, so no keystore belongs in this repository.
