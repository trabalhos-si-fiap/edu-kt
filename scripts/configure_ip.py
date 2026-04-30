#!/usr/bin/env python3
"""Configure the developer machine IP in the Android app's BASE_URL.

Detects the local LAN IP (or accepts one via --ip) and rewrites the
`BASE_URL` buildConfigField in app/build.gradle.kts so the Kotlin app
points to the FastAPI/Django backend running on this machine.

Usage:
    python scripts/configure_ip.py                 # auto-detect
    python scripts/configure_ip.py --ip 192.168.0.10
    python scripts/configure_ip.py --port 8001     # change port too
    python scripts/configure_ip.py --show          # print current value
"""
from __future__ import annotations

import argparse
import re
import socket
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
GRADLE_FILE = REPO_ROOT / "app" / "build.gradle.kts"
NETSEC_FILE = REPO_ROOT / "app" / "src" / "main" / "res" / "xml" / "network_security_config.xml"

URL_RE = re.compile(
    r'(buildConfigField\("String",\s*"BASE_URL",\s*"\\")'
    r'(http://)([\d.]+)(:)(\d+)(/api/\\""\))'
)
DOMAIN_RE = re.compile(
    r'(<domain\s+includeSubdomains="false">)([\d.]+)(</domain>)'
)


def detect_local_ip() -> str:
    """Return the preferred outbound IPv4 of this machine.

    Opens a UDP socket to a routable address (no packets sent) and reads
    the local address the kernel would use. Falls back to hostname lookup.
    """
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(("8.8.8.8", 80))
        return s.getsockname()[0]
    except OSError:
        return socket.gethostbyname(socket.gethostname())
    finally:
        s.close()


def read_current() -> tuple[str, int]:
    text = GRADLE_FILE.read_text()
    m = URL_RE.search(text)
    if not m:
        sys.exit(f"✗ Could not find BASE_URL in {GRADLE_FILE}")
    return m.group(3), int(m.group(5))


def write_new(ip: str, port: int) -> None:
    text = GRADLE_FILE.read_text()
    new_text, n = URL_RE.subn(
        lambda m: f"{m.group(1)}{m.group(2)}{ip}{m.group(4)}{port}{m.group(6)}",
        text,
    )
    if n != 1:
        sys.exit(f"✗ Failed to rewrite BASE_URL in {GRADLE_FILE} (matches: {n})")
    GRADLE_FILE.write_text(new_text)

    netsec = NETSEC_FILE.read_text()
    new_netsec, n = DOMAIN_RE.subn(
        lambda m: f"{m.group(1)}{ip}{m.group(3)}", netsec
    )
    if n != 1:
        sys.exit(f"✗ Failed to rewrite domain in {NETSEC_FILE} (matches: {n})")
    NETSEC_FILE.write_text(new_netsec)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ip", help="IP address to write (skips auto-detection)")
    parser.add_argument("--port", type=int, help="Backend port (defaults to current)")
    parser.add_argument("--show", action="store_true", help="Only show current value")
    args = parser.parse_args()

    current_ip, current_port = read_current()

    if args.show:
        print(f"BASE_URL → http://{current_ip}:{current_port}/api/")
        return 0

    new_ip = args.ip or detect_local_ip()
    new_port = args.port or current_port

    if new_ip == current_ip and new_port == current_port:
        print(f"= No change. BASE_URL already http://{new_ip}:{new_port}/api/")
        return 0

    write_new(new_ip, new_port)
    print(f"→ BASE_URL: http://{current_ip}:{current_port}/api/")
    print(f"           http://{new_ip}:{new_port}/api/")
    print(f"  network_security_config.xml domain → {new_ip}")
    print("  Rebuild the app for the change to take effect (e.g. `make build`).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
