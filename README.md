# EithonBungee

A plugin to handle BungeeCord stuff.

## Release history

### 1.7 (2016-05-28)

* NEW: Now has switch server messages.

### 1.6 (2016-05-26)

* CHANGE: Welcome new player message now sent to all servers, but only if player joined Hub for the first time.

### 1.5 (2016-05-23)

* NEW: Added commands "ban add", "ban remove" and "ban list".

### 1.4 (2016-05-22)

* NEW: Support for temporarily banning players from servers. (Needed in from EithonHardcore plugin).

### 1.3.3 (2016-05-22)

* BUG: When adding a warp location, the current server was not updated - only all other servers.

### 1.3.2 (2016-05-15)

* BUG: There were double quit messages on the server you quit from.

### 1.3.1 (2016-05-15)

* BUG: There were double join messages on the server you joined.
* BUG: When you left the current server for another server, the original server lost track of you. 

### 1.3 (2016-05-14)

* CHANGE: Refresh warp locations based on events, not on a timer.
* BUG: Leave messages did not show on other servers.
* BUG: First person to join a server did not result in a join message on other servers.

### 1.2 (2016-05-13)

* NEW: Moved all join/leave code to a new package.

### 1.1 (2016-05-10)

* NEW: Moved all Bungee related code from EithonLibrary to BungeePlugin.

### 1.0 (2016-05-09)

* CHANGE: Complete rewrite of how we keep track of all players on all BungeeCord servers.

### 0.9 (2016-05-08)

* CHANGE: Replaced a frequent call to the database with a scheduled call.

### 0.8 (2016-05-06)

* NEW: Added connection to EithonBungeeFixes
* BUG: When warping, the player sometimes is sent to the server, but not to the final warp location.

### 0.7 (2016-04-27)

* NEW: Added permissions for teleporting to other servers.
* NEW: Added a server command (and removed it from eithonfixes)
* BUG: Could not handle list of bungee players on server with no bungee connnection

### 0.6 (2016-04-03)

* NEW: Added support for warp

### 0.5 (2016-04-02)

* NEW: Added support for message and reply

### 0.4 (2016-03-30)

* NEW: Added support for tpa, tphere, tpahere, deny and accept

### 0.3 (2016-03-28)

* CHANGE: Now deletes records when quitting.

### 0.2.1 (2016-03-28)

* BUG: Moving between servers could result in thinking that the player has quitted.

### 0.2 (2016-03-28)

* NEW: First working version.

### 0.1 (2016-01-02)

* NEW: First try. 
