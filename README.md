# Bellows (formally Radon)
Bellows is a no-compromises drop-in Fabric mod designed to apply targeted optimizations for Minecraft commands-- especially in environments with many large datapacks. Do to the targeted nature of Bellow's optimizations, performance gains are somewhat variable. Most large datapacks see a 50-60% reduction in tick time using Bellows. Certain packs like Animated Java can see a 70-90% reduction in tick time.

The following optimizations are used:
* Limiting serialization of NBT data to the requested data instead of all the data, saving massive time on `data get` (etc.) operations. For example, if you do `data get entity @s Health` on a player, normally Minecraft would copy & send *all* of the player NBT data, including the +1000 long array of unlocked recipes. With Bellows, it will copy and send only `Health` and nothing more.
* Deserializing of NBT data is similarly limited to requested data only. This is twice as effective since reading NBT data requires first writing nbt data, which uses the above optimization.
* Entities are cached based on their type (supports type tags) and selector tags. When using @e, the search function will check the caches for valid types and selector tags, and will perform the @e search only on the smallest cache. That is, if you select `@e[type=marker,tag=my_tag]` and there are 100 markers, 50 with `my_tag`, then the @e will only search the 50 entities with `my_tag`. Currently, this does not support type=! and tag=!. Note: As of 1.9.3, Minecraft limits @e searches to specific sub-chunks when using distance= and dx,dy,dz=. Bellows makes no attempt to optimize these searches, it only does global searches.
* Changing entity context (using `execute as` or `execute on`) re-computes the entity display name, which can consume a surprisingly large amount of tick time. Bellows makes this computation lazily executed, so the display name is only computed when needed. Interesting side note: in vanilla, making a resourcepack strip out all entity display names can actually increase datapack performance!
* Fix for [MC-168596](https://bugs.mojang.com/browse/MC-168596) "Chunks outside of render distance are not unloaded if 'execute if block' runs on it every tick." While this may not be noticeable in single player, on a server hundreds of chunks can be force-loaded in random players' bases due to custom blocks receiving tickly `if block` checks.

Special NBT cases:
* "Quick Data:" a type of optimization which pre-compiles an nbt modification and directly updates the entity, bypassing the normal NBT process (even with the other nbt optimization, this can save a lot of time). Currently only applies the transformations, but can be expanded to other data fields.
* The Transformation field on display entities always calculates the matrix form when built, even though the server never uses this form. Bellows disables this construction.
* "data" field on entities uses a standard deep copy instead of a Codec. Reduces time by ~50%.

## Usage
If you're setting up a server with a lot of datapacks and you're already using performance mods (i.e. Lithium), or your computer is truly a potato and you need to squeeze out every drop of performance you can get, then definitely give Bellows a try. Just drop it in your mods folder and datapacks will magically perform better.

While Bellows provides drop-in optimizations, it changes optimization rules that apply in vanilla (like no longer needing to reduce @e usage aggressively). In general, it is considered bad practice to develop optimizations for Bellows specifically, but there are some exceptions like:
* You are in a controlled environment, i.e. your own server which is running Bellows
* You are using a pre-compiler which can provide a normal or Bellows build target

In testing, Moonrise (ports some of paper's optimizations to fabric) seems to synergize to Bellows, reducing tick time by significantly more than either alone. Give it a try!

### Setup

Download the latest version from [Releases](https://github.com/Smithed-MC/Radon/releases) and place the mod in your [Fabric](https://fabricmc.net/) mods folder.

## Advanced Features
Want to see the difference for yourself? Bellows supports toggling optimizations on the fly using these commands. You can observe the mspt in f3, or run a profile with f3+L in single player and /perf <start/stop> in multiplayer:
* `/bellows nbt-optimiations <true/false>`
* `/bellows selector-optimizations <true/false>`
* `/bellows fix-block-access-forceload <true/false>`

Bellows also has a debug mode, which can print potential problems to console if you find your commands aren't running faster or worse are running slower.
* `/bellows debug <command>` # this prints debug info from a single command
* `/bellows debug-mode <true/false>` # this prints debug info for all commands (don't do this with ticking commands running)
