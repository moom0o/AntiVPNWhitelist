# AntiVPNWhitelist
Protect your minecraft server from bots!
<img src="https://bstats.org/signatures/bukkit/AntiVPNWhitelist.svg">
## How to use? 
**Make sure to set your api key in config.yml.** You can get one at proxycheck.io. You are allowed 1000 requests per day for free. This plugin will automatically cache ips until the server reboots, so you won't use up your requests so easily.
## Config
```
# Get this by creating an account at https://proxycheck.io Free plan allows you to have 1000 checks per day.
proxycheck_api_key: xxxxxx-xxxxxx-xxxxxx-xxxxxx

options:
  # Requires you to move before chatting on join.
  MoveToChat: true
  # Only sends join message if player is not on a vpn/is whitelisted
  DoNotSendJoinMessage: true
  # Maximum VPN users allowed online at a time (these users still wont be able to chat) ---- SET TO -1 TO NOT ALLOW ANY VPN USERS IN.
  MaxVPNUsers: 10
  # Max players until join messages disabled
  MaxPlayersJoinmessages: 50

messages:
  # Message when someone joins with a vpn and they try to talk
  UnableToChatMessage:
    - "&7You can not chat because you are using a &cvpn."
    - "&7In order to chat you must &cwhitelist yourself at "
    - "&6https://whitelist.moobot.dev/"
    - "&cAfter adding your name make sure to relog."
  # Message when someone tries to talk without moving upon join.
  MoveToChat: "&4You need to actually move before you can do shit."
  # Message when server is in bot attack mode and does not let anyone else in the server
  BotAttackMode: "&4Bot attack mode is currently enabled\n&6You can bypass this by going to https://whitelist.moobot.dev"
  ```
