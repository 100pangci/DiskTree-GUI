#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

RELEASES_DIR="$ROOT_DIR/Releases"
rm -rf "$RELEASES_DIR"
mkdir -p "$RELEASES_DIR"

copy_if_exists() {
  local source_path="$1"
  local target_dir="$2"

  if [ -d "$source_path" ]; then
    mkdir -p "$target_dir"
    cp -R "$source_path"/. "$target_dir"/
  elif [ -f "$source_path" ]; then
    mkdir -p "$target_dir"
    cp "$source_path" "$target_dir"/
  fi
}

echo "==> Generating Gradle wrapper with Gradle 8.10"
gradle -b wrapper.gradle.kts wrapper --no-daemon

echo "==> Building Android debug"
./gradlew :composeApp:assembleDebug --no-daemon
copy_if_exists "$ROOT_DIR/composeApp/build/outputs/apk/debug" "$RELEASES_DIR/android/debug"

if [ -n "${ANDROID_KEYSTORE_PATH:-}" ] && [ -f "${ANDROID_KEYSTORE_PATH:-}" ] && [ -n "${ANDROID_KEYSTORE_PASSWORD:-}" ] && [ -n "${ANDROID_KEY_ALIAS:-}" ] && [ -n "${ANDROID_KEY_PASSWORD:-}" ]; then
  echo "==> Building Android release"
  ./gradlew :composeApp:assembleRelease --no-daemon
  copy_if_exists "$ROOT_DIR/composeApp/build/outputs/apk/release" "$RELEASES_DIR/android/release"
else
  echo "==> Skipping Android release build because signing environment variables are incomplete"
fi

echo "==> Building Desktop package for current OS"
./gradlew :composeApp:packageDistributionForCurrentOS --no-daemon
copy_if_exists "$ROOT_DIR/composeApp/build/compose/binaries" "$RELEASES_DIR/desktop"

echo "==> Release artifacts collected in $RELEASES_DIR"