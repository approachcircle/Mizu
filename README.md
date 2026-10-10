# Mizu
A very opinionated server plugin with fun addons and Quality Of Life improvements.

## Uhh, should i use this on my own server?
Well, you can use this on your server of course, but bear in mind that this plugin is not
built with other people in mind... Notably, there is an easter egg within the `Utils.error`
function that specifically checks if i (approachcircle) am present in the server when
the exception occurs, and if i or no operators are present, the plugin broadcasts a message
explicitly telling all members to ping me to let me know that the plugin is broken. The
easter egg is more designed for my friends on my own server, so you may want to change that
behaviour yourself...

There are also many features with defaults that make sense for my server configuration, but
may not make sense for yours. For example, there are environment variables inferred for 
sending messages to a discord webhook and reporting to Sentry. One of the biggest ones
is also that, by default, there is a live message translation feature enabled which
expects a LibreTranslate server running locally on the server. If this server is not
running, then expect errors on like every message unless you turn off "translation" in the
config.

TL;DR You can but things aren't guaranteed to work perfectly and your server setup is
expected to be in a very specific configuration for all features to work perfectly. Refer
to the configuration.yml file to alter things for your setup, and use the /killswitch 
feature to disable commands you don't want. I'd say it's generally not a good idea unless
you know what the plugin does in its entirety.

## The build i'm using is broken/isn't working...
You see sometimes mistakes happen and i may accidentally push code with bugs to the 
master branch... So just keep in mind that the current build on the master branch may
have some bugs or issues hiding somewhere. The only way for me to catch these as of right
now is by testing, or being told by one of my friends that something isn't working and
then investigating the corresponding area in code to find the issue. If you do have a
problem, i urge you to open an issue in the "Issues" tab and describe the problem. I
promise that whatever it is, i'd rather know as soon as possible rather than it being
swept under the rug.

## I don't get it
This plugin is a project that has been gradually expanding more and more over the years.
The main purpose of it is to add Quality Of Life features, and fun activities to improve
the experience of me and my friends on Minecraft servers. Therefore, this plugin is an 
amalgamation of random features and things that work for me and my friends, but maybe
not for others. This repository can also act as a portfolio for my experience with
programming in Java, and interacting with the Bukkit/Spigot/Purpur API. But maybe don't
look too far into the files which are older because some of the infrastructure of this
plugin are in a weird inbetween state where they still use older code which i haven't
refactored yet... An example of this refactoring would be the new Storage class i have
implemented, but some things still use old manual file management and error handling
stuff. Pretty much take it with a grain of salt, but most of the code in this repo is
code written to the best of my ability. If you find code that seems to be a bit 
questionable, there is probably a comment explaining why i have made the choices i have
for that scenario.

## What on earth is this build system???
Yeah... Unfortunately when i made this plugin years ago, i was too scared to even think
about or touch a build system like Maven or Gradle, so the best way to build the plugin into
a .JAR is by running the Build Artifacts task in IntelliJ. For other IDEs... I'm honestly
not so sure. Basically the end goal you want is a fat .JAR with all classes and libraries
packaged together, since i am using raw .JAR files as libraries. I promise i will add
a proper build system at some point, because honestly it is kind of painful managing it
this way also.

### WHERE'S THE BUILD TARGET DIRECTORY???
Ah, yeah... forgot about that. The build directory is pointing at a folder called
dev_server which is not in this repo as it is part of the .gitignore since it is a fully
flourished Minecraft server which i run locally on my machine to test builds of the plugin.

## Why are there localisation files for only English and Portuguese?
Again, since this plugin was made to be a utility for improving the experience of my own
personal servers, there is only partial localisation for Portuguese in terms of messages
sent/broadcasted by the plugin. Most of the people who play on my servers are either
fluent in English, or fluent in Portuguese with varying levels of English, hence the 
Portuguese localisation.

## Closing comments
I hope i was able to explain myself and clear up some of the very weird things about
this plugin. If you have any questions or anything like that, message me on discord
(approachcircle) and I'll try to help.