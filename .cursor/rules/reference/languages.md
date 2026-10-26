# Supported languages catalog — `DpLanguages` / `LanguageDataSource`

Full detail for `30-screen-language`. Do not delete lines from this file — edit here and keep the rule stub in sync.

This is the **single source of truth** for which languages an OrbitalSonic app offers and how each one maps to an Android `values-*` folder. Reference app: QiblaCompass (`DpLanguages.kt`, `getLanguagesList()`).

Used by:

- The language data source in `:data` (`LanguageDataSource` on new projects; legacy / reference apps name it **`DpLanguages`**) — its list must be built from this catalog.
- The localize skill (`sonicc-localize` / `sonic-localize` / `sonicg-localize`) — code → folder mapping, RTL flags, display names for the language MCQ.
- `StartupLanguageFragment` / `LanguageFragment` rows (`30-screen-language`): flag + display name come from the same list.

## Data source shape (reference app)

```kotlin
class DpLanguages {

    fun getLanguagesList(languageCode: String = ""): List<LanguageItem> {

        val list = arrayListOf(
            LanguageItem("en", "English", "English", R.drawable.flag_united_states_america, false),
            LanguageItem("af", "Afrikaans", "Afrikaans", R.drawable.flag_south_africa, false),
            // … one row per catalog entry below, same order …
        )

        return list.map {
            it.copy(selected = it.languageCode == languageCode)
        }
    }
}
```

`LanguageItem(languageCode, englishName, displayName, @DrawableRes flag, selected)`:

| Field | Rule |
|-------|------|
| `languageCode` | Column **Code** below — exactly as written (`zh-CN`, `zh-TW`, `ceb`, `jv`, …). This is what prefs store and what the localize skill reads |
| English name | Column **English name** — used for search / sort |
| Display name | Column **Display name** — `English name (native)`; shown in the list row. Not translated; `translatable="false"` if moved to resources |
| Flag | Column **Flag** — `flag_*` drawable in `:core-ui` (`09-resources-xml` prefix table) |
| `selected` | Computed from the saved code, never hardcoded `true` in the list |

- Keep **this order**: English first, then the catalog order below (alphabetical by English name as shipped).
- A product may ship a **subset**, never a code outside this catalog without asking. Adding a language = add the row here first, then `DpLanguages` / `LanguageDataSource`, then run the localize skill.
- New projects (`sonicc-setup-new-project`) name the class `LanguageDataSource` in `:data`; keep the same list and `LanguageItem` shape. Do not rename `DpLanguages` in an existing app just to match.

## Catalog (104 languages)

**Dir**: `LTR` / `RTL`. **Folder**: the Android resource folder the localize skill writes. `en` is the default `values/` source locale — never create `values-en/`.

| # | Code | English name | Display name | Flag | Folder | Dir | Notes |
|---|------|--------------|--------------|------|--------|-----|-------|
| 1 | `en` | English | English | `flag_united_states_america` | `values/` (source) | LTR | Default locale — skip in localization |
| 2 | `af` | Afrikaans | Afrikaans | `flag_south_africa` | `values-af` | LTR | |
| 3 | `sq` | Albanian | Albanian (shqiptare) | `flag_albania` | `values-sq` | LTR | |
| 4 | `am` | Amharic | Amharic (አማርኛ) | `flag_ethiopia` | `values-am` | LTR | Ethiopic script |
| 5 | `ar` | Arabic | Arabic (عربي) | `flag_saudi_arabia` | `values-ar` | **RTL** | 6 plural forms (zero/one/two/few/many/other) |
| 6 | `hy` | Armenian | Armenian (հայերեն) | `flag_armenia` | `values-hy` | LTR | |
| 7 | `az` | Azerbaijani | Azerbaijani (Azərbaycan) | `flag_azerbaijan` | `values-az` | LTR | Latin script |
| 8 | `eu` | Basque | Basque (euskara) | `flag_france` | `values-eu` | LTR | |
| 9 | `be` | Belarusian | Belarusian (беларуская) | `flag_belarus` | `values-be` | LTR | one/few/many/other plurals |
| 10 | `bn` | Bangla | Bangla (বাংলা) | `flag_bangladesh` | `values-bn` | LTR | Also called Bengali |
| 11 | `bs` | Bosnian | Bosnian (bosanski) | `flag_bosnia_herzegovina` | `values-bs` | LTR | one/few/other plurals |
| 12 | `bg` | Bulgarian | Bulgarian (български) | `flag_bulgaria` | `values-bg` | LTR | |
| 13 | `ca` | Catalan | Catalan (català) | `flag_spain` | `values-ca` | LTR | |
| 14 | `ceb` | Cebuano | Cebuano | `flag_philippines` | `values-b+ceb` | LTR | 3-letter code → BCP 47 `b+` folder |
| 15 | `ny` | Chichewa | Chichewa | `flag_malawi` | `values-ny` | LTR | |
| 16 | `zh-CN` | Chinese-Simplified | Chinese-Simplified (简体中文) | `flag_china` | `values-zh-rCN` | LTR | Region qualifier `-r` |
| 17 | `zh-TW` | Chinese-Traditional | Chinese-Traditional (中國傳統的) | `flag_china` | `values-zh-rTW` | LTR | Region qualifier `-r` |
| 18 | `co` | Corsican | Corsican (Corsu) | `flag_italy` | `values-co` | LTR | |
| 19 | `hr` | Croatian | Croatian (Hrvatski) | `flag_croatia` | `values-hr` | LTR | one/few/other plurals |
| 20 | `cs` | Czech | Czech (čeština) | `flag_czech_republic` | `values-cs` | LTR | one/few/many/other plurals |
| 21 | `da` | Danish | Danish (dansk) | `flag_denmark` | `values-da` | LTR | |
| 22 | `nl` | Dutch | Dutch (Nederlands) | `flag_netherlands` | `values-nl` | LTR | |
| 23 | `eo` | Esperanto | Esperanto | `flag_belgium` | `values-eo` | LTR | |
| 24 | `et` | Estonian | Estonian (eesti keel) | `flag_estonia` | `values-et` | LTR | |
| 25 | `tl` | Filipino | Filipino | `flag_philippines` | `values-tl` | LTR | Android maps `tl` → `fil`; `values-b+fil` is equivalent — keep one |
| 26 | `fi` | Finnish | Finnish (Suomalainen) | `flag_finland` | `values-fi` | LTR | |
| 27 | `fr` | French | French (Français) | `flag_france` | `values-fr` | LTR | |
| 28 | `fy` | Frisian | Frisian | `flag_netherlands` | `values-fy` | LTR | |
| 29 | `gl` | Galician | Galician (galego) | `flag_spain` | `values-gl` | LTR | |
| 30 | `ka` | Georgian | Georgian (ქართული) | `flag_georgia` | `values-ka` | LTR | |
| 31 | `de` | German | German (Deutsch) | `flag_germany` | `values-de` | LTR | |
| 32 | `el` | Greek | Greek (Ελληνικά) | `flag_greece` | `values-el` | LTR | |
| 33 | `gu` | Gujarati | Gujarati (ગુજરાતી) | `flag_india` | `values-gu` | LTR | |
| 34 | `ht` | Haitian Creole | Haitian Creole (Kreyòl ayisyen) | `flag_haiti` | `values-ht` | LTR | |
| 35 | `ha` | Hausa | Hausa | `flag_nigeria` | `values-ha` | LTR | |
| 36 | `haw` | Hawaiian | Hawaiian (Ōlelo Hawai) | `flag_united_states_america` | `values-b+haw` | LTR | 3-letter code → BCP 47 `b+` folder |
| 37 | `he` | Hebrew | Hebrew (עִברִית) | `flag_israel` | `values-iw` | **RTL** | Legacy Android code `iw`; `values-he` is accepted and equivalent — follow the project, never both |
| 38 | `hi` | Hindi | Hindi (हिन्दी) | `flag_india` | `values-hi` | LTR | |
| 39 | `hmn` | Hmong | Hmong (Hmoob) | `flag_china` | `values-b+hmn` | LTR | 3-letter code → BCP 47 `b+` folder |
| 40 | `hu` | Hungarian | Hungarian (Magyar) | `flag_hungary` | `values-hu` | LTR | |
| 41 | `is` | Icelandic | Icelandic (íslenskur) | `flag_iceland` | `values-is` | LTR | |
| 42 | `ig` | Igbo | Igbo | `flag_nigeria` | `values-ig` | LTR | |
| 43 | `id` | Indonesian | Indonesian | `flag_indonesia` | `values-in` | LTR | Legacy Android code `in`; `values-id` is accepted and equivalent — follow the project, never both |
| 44 | `ga` | Irish | Irish (Gaeilge) | `flag_ireland` | `values-ga` | LTR | one/two/few/many/other plurals |
| 45 | `it` | Italian | Italian | `flag_italy` | `values-it` | LTR | |
| 46 | `ja` | Japanese | Japanese (日本) | `flag_japan` | `values-ja` | LTR | No plural distinction (`other` only) |
| 47 | `jv` | Javanese | Javanese (basa jawa) | `flag_indonesia` | `values-jv` | LTR | ISO 639-1 is `jv`. The shipped QiblaCompass list used the withdrawn code `jw` — use `jv` in `DpLanguages`, prefs and folder; map a saved legacy `jw` to `jv` when reading prefs |
| 48 | `kn` | Kannada | Kannada (ಕನ್ನಡ) | `flag_india` | `values-kn` | LTR | |
| 49 | `kk` | Kazakh | Kazakh (қазақ) | `flag_kazakhstan` | `values-kk` | LTR | |
| 50 | `km` | Khmer | Khmer (ខ្មែរ) | `flag_cambodia` | `values-km` | LTR | |
| 51 | `ko` | Korean | Korean (한국인) | `flag_south_korea` | `values-ko` | LTR | No plural distinction (`other` only) |
| 52 | `ku` | Kurdish | Kurdish | `flag_iraq` | `values-ku` | LTR | Kurmanji (Latin). Sorani would be `values-b+ckb` RTL — ask before switching |
| 53 | `ky` | Kyrgyz | Kyrgyz (Кыргызча) | `flag_kyrgyzstan` | `values-ky` | LTR | |
| 54 | `lo` | Lao | Lao (ພາສາລາວ) | `flag_lao_republic` | `values-lo` | LTR | |
| 55 | `la` | Latin | Latin | `flag_spain` | `values-la` | LTR | |
| 56 | `lv` | Latvian | Latvian (latviski) | `flag_latvia` | `values-lv` | LTR | zero/one/other plurals |
| 57 | `lt` | Lithuanian | Lithuanian (lietuvių) | `flag_lithuania` | `values-lt` | LTR | one/few/many/other plurals |
| 58 | `lb` | Luxembourgish | Luxembourgish (lëtzebuergesch) | `flag_luxembourg` | `values-lb` | LTR | |
| 59 | `mk` | Macedonian | Macedonian (македонски) | `flag_macedonia` | `values-mk` | LTR | |
| 60 | `mg` | Malagasy | Malagasy | `flag_madagascar` | `values-mg` | LTR | |
| 61 | `ms` | Malay | Malay | `flag_malaysia` | `values-ms` | LTR | No plural distinction (`other` only) |
| 62 | `ml` | Malayalam | Malayalam (മലയാളം) | `flag_india` | `values-ml` | LTR | |
| 63 | `mt` | Maltese | Maltese | `flag_malta` | `values-mt` | LTR | one/few/many/other plurals |
| 64 | `mi` | Maori | Maori | `flag_new_zealand` | `values-mi` | LTR | |
| 65 | `mr` | Marathi | Marathi (मराठी) | `flag_india` | `values-mr` | LTR | |
| 66 | `mn` | Mongolian | Mongolian (Монгол) | `flag_mongolia` | `values-mn` | LTR | Cyrillic |
| 67 | `my` | Myanmar | Myanmar | `flag_myanmar` | `values-my` | LTR | Burmese |
| 68 | `ne` | Nepali | Nepali (नेपाली) | `flag_nepal` | `values-ne` | LTR | |
| 69 | `no` | Norwegian | Norwegian (norsk) | `flag_norway` | `values-no` | LTR | Android Studio offers `nb`; keep **one** folder — reuse the project's existing one if present |
| 70 | `ps` | Pashto | Pashto (پښتو) | `flag_pakistan` | `values-ps` | **RTL** | |
| 71 | `fa` | Persian | Persian (فارسي) | `flag_iran` | `values-fa` | **RTL** | |
| 72 | `pl` | Polish | Polish (Polski) | `flag_poland` | `values-pl` | LTR | one/few/many/other plurals |
| 73 | `pt` | Portuguese | Portuguese | `flag_portugal` | `values-pt` | LTR | `pt-BR` is a separate product decision (`values-pt-rBR`) |
| 74 | `pa` | Punjabi | Punjabi (ਪੰਜਾਬੀ) | `flag_india` | `values-pa` | LTR | Gurmukhi script |
| 75 | `ro` | Romanian | Romanian (Română) | `flag_romania` | `values-ro` | LTR | one/few/other plurals |
| 76 | `ru` | Russian | Russian (Русский) | `flag_russian_federation` | `values-ru` | LTR | one/few/many/other plurals |
| 77 | `sm` | Samoan | Samoan | `flag_samoa` | `values-sm` | LTR | |
| 78 | `gd` | Scottish Gaelic | Scottish Gaelic (Gàidhlig na h-Alba) | `flag_samoa` | `values-gd` | LTR | Flag as shipped in the reference app — fix per product (UK / Scotland) |
| 79 | `sr` | Serbian | Serbian (Српски) | `flag_serbia` | `values-sr` | LTR | Cyrillic (display name is Cyrillic); one/few/other plurals |
| 80 | `st` | Sesotho | Sesotho | `flag_south_africa` | `values-st` | LTR | |
| 81 | `sn` | Shona | Shona | `flag_zimbabwe` | `values-sn` | LTR | |
| 82 | `sd` | Sindhi | Sindhi (سنڌي) | `flag_pakistan` | `values-sd` | **RTL** | |
| 83 | `si` | Sinhala | Sinhala (සිංහල) | `flag_sri_lanka` | `values-si` | LTR | |
| 84 | `sk` | Slovak | Slovak (slovenský) | `flag_slovakia` | `values-sk` | LTR | one/few/many/other plurals |
| 85 | `sl` | Slovenian | Slovenian (Slovenščina) | `flag_slovenia` | `values-sl` | LTR | one/two/few/other plurals |
| 86 | `so` | Somali | Somali | `flag_somalia` | `values-so` | LTR | |
| 87 | `es` | Spanish | Spanish (Española) | `flag_spain` | `values-es` | LTR | |
| 88 | `su` | Sundanese | Sundanese (basa Sunda) | `flag_indonesia` | `values-su` | LTR | |
| 89 | `sw` | Swahili | Swahili (kiswahili) | `flag_kenya` | `values-sw` | LTR | |
| 90 | `sv` | Swedish | Swedish (svenska) | `flag_sweden` | `values-sv` | LTR | |
| 91 | `tg` | Tajik | Tajik (тоҷикӣ) | `flag_tajikistan` | `values-tg` | LTR | |
| 92 | `ta` | Tamil | Tamil (தமிழ்) | `flag_india` | `values-ta` | LTR | |
| 93 | `te` | Telugu | Telugu (తెలుగు) | `flag_india` | `values-te` | LTR | |
| 94 | `th` | Thai | Thai (ไทย) | `flag_thailand` | `values-th` | LTR | No plural distinction (`other` only) |
| 95 | `tr` | Turkish | Turkish (Türkçe) | `flag_turkey` | `values-tr` | LTR | |
| 96 | `uk` | Ukrainian | Ukrainian (український) | `flag_ukraine` | `values-uk` | LTR | one/few/many/other plurals |
| 97 | `ur` | Urdu | Urdu (اردو) | `flag_pakistan` | `values-ur` | **RTL** | |
| 98 | `uz` | Uzbek | Uzbek | `flag_uzbekistan` | `values-uz` | LTR | Latin script |
| 99 | `vi` | Vietnamese | Vietnamese (Tiếng Việt) | `flag_viet_nam` | `values-vi` | LTR | No plural distinction (`other` only) |
| 100 | `cy` | Welsh | Welsh (Cymraeg) | `flag_wallis_and_futuna` | `values-cy` | LTR | Flag as shipped in the reference app — fix per product (UK / Wales); zero/one/two/few/many/other plurals |
| 101 | `xh` | Xhosa | Xhosa (isiXhosa) | `flag_south_africa` | `values-xh` | LTR | |
| 102 | `yi` | Yiddish | Yiddish (יידיש) | `flag_israel` | `values-ji` | **RTL** | Legacy Android code `ji`; `values-yi` is accepted and equivalent — follow the project, never both |
| 103 | `yo` | Yoruba | Yoruba | `flag_nigeria` | `values-yo` | LTR | |
| 104 | `zu` | Zulu | Zulu | `flag_south_africa` | `values-zu` | LTR | |

RTL set (7): `ar`, `he`, `ps`, `fa`, `sd`, `ur`, `yi` (+ `ku` only if the product switches to Sorani `ckb`). Every RTL locale needs `android:supportsRtl="true"` (already on the template manifest) and `anim-ldrtl/` nav transitions (`09-resources-xml`).

## Code → folder rules

Apply in this order; the table above is the result.

1. **Source locale** `en` → `values/`. Never `values-en/`.
2. **Two-letter code, no region** → `values-<code>` (`ur` → `values-ur`).
3. **Legacy Android codes** — AAPT historically used the old ISO codes; both spellings resolve to the same locale, so a project must have **one** folder per language:
   - `he` → `values-iw` (or `values-he`)
   - `id` → `values-in` (or `values-id`)
   - `yi` → `values-ji` (or `values-yi`)
   Prefer the legacy folder on a fresh project; on an existing project reuse whichever already exists. Never create both.
4. **Withdrawn ISO codes are not allowed in the app list**: Javanese is `jv` (`jw` was withdrawn in 2001), so code, saved pref and folder all use `jv` → `values-jv`. Only Android's own legacy folder codes (rule 3) may differ from the app code.
5. **Code with region** `xx-YY` → `values-xx-rYY` (`zh-CN` → `values-zh-rCN`, `zh-TW` → `values-zh-rTW`, `pt-BR` → `values-pt-rBR`, `fr-CA` → `values-fr-rCA`).
6. **Three-letter code** (ISO 639-2/3) → BCP 47 folder `values-b+<code>` (`ceb` → `values-b+ceb`, `haw` → `values-b+haw`, `hmn` → `values-b+hmn`). Region on a 3-letter code: `values-b+<code>+<REGION>`.
7. **Filipino** `tl` → `values-tl` (Android treats it as `fil`; `values-b+fil` is equivalent — keep one).
8. **Norwegian** `no` → `values-no` (Android Studio suggests `nb`; keep one).
9. Anything not in this catalog → stop and ask before creating a folder.

## Localization notes per script family

- **RTL** (`ar`, `he`, `fa`, `ps`, `ur`, `sd`, `yi`): keep placeholders in logical order; wrap mixed-direction placeholders in LRM (`‎`) when a Latin token sits inside RTL text; test with "Force RTL layout".
- **CJK** (`zh-CN`, `zh-TW`, `ja`, `ko`): no plural forms beyond `other`; avoid Latin ellipsis / spacing conventions; full-width punctuation.
- **Indic / Southeast Asian** (`hi`, `bn`, `gu`, `kn`, `ml`, `mr`, `ne`, `pa`, `ta`, `te`, `si`, `th`, `lo`, `km`, `my`): check font fallback on `TextStyle.*` (Poppins has no glyphs) — do not add fonts without approval.
- **Slavic / Baltic** (`ru`, `uk`, `be`, `pl`, `cs`, `sk`, `hr`, `bs`, `sr`, `sl`, `lt`, `lv`): `<plurals>` must carry every CLDR quantity the locale uses, not a copied `one` / `other`.

## Forbidden

- Hardcoding this list in Fragments / ViewModels / `*ScreenContent` — it lives in `DpLanguages` / `LanguageDataSource` (`:data`) only
- A language code in the app that is not in this catalog (ask first, then add the row here)
- Two folders for the same language (`values-he` **and** `values-iw`, `values-id` **and** `values-in`)
- `values-en/`, `values-jw/`, `values-ceb/` (invalid or duplicate qualifiers); `"jw"` as an app code (withdrawn — use `jv`)
- Translating the display-name column — it is already `English (native)` by design
