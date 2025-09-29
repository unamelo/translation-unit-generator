 # Translation Unit Auto Generator

  A little helper for PhpStorm (and friends) that turns highlighted text into reusable translation units. It understands `.php`, `.js`, and `.twig` files,
  swaps your selection for the right translation helper, and updates your XLIFF catalog on the spot.

  ## Highlights

  - Right-click → “Generate Translation Unit” (or hit `Ctrl+Alt+T` / `⌘⌥T`) to wrap the selection with your configured template.
  - Keeps `translations/messages.en.xlf` (or whatever path you choose) in sync by adding or updating the matching `<trans-unit>`.
  - Supports separate templates for Twig, PHP, and JavaScript calls.
  - Tool window on the right lets you tweak templates, choose the XLIFF file, and set an optional ID prefix without digging into config files.

  ## Getting Started

  1. Clone the repo and open it in IntelliJ IDEA or PhpStorm.
  2. Make sure you have an XLIFF translation file handy (default path is `translations/messages.en.xlf`).
  3. Run the plugin with `./gradlew runIde` or build it with `./gradlew buildPlugin`.

  ## Using It

  - Highlight the string you want translated in a `.php`, `.js`, or `.twig` file.
  - Fire the “Generate Translation Unit” action.
  - Keep coding—your translation catalog now knows about that string.

  ## Tweaks

  Open the **Translation Settings** tool window on the right:

  - Update the Twig/PHP/JS templates to match your project conventions.
  - Point to a different XLIFF file if you keep translations elsewhere.
  - Add a prefix so generated IDs follow your team’s naming scheme.