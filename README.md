# PodTube

Because youtube RED is not available in most country, this app allows you to download a youtube video or subscribe to a youtube channel.

The main goal is to be able to subscribe to a channel or a user as we subscribe to podcast. 
To either have an audio or video podcast from youtube.

If you share a youtube user / channel / playlist page with [pushbullet](https://www.pushbullet.com/) open it on your phone,
click on the share button and select PodTube you will be able to subscribe to this feed

For example : https://www.youtube.com/user/CNN

## What's new in 
### 1.1.0:
- Manage playlist : you can now share a playlist from pushBullet or the share android button
### 1.0.0:
- New download manager
- Display notifications when a download start / end
- You can retry / delete / play a download item

## Screenshots

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
- Atom RSS parser : https://github.com/einmalfel/Earl
- Json Parser : https://github.com/google/gson
- File Downloader : https://github.com/lingochamp/FileDownloader

## TODO (next):

- Manually parse feed xml to retrieve the feed / item cover and description
- Find a way to easily subscribe to a feed/user without going through pushbullet.
- Allow to choose in which directory do download (in a settings are at download time)
- Find a way to be podcast player compatible
- Search for feed to add
- Download automatically the last episode released 
- Choose where to save the download 
- Delete a feed 
- Have a cleaner UI
- Improve code


