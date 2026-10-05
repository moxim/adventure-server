# Bundled fonts

Web fonts an author can choose for running an adventure (`AdventureFont`, declared in `../adventure-fonts.css`).

| Font | Role | Licence |
|------|------|---------|
| Inter | clean sans-serif | SIL OFL 1.1 (`LICENSE-inter.txt`) |
| Lora | readable serif | SIL OFL 1.1 (`LICENSE-lora.txt`) |
| IBM Plex Mono | retro terminal | SIL OFL 1.1 (`LICENSE-ibm-plex-mono.txt`) |
| MedievalSharp | fantasy | SIL OFL 1.1 (`LICENSE-medievalsharp.txt`) |
| Cinzel | fantasy | SIL OFL 1.1 (`LICENSE-cinzel.txt`) |
| Oxanium | futuristic | SIL OFL 1.1 (`LICENSE-oxanium.txt`) |
| Share Tech Mono | futuristic terminal | SIL OFL 1.1 (`LICENSE-share-tech-mono.txt`) |
| Special Elite | worn typewriter | **Apache 2.0** (`LICENSE-special-elite.txt`, with the upstream copyright line added on top) |
| IM Fell English | old print | SIL OFL 1.1 (`LICENSE-im-fell-english.txt`) |
| Courier Prime | typewriter | SIL OFL 1.1 (`LICENSE-courier-prime.txt`) |
| Asimovian | futuristic | SIL OFL 1.1 (`LICENSE-asimovian.txt`) |
| Audiowide | retro-futuristic | SIL OFL 1.1 (`LICENSE-audiowide.txt`) |
| UnifrakturMaguntia | blackletter | SIL OFL 1.1 (`LICENSE-unifrakturmaguntia.txt`) |

The `.woff2` files are the unmodified latin-subset builds from the `@fontsource/<name>` npm packages
(`files/<name>-latin-<weight>-normal.woff2`), which also supply the licence texts. Lora, Share Tech Mono, Audiowide and UnifrakturMaguntia declare Reserved Font Names; the files are used exactly as published, under their own names.

To add a font: fetch its
files and licence the same way, add `@font-face` rules to `adventure-fonts.css` and a constant to `AdventureFont`.
