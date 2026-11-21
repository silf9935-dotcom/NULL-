#!/usr/bin/env python3
"""Validate the mod's JSON resources, texture asset, and translation references."""

from __future__ import annotations

import json
import re
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LANG_DIR = ROOT / "src/main/resources/assets/null_remnant/lang"
JAVA_DIR = ROOT / "src/main/java"
PNG_PATH = ROOT / "src/main/resources/assets/null_remnant/textures/entity/remnant.png"

KEY_PATTERN = re.compile(
    r'"((?:(?:message|event|horror|stalker|anomaly|memory|sign|command|lore|hud|item)'
    r'\.null_remnant\.[A-Za-z0-9_.]+|itemGroup\.null_remnant))"'
)


def load_json(path: Path):
    try:
        with path.open("r", encoding="utf-8") as file:
            return json.load(file)
    except (OSError, json.JSONDecodeError) as error:
        raise SystemExit(f"Invalid JSON resource {path.relative_to(ROOT)}: {error}") from error


def main() -> None:
    mod = load_json(ROOT / "src/main/resources/fabric.mod.json")
    catalog = load_json(ROOT / "src/main/resources/data/null_remnant/event_catalog.json")
    lore = load_json(ROOT / "src/main/resources/data/null_remnant/lore.json")
    sounds = load_json(ROOT / "src/main/resources/assets/null_remnant/sounds.json")

    if mod.get("id") != "null_remnant":
        raise SystemExit("fabric.mod.json must declare id=null_remnant")
    if mod.get("environment") != "*":
        raise SystemExit("The mod must declare the intended common/client environment")
    entrypoints = mod.get("entrypoints", {})
    if not isinstance(entrypoints, dict) or not {"main", "client"}.issubset(entrypoints):
        raise SystemExit("fabric.mod.json must register both main and client entrypoints")
    dependencies = mod.get("depends", {})
    if dependencies.get("minecraft") != "~1.20.1" or dependencies.get("java") != ">=17":
        raise SystemExit("fabric.mod.json must target Minecraft 1.20.1 and Java 17")
    if len(catalog.get("stages", [])) != 5:
        raise SystemExit("event_catalog.json must describe exactly five stages")
    if len(lore.get("entries", [])) != 5:
        raise SystemExit("lore.json must contain exactly five lore entries")
    if not isinstance(sounds, dict):
        raise SystemExit("sounds.json must contain a JSON object")

    item_source = JAVA_DIR / "dev/nullremnant/item/RemnantItems.java"
    if not item_source.is_file():
        raise SystemExit("The dedicated creative-tab item registration is missing")
    item_code = item_source.read_text(encoding="utf-8")
    for required in ("remnant_spawn_egg", "Registries.ITEM_GROUP", "ItemGroups.SPAWN_EGGS"):
        if required not in item_code:
            raise SystemExit(f"RemnantItems.java is missing required registration: {required}")

    if not PNG_PATH.is_file():
        raise SystemExit("The Remnant entity texture is missing")
    png = PNG_PATH.read_bytes()
    if len(png) < 100 or not png.startswith(b"\x89PNG\r\n\x1a\n"):
        raise SystemExit("The Remnant entity texture is not a valid PNG file")
    width, height = struct.unpack(">II", png[16:24])
    if (width, height) != (64, 64):
        raise SystemExit(f"The Remnant model expects a 64x64 texture, got {width}x{height}")

    java_sources = sorted(JAVA_DIR.rglob("*.java"))
    referenced_keys: set[str] = set()
    for source in java_sources:
        text = source.read_text(encoding="utf-8")
        referenced_keys.update(KEY_PATTERN.findall(text))

    if "message.null_remnant.stage" in referenced_keys:
        referenced_keys.remove("message.null_remnant.stage")
        referenced_keys.update(
            f"message.null_remnant.stage{stage}" for stage in range(1, 5)
        )

    languages = {}
    for language in ("en_us", "ru_ru"):
        path = LANG_DIR / f"{language}.json"
        translations = load_json(path)
        if not isinstance(translations, dict):
            raise SystemExit(f"{path.name} must contain a JSON object")
        empty = sorted(key for key, value in translations.items()
                       if not isinstance(value, str) or not value.strip())
        if empty:
            raise SystemExit(f"{path.name} contains empty or non-string translations: {empty}")
        languages[language] = translations

    english_keys = set(languages["en_us"])
    russian_keys = set(languages["ru_ru"])
    if english_keys != russian_keys:
        missing_ru = sorted(english_keys - russian_keys)
        missing_en = sorted(russian_keys - english_keys)
        raise SystemExit(
            "Translation key sets differ. "
            f"Missing in ru_ru: {missing_ru}; missing in en_us: {missing_en}"
        )

    missing = sorted(referenced_keys - english_keys)
    if missing:
        raise SystemExit(f"Java code references missing translation keys: {missing}")

    placeholder_pattern = re.compile(r"%s")
    mismatched_placeholders = []
    for key in sorted(english_keys):
        english_count = len(placeholder_pattern.findall(languages["en_us"][key]))
        russian_count = len(placeholder_pattern.findall(languages["ru_ru"][key]))
        if english_count != russian_count:
            mismatched_placeholders.append(
                f"{key}: en_us={english_count}, ru_ru={russian_count}"
            )
    if mismatched_placeholders:
        raise SystemExit(
            "Translation placeholder counts differ: "
            + "; ".join(mismatched_placeholders)
        )

    print(
        "Resource validation passed: "
        f"{len(java_sources)} Java files, {len(english_keys)} shared translations, "
        f"{len(catalog['stages'])} stages, {len(lore['entries'])} lore entries, "
        f"64x64 entity texture ({len(png)} bytes)."
    )


if __name__ == "__main__":
    main()
