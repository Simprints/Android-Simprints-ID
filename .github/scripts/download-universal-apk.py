#!/usr/bin/env python3
"""
Download the Play-signed universal APK for a given version code from Google Play.

Uses the Android Publisher API generatedApks resource. Google Play generates APKs
from an uploaded AAB asynchronously, so this script retries until the APK is ready
or MAX_WAIT_SECONDS is exceeded.

Required environment variables:
  GOOGLE_API_KEY_JSON   Service account JSON (as a string, not a file path)
  VERSION_CODE          Integer Android version code
  PACKAGE_NAME          Android package name (default: com.simprints.id)
  OUTPUT_APK            Output file path (default: universal.apk)
  MAX_WAIT_SECONDS      Maximum seconds to wait for Play to finish processing (default: 600)
"""

import json
import os
import sys
import time

from google.oauth2.service_account import Credentials
from googleapiclient.discovery import build
from googleapiclient.http import MediaIoBaseDownload

RETRY_INTERVAL_SECONDS = 30


def build_service(service_account_json: str):
    creds = Credentials.from_service_account_info(
        json.loads(service_account_json),
        scopes=["https://www.googleapis.com/auth/androidpublisher"],
    )
    return build("androidpublisher", "v3", credentials=creds, cache_discovery=False)


def find_universal_apk_download_id(service, package_name: str, version_code: int) -> str | None:
    result = service.generatedapks().list(
        packageName=package_name,
        versionCode=version_code,
    ).execute()

    for apk in result.get("generatedApks", []):
        universal = apk.get("generatedUniversalApk")
        if universal:
            return universal["downloadId"]
    return None


def download_apk(service, package_name: str, version_code: int, download_id: str, output_path: str):
    request = service.generatedapks().download_media(
        packageName=package_name,
        versionCode=version_code,
        downloadId=download_id,
    )
    with open(output_path, "wb") as f:
        downloader = MediaIoBaseDownload(f, request)
        done = False
        while not done:
            status, done = downloader.next_chunk()
            if status:
                print(f"  {int(status.progress() * 100)}%", flush=True)

    size_mb = os.path.getsize(output_path) / 1_048_576
    print(f"Saved to {output_path} ({size_mb:.1f} MB)")


def main():
    service_account_json = os.environ["GOOGLE_API_KEY_JSON"]
    package_name = os.environ.get("PACKAGE_NAME", "com.simprints.id")
    version_code = int(os.environ["VERSION_CODE"])
    output_path = os.environ.get("OUTPUT_APK", "universal.apk")
    max_wait = int(os.environ.get("MAX_WAIT_SECONDS", "600"))

    print(f"Looking for universal APK: package={package_name} versionCode={version_code}")

    service = build_service(service_account_json)
    deadline = time.monotonic() + max_wait
    attempt = 0

    while True:
        attempt += 1
        try:
            download_id = find_universal_apk_download_id(service, package_name, version_code)
            if download_id:
                print(f"Universal APK ready (attempt {attempt}): downloadId={download_id}")
                break
            print(f"Attempt {attempt}: APKs not generated yet by Play Store.")
        except Exception as exc:
            print(f"Attempt {attempt}: API error — {exc}")

        remaining = deadline - time.monotonic()
        if remaining <= 0:
            print(f"ERROR: Universal APK not available after {max_wait}s", file=sys.stderr)
            sys.exit(1)

        wait = min(RETRY_INTERVAL_SECONDS, remaining)
        print(f"Retrying in {int(wait)}s... ({int(remaining)}s remaining)")
        time.sleep(wait)

    print("Downloading...")
    download_apk(service, package_name, version_code, download_id, output_path)


if __name__ == "__main__":
    main()
