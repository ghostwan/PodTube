# PodTube
![alt text](screenshots/icon.png "") 

Because [youtube RED](https://www.youtube.com/red) is not available in most country, this app allows you to download a youtube video or subscribe to a youtube channel.

The main goal is to be able to subscribe to a channel or a user as we subscribe to podcast. 
To either have an audio or video podcast from youtube and be able to play in background our favorites youtube shows.

If you share a youtube user / channel / playlist page with [pushbullet](https://www.pushbullet.com/) open it on your phone,
click on the share button and select PodTube you will be able to subscribe to this feed

For example : https://www.youtube.com/user/CNN

Or you can directly from your phone share a video or a playlist to either download it or add the feed in your library.

## What's new in 

### 1.3.0:
- Manually parse feed xml to retrieve the feed / item cover and description
- Get feed / entry thumbnail
### 1.2.0:
- Change downloader library to improve download performance use a modify version of Gigaget
- Add preference to change downloads path
- Delete a feed from the library
### 1.1.0:
- Manage playlist : you can now share a playlist from pushBullet or from the share button in the youtube android app
### 1.0.0:
- New download manager
- Display notifications when a download start / end
- You can retry / delete / play a download item

## Screenshots

### Share Feed / Playlist to library 
![alt text](screenshots/share_playlist.png "browse a feed list (user/channel/playlist)")
### Browse a feed 
![alt text](screenshots/add_feed.png "browse a feed list (user/channel/playlist)")
### Feed library
![alt text](screenshots/feed_list.png "Library list")
### Download video / audio
![alt text](screenshots/download_item.png "Library list")
### Download Manager
![alt text](screenshots/download_manager.png "Library list")

## Libraries:

- Android based YouTube url extractor : https://github.com/HaarigerHarald/android-youtubeExtractor
- Annotation-triggered method call by specified thread : https://github.com/KoMinkyu/teaspoon
- Json Parser : https://github.com/google/gson
- Gigaget Downloader : https://github.com/PaperAirplane-Dev-Team/GigaGet
- Butterknife View binder : https://github.com/JakeWharton/butterknife
- Glide Image downloader : https://github.com/bumptech/glide
- Simple XML parser : http://simple.sourceforge.net/

## TODO (next):

- Find a way to easily subscribe to a feed/user without going through pushbullet.
- Allow to choose in which directory do download a file at download time
- Find a way to be podcast player compatible
- Search for feed to add
- Download automatically the last episode released 
- Have a cleaner UI
- Improve code

WARNING: PUTTING PODTUBE OR ANY FORK OF IT INTO GOOGLE PLAYSTORE VIOLATES THEIR TERMS OF CONDITIONS.


