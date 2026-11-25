# RayDej NBA (Android)

A native Android NBA scoreboard app with a blue-and-white visual theme.

## Features
- NBA games and scores from ESPN's public scoreboard JSON feed
- Live / scheduled / final game status
- Manual refresh and automatic refresh every 30 seconds
- Dark navy interface with electric-blue and white typography

## Build an APK
This workspace contains the Android Studio project source. To produce the installable APK:

1. Install Android Studio (current stable) with Android SDK Platform 35 and a compatible JDK.
2. Open this `RayDejNBA` folder in Android Studio.
3. Allow Gradle sync to finish.
4. Select **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. Android Studio will create the APK under `app/build/outputs/apk/debug/`.

For a command-line build, run `gradle assembleDebug` from this folder after installing Gradle and Android SDK.

## Data note
The app reads the public ESPN scoreboard endpoint:
`https://site.api.espn.com/apis/site/v2/sports/basketball/nba/scoreboard`

This is an unofficial public feed, not an official NBA API. Availability, fields, and update timing are controlled by ESPN and may change. An internet connection is required for scores.
