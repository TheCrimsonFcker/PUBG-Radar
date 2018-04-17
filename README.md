Bluehole encrypted packets after 3.7.19.

I will not provide decryption method in this fork, __this is not a "Ready to work" source code.__ I only update some features and UI staffs here.(It's not me figured out how to decrypt, I just copy and paste from Internet.)


# About
Big thanks to [@AiYinZiLeGong](https://github.com/AiYinZiLeGong) [@Jerry1211](https://github.com/Jerry1211) and [@SamuelNZ](https://github.com/SamuelNZ). Everthing here is based on their work.

I'm not a prof. programmer, just a noob learns to code. I even don't know the difference between _val_ and _var_. So feel free to optimize my shitty code.

And this is a personal programme based on my usage habit, so I don't have the ability to solve all your problems or requests. But you can get help from [here](https://github.com/AiYinZiLeGong/PUBG-Radar/issues).

<br />

# Radar Unknown
![RadarUnknown](https://i.imgur.com/kfFZfnX.png)

(I prefer the neat shapes icons. If you like item icons, you can try [@Jerry1211's fork](https://github.com/Jerry1211/RadarProject).)

![PlayerInfo](http://ww1.sinaimg.cn/large/006NGikrgy1fpl2nlkublj30jh0gnq52.jpg)

(You know exactly how many shots you need to kill a enemy from healthbar color, so you can press 0 to hide gear info.)

__Item Lgend:__ 

[Click here](https://i.imgur.com/p69oQhX.png)

__Key Binds:__

Key | Function |
---|---|
1 | Show/hide Weapons |
2 | Show/hide Attachment |
3 | Show/hide Lv.2 Gear |
4 | Show/hide Reddot/Holosight/2X scpoe |
5 | Center on player or Quick zoom-in/out |
6 | Zoom-out |
7 | Zoom-in |
8 | Center on latest airdrop / Center on player|
9 | Show/hide Compass |
0 | Show enemy's angle&distance info / Show enemy's name |
\- | Show/hide enemy's gear info |
= | Show/hide enemy's med items info |
↑ ↓ ← → | Move map |
Mouse Left Button | Drag map |
Mouse Middle Button | Center on player or Quick zoom-in/out |

<br />

# Changelog 

__20180417__
* Updated Miramar map to 3.7.28.
* Updated data struct to 3.7.28.
* Added Solo/Team mode check in ```RetrievePlayerInfo.kt```.
* Press ```0``` to show enemy's name.
* Press ```=``` to show enemy's med item number.
* Fixed safe circle count down.
* Added a indicator to show how many enemies' info we obtained.
* Added a indicator to show how many players are spectating you.
* Automatically hide death box in War mode.
* Automatically hide player's stats in War mode.
* Automatically hide player's stats if he is down.
* Colored enemy's healthbar in gray if he is down.

__20180315__
* Updated to compatible with latest game version 3.7.27.
* Automaticlly filter Lv.2 gears on map when you have it.

__20180325__
* Optimized ```RetrievePlayerInfo.kt```.

__20180324__

![Hacker](https://i.imgur.com/Mp2JKsy.png)
* Show HSR and KDR when enemy is an aimbot suspect.
* Optimized the method to check aimbot hacker. 
* Added teammate pin marker.
* Added Flare Gun for event mode.

__20180321__
* Rewrote ```RetrievePlayerInfo.kt```. Now we can get player stats from [pubg.op.gg](https://pubg.op.gg). 
* Check aimbot suspects (KD > 3 and Headshot kill rate > 35%), and draw them in pink.

__20180319__
* Major Update: Airdrop chaser.

__20180315__
* Updated to compatible with latest game version 3.7.19.

__20180311__
* Redesigned player info display.

__20180307__
* Major Update: Window-edge display.

<br />

# Major Update 

## Airdrop Chaser

![Airdrop](https://i.imgur.com/UWpOK8f.png)

* Add sound notification for airdrop.
* Display items in airdrop when you zoom in. ([Sample](https://imgur.com/a/s2YkQ))
* Press 8 to focus on latest airdrop.

## Window Edge Display

![WindowEdge](https://i.imgur.com/0HUtbSj.png)

* Show airdrops/enemies on window edge when they are out of screen but within certain range. ([Demo](https://gfycat.com/gifs/detail/FriendlyLonelyEider)).

<br />

# Build
Using [maven](https://maven.apache.org/)
