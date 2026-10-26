MACE LADDER KEYS MOD v1.2.0 (client mod - every player installs it)
Needs Minecraft 26.3, Fabric Loader and Fabric API 0.161.0+26.3.

KEYS (change them in Options > Controls > Key Binds > Mace Abilities)
  `      = Dash (Gold Mace and up)  - Lunge III push forward, 1s cooldown, hold your mace
  Z      = Sky Jump (Amethyst Mace and up) - 20 blocks up
  Space twice in the air = Double Jump (Diamond Mace and up) - hold your mace
Needs the Mace Ladder datapack v13 or newer on the server.

BUILD ON GITHUB (no Java needed on your PC)
1. Make a NEW empty repo on github.com.
2. "uploading an existing file" -> drag in EVERYTHING from this zip (unzipped).
   IMPORTANT: the hidden ".github" folder must be uploaded too. If your file
   browser hides it, do this instead after uploading the rest:
     Add file > Create new file > name it  .github/workflows/build.yml
     and paste in the contents of build.yml from this zip. Commit.
3. Open the Actions tab -> "build" -> wait for the green check (~3-5 min).
4. Scroll down to Artifacts -> download "mace-keys-mod" -> unzip it ->
   put mace-keys-1.2.0.jar (NOT the -sources jar) in your .minecraft/mods folder.
Delete the old mace-keys-1.1.0.jar from mods.
