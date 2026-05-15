# LLM Assistant Application

This application provides a simple command-line interface to interact with AI language models, supporting both OpenAI and Google Gemini models.

## Features

- Support for both OpenAI and Google Gemini AI models
- Extensible design with AIAssistant interface for easy addition of new AI providers
- Factory pattern for creating the appropriate assistant based on configuration
- Configurable model selection with efficient properties caching
- Robust error handling with retry mechanism
- Structured logging with configurable log levels

## Setup

1. Clone the repository
2. Create a `config.properties` file in one of these locations:
   - In the project root directory (recommended for development)
   - In the `src/main/resources` directory (for packaged applications)
   - In your user's working directory
3. Configure your API keys in the `config.properties` file:
   - For OpenAI: Get an API key from [OpenAI Platform](https://platform.openai.com/)
   - For Gemini: Get an API key from [Google AI Studio](https://ai.google.dev/)
4. Build the project using Gradle:
   ```
   ./gradlew build
   ```
5. Run the application:
   ```
   ./gradlew run
   ```

## Configuration

The `config.properties` file may contain the following settings:

- `OPENAI_API_KEY=...`
- `GEMINI_API_KEY=...`
- `AI_LLM=...` <span style="color: gray;">// Possible values: OPENAI, GEMINI; defaults to OPENAI</span>
- `LOG_LEVEL=...` <span style="color: gray;">// Possible values: OFF, ERROR, WARN, INFO, DEBUG, TRACE; defaults to OFF</span>


## 3.3 Task 3: Temperature Tests

Temperature controls how random or creative the model's responses are. A low temperature makes the model more deterministic and predictable, always favouring the most probable next word. A high temperature spreads the probability more evenly across options, producing more varied and creative outputs.

Two prompts were tested at temperature 0 (min.), 0.5 (med.) and 1 (max.).

---

### Test Case 1: Open-ended prompt — `write a completely absurd and chaotic poem with no rules`

**Temperature 0:**
> The whisper of a purple teapot argued with a startled cloud, which promptly deflated into a puddle of forgotten Tuesdays. A monocled squirrel, wearing a tiny sombrero, tapped a rhythm on a banana peel, while the moon, made entirely of lukewarm marmalade, began to sing opera backwards. Gravity took a vacation to a dimension of fluffy socks, leaving the teacups to float aimlessly, each one humming a different, off-key lullaby. A fish on a unicycle, pedaling with existential dread, swam through a forest of spaghetti trees, whose leaves were tiny, indignant alarm clocks. The sky, a patchwork quilt of static electricity and old receipts, suddenly sneezed a rainbow of melted cheese. Everything was Tuesdays. Except for the Tuesdays that were actually Wednesdays pretending to be Tuesdays. And then a spoon spontaneously combusted into a flock of startled pigeons. The end. Or perhaps, the beginning of a very confused sandwich.

**Temperature 0.5:**
> A pickle-jar waltzed with a sentient spoon, while the moon wore a hat made of lukewarm prune. A whisper of spaghetti, a giggle of glue, the sky tasted purple, and so did the zoo. Invisible elephants tap-danced on clouds, wearing tutus of static, escaping the shrouds of a forgotten Tuesday, where teacups could sing, and a walrus played banjo on a trampoline spring. The lamppost declared itself Emperor of Lint, demanding a tribute of peppermint tint. My left sock is plotting a coup with the toast, while a rainbow-striped badger just ghosted my host. So butter your eyebrows and whistle a chair, for the chaos has spoken, and it simply doesn't care!

**Temperature 1:**
> **The Wobble of Whimsy's Whistle**
> Spoonfuls of silent starlight drip through a turnip's ear. The sky hums in purple delight, a rhinoceros sheds a small, wobbly tear. Teacups, jazz-gloved, tap-dance a jig on the back of a particularly nervous fig. Butterflies, crafted from overdue library fines, flap numerical wings over wonky chalk lines. Gravity yawns, redefines "over there," as a banana peels backwards, revealing a stare. A dandelion hums an old opera tune, then turns into marmalade, beneath the wrong moon. No rules, just the glorious, shimmering blur of a pigeon playing chess with a disgruntled purr.

**Analysis:** With an open-ended and creative prompt, the temperature effect is clearly visible. At temperature 0 the poem is more repetitive, reusing the same images (notably "Tuesdays" appears multiple times) and reads more like structured prose than chaos. At temperature 0.5 the output gains rhyme and rhythm, feeling more playful and varied. At temperature 1 the model produces the most genuinely creative result — it invents an original title, uses unexpected imagery ("butterflies crafted from overdue library fines") and feels the most unpredictable and poetic of the three.

### Test Case 2: Factual prompt — `Give me 5 words to describe the color blue`

**Temperature 0:**
> 1. Calm
> 2. Deep
> 3. Vibrant
> 4. Cool
> 5. Serene

**Temperature 0.5:**
> 1. **Serene:** Evokes a sense of calm and peace, like a clear blue sky.
> 2. **Oceanic:** Brings to mind the vastness and depth of the sea.
> 3. **Cool:** Often associated with refreshing and crisp feelings, or lower temperatures.
> 4. **Vibrant:** Describes a bright, lively, and intense shade of blue.
> 5. **Mysterious:** Can suggest the dark, deep, and unknown qualities of certain blues.

**Temperature 1:**
> 1. **Melancholic:** Blue carries an emotional weight, often tied to sadness or longing.
> 2. **Infinite:** Like the sky or ocean, it stretches beyond what the eye can hold.
> 3. **Electric:** Think neon signs, lightning, the buzz of something charged and alive.
> 4. **Hushed:** A quietness lives in blue, the colour of dusk and held breath.
> 5. **Boundless:** Blue refuses edges — it bleeds into horizons and disappears.

**Analysis:** With a simple factual prompt, the temperature effect is very noticeable. At temperature 0 the model gives the most obvious and expected words with no elaboration. At temperature 0.5 it expands each word with a brief explanation, adding context but still choosing safe options. At temperature 1 the word choices become more original and poetic — "hushed" and "boundless" are far less predictable than "calm" or "cool" — showing how higher temperatures push the model toward more creative and unexpected outputs even for straightforward prompts.